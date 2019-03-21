package com.ringoid.data.remote

import com.ringoid.data.remote.model.BaseResponse
import com.ringoid.data.remote.model.actions.CommitActionsResponse
import com.ringoid.data.remote.model.feed.FeedResponse
import com.ringoid.data.remote.model.feed.LmmResponse
import com.ringoid.data.remote.model.image.ImageUploadUrlResponse
import com.ringoid.data.remote.model.image.UserImageListResponse
import com.ringoid.data.remote.model.user.AuthCreateProfileResponse
import com.ringoid.data.remote.model.user.UserSettingsResponse
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.*

interface RingoidRestAdapter {

    /* User (Auth, Profile) */
    // --------------------------------------------------------------------------------------------
    @POST("auth/create_profile")
    fun createUserProfile(@Body body: RequestBody): Single<AuthCreateProfileResponse>

    @POST("auth/delete")
    fun deleteUserProfile(@Body body: RequestBody): Single<BaseResponse>

    @GET("auth/get_settings")
    fun getUserSettings(@Query("accessToken") accessToken: String): Single<UserSettingsResponse>

    @POST("auth/update_settings")
    fun updateUserSettings(@Body body: RequestBody): Single<BaseResponse>

    // ------------------------------------------
    @POST("auth/claim")
    fun applyReferralCode(@Body body: RequestBody): Completable

    /* Actions */
    // --------------------------------------------------------------------------------------------
    @POST("actions/actions")
    fun commitActions(@Body body: RequestBody): Single<CommitActionsResponse>

    /* Image */
    // --------------------------------------------------------------------------------------------
    @POST("image/get_presigned")
    fun getImageUploadUrl(@Body body: RequestBody): Single<ImageUploadUrlResponse>

    @GET("image/get_own_photos")
    fun getUserImages(@Query("accessToken") accessToken: String,
                      @Query("resolution") resolution: String): Single<UserImageListResponse>

    @POST("image/delete_photo")
    fun deleteUserImage(@Body body: RequestBody): Single<BaseResponse>

    @PUT
    fun uploadImage(@Url url: String, @Body body: RequestBody): Completable

    /* Feed */
    // --------------------------------------------------------------------------------------------
    @GET("feeds/get_new_faces")
    fun getNewFaces(@Query("accessToken") accessToken: String,
                    @Query("resolution") resolution: String?,
                    @Query("limit") limit: Int?,
                    @Query("lastActionTime") lastActionTime: Long = 0L): Single<FeedResponse>

    @GET("feeds/get_lmm")
    fun getLmm(@Query("accessToken") accessToken: String,
               @Query("resolution") resolution: String?,
               @Query("source") source: String?,
               @Query("lastActionTime") lastActionTime: Long = 0L): Single<LmmResponse>

    /* Test */
    // --------------------------------------------------------------------------------------------
    @POST("timeout")
    fun debugTimeout(): Completable

    @POST("invalidtoken")
    fun debugInvalidToken(): Completable

    @POST("nonok")
    fun debugNotSuccess(): Completable

    @POST("old_version")
    fun debugOldVersion(): Completable

    @POST("internalerror")
    fun debugServerError(): Completable
}
