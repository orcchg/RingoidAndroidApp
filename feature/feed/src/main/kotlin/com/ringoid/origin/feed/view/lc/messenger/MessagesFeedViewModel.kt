package com.ringoid.origin.feed.view.lc.messenger

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ringoid.analytics.Analytics
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.view.ViewState
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.feed.CacheBlockedProfileIdUseCase
import com.ringoid.domain.interactor.feed.ClearCachedAlreadySeenProfileIdsUseCase
import com.ringoid.domain.interactor.feed.GetLcUseCase
import com.ringoid.domain.interactor.feed.property.GetCachedFeedItemByIdUseCase
import com.ringoid.domain.interactor.feed.property.TransferFeedItemUseCase
import com.ringoid.domain.interactor.feed.property.UpdateFeedItemAsSeenUseCase
import com.ringoid.domain.interactor.image.CountUserImagesUseCase
import com.ringoid.domain.interactor.messenger.ClearMessagesForChatUseCase
import com.ringoid.domain.interactor.messenger.GetChatOnlyUseCase
import com.ringoid.domain.interactor.messenger.GetChatUseCase
import com.ringoid.domain.log.SentryUtil
import com.ringoid.domain.memory.ChatInMemoryCache
import com.ringoid.domain.memory.IFiltersSource
import com.ringoid.domain.memory.IUserInMemoryCache
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.feed.LmmSlice
import com.ringoid.domain.model.messenger.EmptyChat
import com.ringoid.origin.feed.misc.HandledPushDataInMemory
import com.ringoid.origin.feed.view.lc.base.BaseLcFeedViewModel
import com.ringoid.origin.feed.view.lmm.SEEN_ALL_FEED
import com.ringoid.origin.feed.view.lmm.base.PUSH_NEW_MATCHES_TOTAL
import com.ringoid.origin.feed.view.lmm.base.PUSH_NEW_MESSAGES
import com.ringoid.origin.feed.view.lmm.base.PUSH_NEW_MESSAGES_TOTAL
import com.ringoid.origin.utils.ScreenHelper
import com.ringoid.origin.view.main.LcNavTab
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.internal.functions.ObjectHelper
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class MessagesFeedViewModel @Inject constructor(
    private val getChatUseCase: GetChatUseCase,
    private val getChatOnlyUseCase: GetChatOnlyUseCase,
    getLcUseCase: GetLcUseCase,
    getCachedFeedItemByIdUseCase: GetCachedFeedItemByIdUseCase,
    updateFeedItemAsSeenUseCase: UpdateFeedItemAsSeenUseCase,
    transferFeedItemUseCase: TransferFeedItemUseCase,
    clearCachedAlreadySeenProfileIdsUseCase: ClearCachedAlreadySeenProfileIdsUseCase,
    clearMessagesForChatUseCase: ClearMessagesForChatUseCase,
    cacheBlockedProfileIdUseCase: CacheBlockedProfileIdUseCase,
    countUserImagesUseCase: CountUserImagesUseCase,
    filtersSource: IFiltersSource, userInMemoryCache: IUserInMemoryCache, app: Application)
    : BaseLcFeedViewModel(
        getLcUseCase,
        getCachedFeedItemByIdUseCase,
        updateFeedItemAsSeenUseCase,
        transferFeedItemUseCase,
        clearCachedAlreadySeenProfileIdsUseCase,
        clearMessagesForChatUseCase,
        cacheBlockedProfileIdUseCase,
        countUserImagesUseCase,
        filtersSource, userInMemoryCache, app) {

    private val incomingPushMatch = PublishSubject.create<BusEvent>()
    private val incomingPushMessages = PublishSubject.create<BusEvent>()
    internal val pushNewMatch by lazy { MutableLiveData<Long>() }
    internal val pushNewMessage by lazy { MutableLiveData<Long>() }

    init {
        // show 'tap-to-refresh' popup on Feed screen
        incomingPushMatch
            .debounce(DomainUtil.DEBOUNCE_PUSH, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({
                // show badge on Messages LC tab (as being for new Matches)
                viewState.value = ViewState.DONE(PUSH_NEW_MATCHES_TOTAL)
                // show 'tap-to-refresh' popup on Feed screen
                refreshOnPush.value = true
            }, Timber::e)

        incomingPushMessages
            .subscribeOn(Schedulers.computation())
            .map { (it as BusEvent.PushNewMessage).peerId }
            // consume push event and skip any updates if target Chat is currently open
            .filter { !ChatInMemoryCache.isChatOpen(chatId = it) }
            .distinctUntilChanged { prev, cur -> checkFlagAndDrop() && ObjectHelper.equals(prev, cur) }
            .flatMapSingle { peerId ->
                val params = Params().put(ScreenHelper.getLargestPossibleImageResolution(context))
                                     .put("chatId", peerId)
                getChatUseCase.source(params = params)
                    .doOnSuccess { markFeedItemAsNotSeen(feedItemId = peerId) }
                    .onErrorResumeNext { Single.just(EmptyChat) }
                    .map { peerId }
            }  // use case will deliver it's result to Main thread
            .doOnNext { viewState.value = ViewState.DONE(PUSH_NEW_MESSAGES(profileId = it)) }
            .debounce(DomainUtil.DEBOUNCE_PUSH, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({
                // show badge on Messages LC tab
                viewState.value = ViewState.DONE(PUSH_NEW_MESSAGES_TOTAL)
                // show 'tap-to-refresh' popup on Feed screen
                refreshOnPush.value = true
            }, Timber::e)
    }

    // ------------------------------------------
    override fun countNotSeen(feed: List<FeedItem>): List<String> =
        feed.takeIf { it.isNotEmpty() }
            ?.let { items ->
                items.map { it.id to it.countOfPeerMessages() }
                     .filter { it.second > 0 }
                     .filter { it.second > ChatInMemoryCache.getPeerMessagesCount(it.first) }
                     .map { it.first }
            } ?: emptyList()

    // ------------------------------------------
    private var compareFlag = AtomicBoolean(true)

    private fun allowSingleUnchanged() {
        compareFlag.set(false)
    }

    private fun checkFlagAndDrop(): Boolean = compareFlag.getAndSet(true)

    // ------------------------------------------
    override fun getFeedFlag(): Int = SEEN_ALL_FEED.FEED_MESSENGER

    override fun getSourceFeed(): LcNavTab = LcNavTab.MESSAGES

    override fun getFeedName(): String = DomainUtil.SOURCE_FEED_MESSAGES

    override fun sourceBadge(): Observable<Boolean> =
        getLcUseCase.repository.badgeMessenger
            .doAfterNext {
                if (it && getUserVisibleHint()) {
                    analyticsManager.fireOnce(Analytics.AHA_FIRST_MESSAGE_RECEIVED, "sourceFeed" to getFeedName())
                }
            }

    override fun sourceFeed(): Observable<LmmSlice> = getLcUseCase.repository.feedMessages

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun handleUserVisibleHint(isVisibleToUser: Boolean) {
        super.handleUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser /** switched to this LC tab */ && badgeIsOn /** has new feed items */) {
            analyticsManager.fireOnce(Analytics.AHA_FIRST_MESSAGE_RECEIVED, "sourceFeed" to getFeedName())
        }
    }

    // --------------------------------------------------------------------------------------------
    override fun onChatClose(profileId: String, imageId: String) {
        super.onChatClose(profileId, imageId)
        markFeedItemAsSeen(feedItemId = profileId)
    }

    /* Event Bus */
    // --------------------------------------------------------------------------------------------
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventPushNewMatch(event: BusEvent.PushNewMatch) {
        Timber.d("Received bus event: $event")
        SentryUtil.breadcrumb("Bus Event ${event.javaClass.simpleName}", "event" to "$event")
        HandledPushDataInMemory.incrementCountOfHandledPushMatches()
        pushNewMatch.value = 0L  // for particle animation
        incomingPushMatch.onNext(event)  // for 'tap-to-refresh' popup
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventPushNewMessage(event: BusEvent.PushNewMessage) {
        Timber.d("Received bus event: $event")
        SentryUtil.breadcrumb("Bus Event ${event.javaClass.simpleName}", "event" to "$event")
        HandledPushDataInMemory.incrementCountOfHandledPushMessages()
        if (!ChatInMemoryCache.isChatOpen(chatId = event.peerId)) {
            pushNewMessage.value = 0L  // for particle animation
        }
        incomingPushMessages.onNext(event)
    }
}