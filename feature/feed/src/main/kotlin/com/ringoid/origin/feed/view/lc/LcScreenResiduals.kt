package com.ringoid.origin.feed.view.lc

import com.ringoid.base.view.Residual

data class FeedCounts(val show: Int, val hidden: Int)

data class SEEN_ALL_FEED(val sourceFeed: Int) : Residual() {
    companion object {
        const val FEED_LIKES = 0
        const val FEED_MATCHES = 1
        const val FEED_MESSENGER = 2
    }
}
