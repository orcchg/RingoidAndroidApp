package com.ringoid.data.repository.image

import com.ringoid.data.action_storage.ActionObjectPool
import com.ringoid.data.local.database.dao.image.ImageDao
import com.ringoid.data.local.database.dao.image.ImageRequestDao
import com.ringoid.data.local.database.model.image.ImageRequestDbo
import com.ringoid.data.local.database.model.image.UserImageDbo
import com.ringoid.data.local.memory.UserInMemoryCache
import com.ringoid.data.local.shared_prefs.accessCompletable
import com.ringoid.data.local.shared_prefs.accessSingle
import com.ringoid.data.remote.RingoidCloud
import com.ringoid.data.remote.model.image.UserImageEntity
import com.ringoid.data.repository.BaseRepository
import com.ringoid.data.repository.handleError
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.exception.isFatalApiError
import com.ringoid.domain.log.SentryUtil
import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.essence.image.*
import com.ringoid.domain.model.image.Image
import com.ringoid.domain.model.image.UserImage
import com.ringoid.domain.model.mapList
import com.ringoid.domain.manager.ISharedPrefsManager
import com.ringoid.domain.repository.image.IUserImageRepository
import com.ringoid.utility.uriString
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class UserImageRepository @Inject constructor(
    @Named("user") private val local: ImageDao,
    @Named("user") private val imageRequestLocal: ImageRequestDao,
    private val userInMemoryCache: UserInMemoryCache,
    cloud: RingoidCloud, spm: ISharedPrefsManager, aObjPool: ActionObjectPool)
    : BaseRepository(cloud, spm, aObjPool), IUserImageRepository {

    override val imageBlocked = PublishSubject.create<String>()
    override val imageCreated = PublishSubject.create<String>()
    override val imageDeleted = PublishSubject.create<String>()
    override val totalUserImages = PublishSubject.create<Int>()

    override fun countUserImages(): Single<Int> =
        local.countUserImages()
             .doOnSuccess {
                 userInMemoryCache.setUserImagesCount(it)
                 totalUserImages.onNext(it)
             }

    // ------------------------------------------------------------------------
    override fun getUserImage(id: String): Single<UserImage> = local.userImage(id).map { it.map() }

    override fun getUserImages(resolution: ImageResolution): Single<List<UserImage>> {
        fun fetchUserImages(): Single<List<UserImage>> =
            spm.accessSingle { cloud.getUserImages(it.accessToken, resolution) }
                .handleError(tag = "getUserImages($resolution)")
                .flatMap {
                    Observable.fromIterable(it.images)  // images fetched from the Server
                        .zipWith(Observable.range(0, it.images.size), BiFunction { image: UserImageEntity, index: Int -> image to index })
                        .doOnNext { (image, index) ->
                            if (image.isBlocked && image.isBlocked != local.isUserImageBlockedByOriginId(originImageId = image.originId)) {
                                Timber.v("Image with id [${image.originId}] has been blocked by Moderator on the Server")
                                imageBlocked.onNext(image.originId)  // notify image has been blocked by Moderator
                            }
                            local.updateUserImageByOriginId(originImageId = image.originId, uri = image.uri,
                                numberOfLikes = image.numberOfLikes, isBlocked = image.isBlocked, sortPosition = index)
                        }
                        .toList()
                }
                .onErrorResumeNext {
                    if (it.isFatalApiError()) {
                        Single.error(it)
                    } else {
                        Single.just(emptyList())
                    }
                }
                .flatMap { countUserImages() }  // actualize user images count
                .flatMap { local.userImages() }
                .map { it.mapList() }

        return imageRequestLocal.countRequests()
            .flatMap { count ->
                DebugLogUtil.i("Total count of failed image requests: $count")
                if (count > 0) {
                    imageRequestLocal.requests()
                        .flatMap {
                            DebugLogUtil.v("Pending image requests [${it.size}]: ${it.joinToString("\n\t\t", "\n\t\t", "\n", transform = { it.type })}")
                            Observable.fromIterable(it)
                                .map { request ->
                                    when (request.type) {
                                        ImageRequestDbo.TYPE_CREATE -> {
                                            DebugLogUtil.i("Execute 'create image' request again, as it's failed before")
                                            createImageRemote(request.createRequestEssence(), request.imageFilePath, retryCount = 0)
                                                .ignoreElement()
                                        }
                                        ImageRequestDbo.TYPE_DELETE -> {
                                            DebugLogUtil.i("Execute 'delete image' request again, as it's failed before")
                                            deleteUserImageRemote(request.deleteRequestEssence(), retryCount = 0)
                                        }
                                        else -> Completable.complete()  // ignored item
                                    }
                                    .doOnComplete { imageRequestLocal.deleteRequest(request) }
                                }
                                .toList()
                        }
                        .flatMap {
                            Completable.concat(it)
                                .onErrorComplete()  // if any request fails, fetch user images whatever it is on the Server
                                .toSingle { 0L }
                        }
                        .flatMap { fetchUserImages().doOnSubscribe { Timber.v("Fetch actualized list of images") } }
                } else {
                    fetchUserImages().doOnSubscribe { Timber.v("Fetch list of images") }
                }
            }
    }

    override fun getUserImagesAsync(resolution: ImageResolution): Observable<List<UserImage>> =
        Observable.concatArray(local.userImages().map { it.mapList() }.toObservable(), getUserImages(resolution).toObservable())

    // ------------------------------------------------------------------------
    override fun deleteUserImage(essence: ImageDeleteEssenceUnauthorized): Completable =
        deleteUserImage(essence, retryCount = BuildConfig.DEFAULT_RETRY_COUNT)

    private fun deleteUserImage(essence: ImageDeleteEssenceUnauthorized, retryCount: Int): Completable =
        spm.accessCompletable { deleteUserImageAsync(ImageDeleteEssence.from(essence, it.accessToken), retryCount) }

    /**
     * Perform deleting image locally and then remotely. If remote delete has failed, record that
     * as pending request to repeat later, on next [getUserImages] call.
     */
    private fun deleteUserImageAsync(essence: ImageDeleteEssence, retryCount: Int): Completable =
        local.userImage(id = essence.imageId)
            .flatMapCompletable { localImage ->
                Single.fromCallable { local.deleteImage(id = essence.imageId) }
                    .doOnSuccess { imageDeleted.onNext(essence.imageId) }  // notify database changed
                    .flatMap { countUserImages() }  // actualize user images count
                    .flatMapCompletable { deleteUserImageRemoteImpl(localImage, essence, retryCount) }
            }

    private fun deleteUserImageRemote(essence: ImageDeleteEssence, retryCount: Int): Completable =
        local.userImage(id = essence.imageId)
            .flatMapCompletable { deleteUserImageRemoteImpl(localImage = it, essence = essence, retryCount = retryCount) }

    private fun deleteUserImageRemoteImpl(localImage: UserImageDbo, essence: ImageDeleteEssence, retryCount: Int): Completable {
        fun addPendingDeleteImageRequest() {
            imageRequestLocal.addRequest(ImageRequestDbo.from(essence))
        }

        return if (localImage.originId.isNullOrBlank()) {
            SentryUtil.w("Deleted local image that has no remote pair")
            Completable.complete()
        } else {
            spm.accessSingle { cloud.deleteUserImage(essence.copyWith(imageId = localImage.originId)) }
                .handleError(count = retryCount, tag = "deleteUserImage")
                .doOnDispose {
                    DebugLogUtil.i("Cancelled image delete")
                    addPendingDeleteImageRequest()
                }
                .doOnError {
                    DebugLogUtil.w("Failed to delete image")
                    addPendingDeleteImageRequest()
                }
                .ignoreElement()
        }
    }

    /**
     * Perform deleting image remotely and then, if succeeded, locally. If failed, show error.
     * Perform less retries in order to fallback earlier in case of error.
     */
    private fun deleteUserImageSync(essence: ImageDeleteEssence, retryCount: Int): Completable =
        local.userImage(id = essence.imageId)
            .flatMap { localImage ->
                if (localImage.originId.isNullOrBlank()) {
                    SentryUtil.w("Deleted local image that has no remote pair")
                    Single.just(0L)
                } else {
                    spm.accessSingle { cloud.deleteUserImage(essence.copyWith(imageId = localImage.originId)) }
                        .handleError(count = minOf(3, retryCount) /* less retries */, tag = "deleteUserImage")
                }
            }
            .flatMap {
                Single.fromCallable { local.deleteImage(id = essence.imageId) }
                      .doOnSuccess { imageDeleted.onNext(essence.imageId) }  // notify database changed
            }
            .flatMap { countUserImages() }
            .ignoreElement()  // convert to Completable

    override fun deleteLocalUserImages(): Completable = Completable.fromCallable { local.deleteAllImages() }

    override fun deleteLocalUserImageRequests(): Completable =
        Completable.fromCallable { imageRequestLocal.deleteAllRequests() }

    // ------------------------------------------------------------------------
    override fun createImage(essence: IImageUploadUrlEssence, imageFilePath: String): Single<Image> =
        createImage(essence, imageFilePath, retryCount = BuildConfig.DEFAULT_RETRY_COUNT)

    private fun createImage(essence: IImageUploadUrlEssence, imageFilePath: String, retryCount: Int): Single<Image> =
        spm.accessSingle { accessToken ->
            val xessence = when (essence) {
                is ImageUploadUrlEssence -> essence  // for ImageUploadUrlEssence with access token supplied
                is ImageUploadUrlEssenceUnauthorized -> ImageUploadUrlEssence.from(essence, accessToken.accessToken)
                else -> throw IllegalArgumentException("Unsupported implementation of IImageUploadUrlEssence for createImage()")
            }

            val imageFile = File(imageFilePath)
            val uriLocal = imageFile.uriString()
            val localImage = UserImageDbo(id = xessence.clientImageId, uri = uriLocal, uriLocal = uriLocal)

            Single.fromCallable { local.addImage(localImage) }
                .doOnSuccess { imageCreated.onNext(xessence.clientImageId) }  // notify database changed
                .flatMap { countUserImages() }  // actualize user images count
                .flatMap { createImageRemoteImpl(localImage = localImage, essence = xessence, imageFilePath = imageFilePath, retryCount = retryCount) }
        }

    private fun createImageRemote(essence: ImageUploadUrlEssence, imageFilePath: String, retryCount: Int): Single<Image> =
        local.userImage(id = essence.clientImageId)
            .flatMap { createImageRemoteImpl(localImage = it, essence = essence, imageFilePath = imageFilePath, retryCount = retryCount) }

    private fun createImageRemoteImpl(localImage: UserImageDbo, essence: ImageUploadUrlEssence, imageFilePath: String, retryCount: Int): Single<Image> {
        fun addPendingCreateImageRequest(imageFilePath: String) {
            imageRequestLocal.addRequest(ImageRequestDbo.from(essence, imageFilePath))
        }

        return cloud.getImageUploadUrl(essence)
            .flatMap { image ->
                if (image.imageUri.isNullOrBlank()) {
                    Single.error(NullPointerException("Upload uri is null: $image"))
                } else {
                    /**
                     * Update [UserImageDbo.originId] and [UserImageDbo.uri] with remote-generated id and uri
                     * for image in local cache, keeping [UserImageDbo.id] and [UserImageDbo.uriLocal] unchanged.
                     */
                    val updatedLocalImage = localImage.copyWith(originId = image.originImageId, uri = image.imageUri)
                    Completable.fromCallable { local.updateUserImage(updatedLocalImage) }  // local image now has proper originId and remote url
                        .toSingleDefault(image)
                }
            }
            .flatMap {
                val imageFile = File(imageFilePath)
                cloud.uploadImage(url = it.imageUri!!, image = imageFile)
                    .andThen(Single.just(it))
            }
            .handleError(count = retryCount, tag = "createImage")
            .doOnDispose {
                DebugLogUtil.i("Cancelled image create and upload")
                addPendingCreateImageRequest(imageFilePath)
            }
            .doOnError {
                DebugLogUtil.w("Failed to create and upload image")
                addPendingCreateImageRequest(imageFilePath)
            }
            .map { it.map() }
    }

    // ------------------------------------------------------------------------
    override fun getImageUploadUrl(essence: ImageUploadUrlEssence): Single<Image> =
        spm.accessSingle { cloud.getImageUploadUrl(essence).map { it.map() } }

    override fun uploadImage(url: String, image: File): Completable = cloud.uploadImage(url = url, image = image)
}
