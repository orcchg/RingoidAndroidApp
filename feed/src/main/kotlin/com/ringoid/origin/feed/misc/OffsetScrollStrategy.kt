package com.ringoid.origin.feed.misc

import com.ringoid.origin.feed.adapter.base.FeedViewHolderPayload

data class OffsetScrollStrategy(val tag: String? = null,
    val type: Type, val deltaOffset: Int, val hide: FeedViewHolderPayload, val show: FeedViewHolderPayload,
    private val hiddenAtPositions: MutableSet<Int> = mutableSetOf(),
    private val shownAtPositions: MutableSet<Int> = mutableSetOf(),
    private val enabledForPositions: MutableSet<Int> = mutableSetOf()) {

    enum class Type { TOP, BOTTOM }

    fun isHiddenAtAndSync(position: Int): Boolean =
        if (!hiddenAtPositions.contains(position)) {
            hiddenAtPositions.add(position)
            shownAtPositions.remove(position)
            false
        } else true

    fun isShownAtAndSync(position: Int): Boolean =
        if (!shownAtPositions.contains(position)) {
            shownAtPositions.add(position)
            hiddenAtPositions.remove(position)
            false
        } else true

    // ------------------------------------------
    fun isEnabledForPosition(position: Int): Boolean =
        tag?.let { enabledForPositions.contains(position) } ?: true

    fun enableForPosition(position: Int) {
        enabledForPositions.add(position)
    }

    fun disableForPosition(position: Int) {
        enabledForPositions.remove(position)
    }
}