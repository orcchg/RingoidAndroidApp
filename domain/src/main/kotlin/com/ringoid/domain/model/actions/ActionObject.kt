package com.ringoid.domain.model.actions

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.action_storage.TriggerStrategy
import com.ringoid.domain.model.IEssence

sealed class BaseActionObject

/**
 * {
 *   "sourceFeed":"new_faces",  // who_liked_me, matches, messages
 *   "actionType":"BLOCK",
 *   "targetUserId":"skdfkjhkjsdhf",
 *   "targetPhotoId":"sldfnlskdj",
 *   "actionTime":12342342354  // unix time
 * }
 */
open class ActionObject(
    @Expose @SerializedName(COLUMN_ACTION_TIME) val actionTime: Long = System.currentTimeMillis(),
    @Expose @SerializedName(COLUMN_ACTION_TYPE) val actionType: String,
    @Expose @SerializedName(COLUMN_SOURCE_FEED) val sourceFeed: String,
    @Expose @SerializedName(COLUMN_TARGET_IMAGE_ID) val targetImageId: String,
    @Expose @SerializedName(COLUMN_TARGET_USER_ID) val targetUserId: String,
    val triggerStrategies: List<TriggerStrategy> = emptyList())
    : BaseActionObject(), IEssence {

    companion object {
        const val COLUMN_ACTION_TIME = "actionTime"
        const val COLUMN_ACTION_TYPE = "actionType"
        const val COLUMN_SOURCE_FEED = "sourceFeed"
        const val COLUMN_TARGET_IMAGE_ID = "targetPhotoId"
        const val COLUMN_TARGET_USER_ID = "targetUserId"
    }

    override fun toString(): String = "${javaClass.simpleName}(actionTime=$actionTime, actionType='$actionType', sourceFeed='$sourceFeed', targetImageId='$targetImageId', targetUserId='$targetUserId', triggerStrategies=$triggerStrategies)"

    override fun toSentryPayload(): String = "[${javaClass.simpleName}]"
}
