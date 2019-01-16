package com.ringoid.domain.model.actions

import com.google.gson.annotations.SerializedName
import com.ringoid.domain.action_storage.CountFromLast
import com.ringoid.domain.action_storage.DelayFromLast

class LikeActionObject(
    @SerializedName(COLUMN_LIKE_COUNT) val likeCount: Int = 1,
    actionTime: Long, actionType: String, sourceFeed: String, targetImageId: String, targetUserId: String)
    : ActionObject(actionTime = actionTime, actionType = actionType, sourceFeed = sourceFeed,
                   targetImageId = targetImageId, targetUserId = targetUserId,
                   triggerStrategies = listOf(CountFromLast(), DelayFromLast())) {

    companion object {
        const val COLUMN_LIKE_COUNT = "likeCount"
    }
}
