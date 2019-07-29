package com.ringoid.origin.view.main

import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.push.PushNotificationData
import com.ringoid.origin.navigation.NavigateFrom

enum class NavTab(val tabName: String) {
    EXPLORE(NavigateFrom.MAIN_TAB_EXPLORE),
    LIKES(NavigateFrom.MAIN_TAB_LIKES),
    MESSAGES(NavigateFrom.MAIN_TAB_MESSAGES),
    PROFILE(NavigateFrom.MAIN_TAB_PROFILE);  // order matters for BottomBar

    companion object {
        val values: Array<NavTab> = values()

        fun get(index: Int): NavTab = values[index]  // here order matters

        fun from(tabName: String): NavTab =
            when (tabName) {
                NavigateFrom.MAIN_TAB_EXPLORE -> EXPLORE
                NavigateFrom.MAIN_TAB_LIKES -> LIKES
                NavigateFrom.MAIN_TAB_MESSAGES -> MESSAGES
                NavigateFrom.MAIN_TAB_PROFILE -> PROFILE
                else -> throw IllegalArgumentException("Unknown tab name: $tabName")
            }
    }
}

enum class LcNavTab(val feedName: String) {
    LIKES(DomainUtil.SOURCE_FEED_LIKES),
    MESSAGES(DomainUtil.SOURCE_FEED_MESSAGES);  // order matters

    companion object {
        val values: Array<LcNavTab> = values()

        fun get(index: Int): LcNavTab? = values[index]  // here order matters

        fun from(sourceFeed: String?): LcNavTab? =
            when (sourceFeed) {
                DomainUtil.SOURCE_FEED_LIKES -> LIKES
                DomainUtil.SOURCE_FEED_MATCHES,
                DomainUtil.SOURCE_FEED_MESSAGES -> MESSAGES
                else -> null
            }

        fun fromPushType(pushType: String): LcNavTab? =
            when (pushType) {
                PushNotificationData.TYPE_LIKE -> LIKES
                PushNotificationData.TYPE_MATCH,
                PushNotificationData.TYPE_MESSAGE -> MESSAGES
                else -> null
            }
    }
}

@Deprecated("LMM -> LC")
enum class LmmNavTab(val feedName: String) {
    LIKES(DomainUtil.SOURCE_FEED_LIKES),
    MATCHES(DomainUtil.SOURCE_FEED_MATCHES),
    MESSAGES(DomainUtil.SOURCE_FEED_MESSAGES);  // order matters

    fun page(): Int = ordinal

    fun slice(): LcNavTab =
        when (this) {
            LIKES -> LcNavTab.LIKES
            else -> LcNavTab.MESSAGES
        }

    companion object {
        val values: Array<LmmNavTab> = values()

        fun get(index: Int): LmmNavTab? = values[index]  // here order matters

        fun from(sourceFeed: String?): LmmNavTab? =
            when (sourceFeed) {
                DomainUtil.SOURCE_FEED_LIKES -> LIKES
                DomainUtil.SOURCE_FEED_MATCHES -> MATCHES
                DomainUtil.SOURCE_FEED_MESSAGES -> MESSAGES
                else -> null
            }

        fun fromPushType(pushType: String): LmmNavTab? =
            when (pushType) {
                PushNotificationData.TYPE_LIKE -> LIKES
                PushNotificationData.TYPE_MATCH -> MATCHES
                PushNotificationData.TYPE_MESSAGE -> MESSAGES
                else -> null
            }
    }
}
