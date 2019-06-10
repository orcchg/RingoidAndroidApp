package com.ringoid.origin.feed.view.lmm.base

import com.ringoid.origin.AppRes
import com.ringoid.origin.feed.adapter.base.*
import com.ringoid.origin.feed.misc.OffsetScrollStrategy

abstract class BaseMatchesFeedFragment<VM : BaseLmmFeedViewModel> : BaseLmmFeedFragment<VM>() {

    /* Scroll listeners */
    // --------------------------------------------------------------------------------------------
    override fun getOffsetScrollStrategies(): List<OffsetScrollStrategy> =
        mutableListOf<OffsetScrollStrategy>()
            .apply {
                addAll(super.getOffsetScrollStrategies())
                add(OffsetScrollStrategy(tag = "bias btn bottom", type = OffsetScrollStrategy.Type.BOTTOM, deltaOffset = AppRes.FEED_ITEM_BIAS_BTN_BOTTOM, hide = FeedViewHolderHideChatBtnOnScroll, show = FeedViewHolderShowChatBtnOnScroll))
                add(OffsetScrollStrategy(tag = "bias btn top", type = OffsetScrollStrategy.Type.TOP, deltaOffset = AppRes.FEED_ITEM_BIAS_BTN_TOP, hide = FeedViewHolderHideChatBtnOnScroll, show = FeedViewHolderShowChatBtnOnScroll))
            }
}
