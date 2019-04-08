package com.ringoid.origin.feed.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.swiperefreshlayout.refreshes
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.view.BaseListFragment
import com.ringoid.base.view.ViewState
import com.ringoid.domain.model.image.EmptyImage
import com.ringoid.origin.AppRes
import com.ringoid.origin.error.handleOnView
import com.ringoid.origin.feed.OriginR_string
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.adapter.base.*
import com.ringoid.origin.feed.misc.OffsetScrollStrategy
import com.ringoid.origin.feed.model.FeedItemVO
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.origin.feed.view.lmm.ILmmFragment
import com.ringoid.origin.navigation.*
import com.ringoid.origin.view.common.EmptyFragment
import com.ringoid.origin.view.common.visibility_tracker.TrackingBus
import com.ringoid.origin.view.dialog.Dialogs
import com.ringoid.utility.*
import com.ringoid.utility.collection.EqualRange
import com.ringoid.widget.view.swipes
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.fragment_feed.*
import timber.log.Timber

abstract class FeedFragment<VM : FeedViewModel> : BaseListFragment<VM>() {

    protected lateinit var feedAdapter: BaseFeedAdapter
        private set
    private lateinit var feedTrackingBus: TrackingBus<EqualRange<ProfileImageVO>>
    private lateinit var imagesTrackingBus: TrackingBus<EqualRange<ProfileImageVO>>

    override fun getLayoutId(): Int = R.layout.fragment_feed
    override fun getRecyclerView(): RecyclerView = rv_items

    protected abstract fun createFeedAdapter(): BaseFeedAdapter

    protected abstract fun getEmptyStateInput(mode: Int): EmptyFragment.Companion.Input?

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        fun onErrorState() {
            onClearState(mode = ViewState.CLEAR.MODE_NEED_REFRESH)
        }

        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.CLEAR -> onClearState(mode = newState.mode)
            is ViewState.DONE -> {
                when (newState.residual) {
                    is BLOCK_PROFILE -> {
                        val count = feedAdapter.getModelsCount()
                        if (count <= 1) {  // remove last feed item - show empty stub
                            onClearState(ViewState.CLEAR.MODE_EMPTY_DATA)
                        } else {  // remove not last feed item
                            feedAdapter.remove { it.id == (newState.residual as BLOCK_PROFILE).profileId }
                        }
                    }
                    is NO_IMAGES_IN_PROFILE -> {
                        Dialogs.showTextDialog(activity,
                            descriptionResId = OriginR_string.feed_explore_dialog_no_user_photo_description,
                            positiveBtnLabelResId = OriginR_string.button_add_photo,
                            negativeBtnLabelResId = OriginR_string.button_later,
                            positiveListener = { _, _ -> navigate(this@FeedFragment, path="/main?tab=${NavigateFrom.MAIN_TAB_PROFILE}&tabPayload=${Payload.PAYLOAD_PROFILE_REQUEST_ADD_IMAGE}") })
                        swipe_refresh_layout.isRefreshing = false
                    }
                }
            }
            is ViewState.IDLE -> onIdleState()
            is ViewState.ERROR -> newState.e.handleOnView(this, ::onErrorState)
        }
    }

    private fun onClearState(mode: Int) {
        fun showEmptyStub(input: EmptyFragment.Companion.Input? = null) {
            fl_empty_container.changeVisibility(isVisible = true)
            val emptyFragment = EmptyFragment.newInstance(input)
            childFragmentManager
                .beginTransaction()
                .replace(R.id.fl_empty_container, emptyFragment, EmptyFragment.TAG)
                .commitNowAllowingStateLoss()
        }

        feedAdapter.clear()  // on MODE_DEFAULT - just clear adapter items
        vm.onClearScreen()
        getEmptyStateInput(mode)?.let {
            onIdleState()
            showEmptyStub(input = it)
        }
    }

    private fun onIdleState() {
        fl_empty_container.changeVisibility(isVisible = false, soft = true)
        swipe_refresh_layout.isRefreshing = false
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        feedAdapter = createFeedAdapter().apply {
            onBeforeLikeListener = { vm.onBeforeLike() }
            settingsClickListener = { model: FeedItemVO, position: Int, positionOfImage: Int ->
                val image = model.images[positionOfImage]
                scrollToTopOfItemAtPositionAndPost(position).post {
                    showScrollFab(isVisible = false)
                    notifyItemChanged(position, FeedViewHolderHideControls)
                }
                communicator(ILmmFragment::class.java)?.showTabs(isVisible = false)
                navigate(this@FeedFragment, path = "/block_dialog?position=$position&profileId=${model.id}&imageId=${image.id}&excludedReasons=10,50,70", rc = RequestCode.RC_BLOCK_DIALOG)
            }
        }
        offsetScrollStrats = getOffsetScrollStrategies()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RequestCode.RC_BLOCK_DIALOG -> {
                if (data == null) {
                    // TODO: crashes on app recreate, save bundle lack of 'data'
                    val e = NullPointerException("No output from Block/Report dialog - this is an error!")
                    Timber.e(e); throw e
                }

                val position = data.extras!!.getString("position", "0").toInt()
                communicator(ILmmFragment::class.java)?.showTabs(isVisible = true)
                scrollToTopOfItemAtPosition(position, offset = AppRes.BUTTON_HEIGHT)
                showScrollFab(isVisible = true, restoreVisibility = true)

                if (resultCode == Activity.RESULT_OK) {
                    val imageId = data.extras!!.getString("imageId")!!
                    val profileId = data.extras!!.getString("profileId")!!

                    if (data.hasExtra(Extras.OUT_EXTRA_REPORT_REASON)) {
                        val reasonNumber = data.getIntExtra(Extras.OUT_EXTRA_REPORT_REASON, 0)
                        vm.onReport(profileId = profileId, imageId = imageId, reasonNumber = reasonNumber)
                    } else {
                        vm.onBlock(profileId = profileId, imageId = imageId)
                    }
                } else {
                    // on dialog dismiss = show controls back
                    getRecyclerView().post {
                        feedAdapter.notifyItemChanged(position, FeedViewHolderShowControls)
                    }
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        feedTrackingBus = TrackingBus(onSuccess = Consumer(vm::onViewVertical), onError = Consumer(Timber::e))
        imagesTrackingBus = TrackingBus(onSuccess = Consumer(vm::onViewHorizontal), onError = Consumer(Timber::e))
        feedAdapter.trackingBus = imagesTrackingBus
    }

    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv_items.apply {
            adapter = feedAdapter
            isNestedScrollingEnabled = false
            layoutManager = LinearLayoutManager(context)
            itemAnimator = FeedItemAnimator { rv_items.post { feedAdapter.notifyItemRangeChanged(it - 1, 2, FeedViewHolderShowControls) } }
            setHasFixedSize(true)
//            OverScrollDecoratorHelper.setUpOverScroll(this, OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL)
            addOnScrollListener(itemOffsetScrollListener)
            addOnScrollListener(topScrollListener)
            addOnScrollListener(visibilityTrackingScrollListener)
        }
        swipe_refresh_layout.apply {
//            setColorSchemeResources(*resources.getIntArray(R.array.swipe_refresh_colors))
            setProgressViewEndTarget(false, resources.getDimensionPixelSize(R.dimen.feed_swipe_refresh_layout_spinner_end_offset))
            refreshes().compose(clickDebounce()).subscribe {
                offsetScrollStrats = getOffsetScrollStrategies()
                vm.onRefresh()
            }
            swipes().compose(clickDebounce()).subscribe { vm.onStartRefresh() }
        }
        scroll_fab.clicks().compose(clickDebounce()).subscribe {
            showScrollFab(isVisible = false)
            scrollToTopOfItemAtPosition(position = 0)
        }
    }

    override fun onResume() {
        super.onResume()
        feedTrackingBus.subscribe()
        imagesTrackingBus.subscribe()
    }

    override fun onPause() {
        super.onPause()
        feedTrackingBus.unsubscribe()
        imagesTrackingBus.unsubscribe()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rv_items.apply {
            removeOnScrollListener(itemOffsetScrollListener)
            removeOnScrollListener(topScrollListener)
            removeOnScrollListener(visibilityTrackingScrollListener)
        }
    }

    // --------------------------------------------------------------------------------------------
    private var wasFabVisible: Boolean = false

    protected fun showScrollFab(isVisible: Boolean, restoreVisibility: Boolean = false) {
        val xIsVisible = if (restoreVisibility) {
            wasFabVisible = scroll_fab.isVisible()
            isVisible && wasFabVisible
        } else isVisible
        scroll_fab.changeVisibility(isVisible = xIsVisible)
    }

    // ------------------------------------------
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        rv_items?.scrollBy(0, -1)
    }

    /* Scroll listeners */
    // --------------------------------------------------------------------------------------------
    private val topScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(rv, dx, dy)
            rv.linearLayoutManager()?.let {
                if (dy > 0) {  // scroll list down - to see new items
                    if (scroll_fab.isVisible()) {
                        showScrollFab(isVisible = false)
                    }
                } else {  // scroll list up - to see previous items
                    val offset = rv.computeVerticalScrollOffset()
                    if (scroll_fab.isVisible()) {
                        if (offset <= 0) {
                            showScrollFab(isVisible = false)
                        }
                    } else {
                        if (offset > 0) {
                            showScrollFab(isVisible = true)
                        }
                    }
                }
            }
        }
    }

    // ------------------------------------------
    private lateinit var offsetScrollStrats: List<OffsetScrollStrategy>

    protected fun getStrategyByTag(tag: String): OffsetScrollStrategy? = offsetScrollStrats.find { it.tag == tag }

    protected open fun getOffsetScrollStrategies(): List<OffsetScrollStrategy> =
        listOf(OffsetScrollStrategy(type = OffsetScrollStrategy.Type.BOTTOM, deltaOffset = AppRes.FEED_ITEM_TABS_INDICATOR_BOTTOM, hide = FeedViewHolderHideTabsIndicatorOnScroll, show = FeedViewHolderShowTabsIndicatorOnScroll),
               OffsetScrollStrategy(type = OffsetScrollStrategy.Type.BOTTOM, deltaOffset = AppRes.FEED_ITEM_SETTINGS_BTN_BOTTOM, hide = FeedViewHolderHideSettingsBtnOnScroll, show = FeedViewHolderShowSettingsBtnOnScroll),
               OffsetScrollStrategy(type = OffsetScrollStrategy.Type.BOTTOM, deltaOffset = AppRes.FEED_ITEM_FOOTER_LABEL_BOTTOM, hide = FeedFooterViewHolderHideControls, show = FeedFooterViewHolderShowControls))

    private val itemOffsetScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
            fun processItemViewControlVisibility(position: Int, view: View, top: Int, bottom: Int) {
                offsetScrollStrats.forEach {
                    if (it.isEnabledForPosition(position)) {
                        view.post {
                            // avoid change rv during layout, leading to crash
                            when (it.type) {
                                OffsetScrollStrategy.Type.BOTTOM -> {
                                    if (bottom - view.top <= AppRes.FEED_ITEM_MID_BTN_BOTTOM + 4) {
                                        if (bottom - view.top < it.deltaOffset) {
                                            if (!it.isHiddenAtAndSync(position)) {
                                                feedAdapter.notifyItemChanged(position, it.hide)
                                            }
                                        } else {
                                            if (!it.isShownAtAndSync(position)) {
                                                feedAdapter.notifyItemChanged(position, it.show)
                                            }
                                        }
                                    }
                                }
                                OffsetScrollStrategy.Type.TOP -> {
                                    if (view.top - top <= AppRes.FEED_ITEM_MID_BTN_TOP + 4) {
                                        if (top - view.top >= it.deltaOffset) {
                                            if (!it.isHiddenAtAndSync(position)) {
                                                feedAdapter.notifyItemChanged(position, it.hide)
                                            }
                                        } else {
                                            if (!it.isShownAtAndSync(position)) {
                                                feedAdapter.notifyItemChanged(position, it.show)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            fun processItemView(position: Int, view: View?) {
                view?.let {
                    val bottom = rv_items.bottom - AppRes.MAIN_BOTTOM_BAR_HEIGHT
                    processItemViewControlVisibility(position, view, AppRes.LMM_TOP_TAB_BAR_HIDE_AREA_HEIGHT, bottom)
                }
            }

            fun trackScrollOffset(rv: RecyclerView) {
                rv.linearLayoutManager()?.let {
                    val from = it.findFirstVisibleItemPosition()
                    val to = it.findLastVisibleItemPosition()
                    for (i in from..to) processItemView(i, it.findViewByPosition(i))
                }
            }

            super.onScrolled(rv, dx, dy)
            trackScrollOffset(rv)
        }
    }

    // ------------------------------------------
    private val visibilityTrackingScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
            fun trackVisibility(rv: RecyclerView) {
                rv.linearLayoutManager()?.let {
                    val from = it.findFirstVisibleItemPosition()
                    val to = it.findLastVisibleItemPosition()
                    val items = feedAdapter.getItemsExposed(from = from, to = to)
                    // use 0th image, because cannot access currently visible image on feed item, see [FeedViewModel::onViewVertical] for more info
                    var range = EqualRange(from = from, to = to,
                        items = items.map {
                            val image = if (it.isRealModel && it.images.isNotEmpty()) it.images[0] else EmptyImage
                            ProfileImageVO(profileId = it.id, image = image)
                        })
                    range = range.takeIf { feedAdapter.withHeader() }
                        ?.takeIf { from == 0 }
                        ?.let { it.dropItems(n = 1) }  // exclude header item from visibility tracking
                            ?: range  // no header item within the visible range
                    range = range.takeIf { feedAdapter.withFooter() }
                        ?.takeIf { to == feedAdapter.footerPosition() }
                        ?.let { it.dropLastItems(n = 1) }  // exclude footer item from visibility tracking
                            ?: range  // no footer item within the visible range
                    Timber.v("Visible feed items [${range.size}] [${range.from}, ${range.to}]: $range")
                    feedTrackingBus.postViewEvent(range)
                }
            }

            super.onScrolled(rv, dx, dy)
            trackVisibility(rv)
        }
    }
}
