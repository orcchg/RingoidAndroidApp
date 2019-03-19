package com.ringoid.origin.feed.view.lmm

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.observe
import com.ringoid.base.view.BaseFragment
import com.ringoid.origin.AppRes
import com.ringoid.origin.feed.R
import com.ringoid.utility.changeTypeface
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.clickDebounce
import kotlinx.android.synthetic.main.fragment_lmm.*

class LmmFragment : BaseFragment<LmmViewModel>(), ILmmFragment {

    companion object {
        private const val BUNDLE_KEY_CURRENT_PAGE = "bundle_key_current_page"

        fun newInstance(): LmmFragment = LmmFragment()
    }

    private lateinit var lmmPagesAdapter: LmmPagerAdapter

    override fun getVmClass(): Class<LmmViewModel> = LmmViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_lmm

    // --------------------------------------------------------------------------------------------
    // TODO: save that fields onSaveInstanceState() for later restore
    private var badge_likes_visibilityPrev: Boolean = false
    private var badge_matches_visibilityPrev: Boolean = false
    private var badge_messages_visibilityPrev: Boolean = false

    override fun accessViewModel(): LmmViewModel = vm

    private fun showBadgeOnLikes(isVisible: Boolean) {
        badge_likes_visibilityPrev = isVisible
        btn_tab_likes.showBadge(isVisible)
    }

    private fun showBadgeOnMatches(isVisible: Boolean) {
        badge_matches_visibilityPrev = isVisible
        btn_tab_matches.showBadge(isVisible)
    }

    private fun showBadgeOnMessenger(isVisible: Boolean) {
        badge_messages_visibilityPrev = isVisible
        btn_tab_messenger.showBadge(isVisible)
    }

    override fun showTabs(isVisible: Boolean) {
        if (isVisible) {
            btn_tab_likes.showBadge(badge_likes_visibilityPrev)
            btn_tab_matches.showBadge(badge_matches_visibilityPrev)
            btn_tab_messenger.showBadge(badge_messages_visibilityPrev)
        } else {
            btn_tab_likes.showBadge(false)
            btn_tab_matches.showBadge(false)
            btn_tab_messenger.showBadge(false)
        }
        btn_tab_likes.changeVisibility(isVisible)
        btn_tab_matches.changeVisibility(isVisible)
        btn_tab_messenger.changeVisibility(isVisible)
        tab_delim1.changeVisibility(isVisible)
        tab_delim2.changeVisibility(isVisible)
    }

    // ------------------------------------------
    override fun onBeforeTabSelect() {
        super.onBeforeTabSelect()
        setCurrentPageVisibleHint(false)
    }

    override fun onTabTransaction(payload: String?) {
        super.onTabTransaction(payload)
        setCurrentPageVisibleHint(true)
    }

    override fun onTabReselect() {
        super.onTabReselect()
        vp_pages?.let {
            val nextPage = it.currentItem + 1
            selectPage(nextPage.takeIf { it > 2 }?.let { 0 } ?: nextPage)
        }
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lmmPagesAdapter = LmmPagerAdapter(childFragmentManager)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(viewLifecycleOwner) {
            observe(vm.badgeLikes, ::showBadgeOnLikes)
            observe(vm.badgeMatches, ::showBadgeOnMatches)
            observe(vm.badgeMessenger, ::showBadgeOnMessenger)
        }

        val page = savedInstanceState?.getInt(BUNDLE_KEY_CURRENT_PAGE) ?: 2
        selectPage(position = page)  // open LikesYou at beginning
    }

    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vp_pages.apply {
            adapter = lmmPagesAdapter
            offscreenPageLimit = 3
        }
        btn_tab_likes.clicks().compose(clickDebounce()).subscribe { selectPage(2) }
        btn_tab_matches.clicks().compose(clickDebounce()).subscribe { selectPage(1) }
        btn_tab_messenger.clicks().compose(clickDebounce()).subscribe { selectPage(0) }
    }

    private fun selectPage(position: Int) {
        when (position) {
            0 -> {
                setPageVisibleHint(1, false)
                setPageVisibleHint(2, false)
                btn_tab_likes?.changeTypeface(textSize = AppRes.BUTTON_FLAT_TEXT_SIZE)
                btn_tab_matches?.changeTypeface(textSize = AppRes.BUTTON_FLAT_TEXT_SIZE)
                btn_tab_messenger?.changeTypeface(style = Typeface.BOLD, isSelected = true, textSize = AppRes.BUTTON_FLAT_INC_TEXT_SIZE)
            }
            1 -> {
                setPageVisibleHint(0, false)
                setPageVisibleHint(2, false)
                btn_tab_likes?.changeTypeface(textSize = AppRes.BUTTON_FLAT_TEXT_SIZE)
                btn_tab_matches?.changeTypeface(style = Typeface.BOLD, isSelected = true, textSize = AppRes.BUTTON_FLAT_INC_TEXT_SIZE)
                btn_tab_messenger?.changeTypeface(textSize = AppRes.BUTTON_FLAT_TEXT_SIZE)
            }
            2 -> {
                setPageVisibleHint(0, false)
                setPageVisibleHint(1, false)
                btn_tab_likes?.changeTypeface(style = Typeface.BOLD, isSelected = true, textSize = AppRes.BUTTON_FLAT_INC_TEXT_SIZE)
                btn_tab_matches?.changeTypeface(textSize = AppRes.BUTTON_FLAT_TEXT_SIZE)
                btn_tab_messenger?.changeTypeface(textSize = AppRes.BUTTON_FLAT_TEXT_SIZE)
            }
        }

        if (vp_pages?.currentItem == position) {
            // current position reselected
            vm.onTabReselect()
        }

        setPageVisibleHint(position, true)
        vp_pages?.setCurrentItem(position, false)
    }

    override fun onStart() {
        super.onStart()
        setCurrentPageVisibleHint(true)
    }

    override fun onStop() {
        super.onStop()
        setCurrentPageVisibleHint(false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(BUNDLE_KEY_CURRENT_PAGE, vp_pages?.currentItem ?: 2)
    }

    // ------------------------------------------
    private fun setPageVisibleHint(position: Int, hint: Boolean) {
        lmmPagesAdapter.accessItem(position)?.userVisibleHint = hint
    }

    private fun setCurrentPageVisibleHint(hint: Boolean) {
        vp_pages?.let { lmmPagesAdapter.accessItem(it.currentItem)?.userVisibleHint = hint }
    }
}
