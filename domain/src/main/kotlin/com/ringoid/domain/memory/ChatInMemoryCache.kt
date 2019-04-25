package com.ringoid.domain.memory

import com.ringoid.domain.BuildConfig
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.manager.ISharedPrefsManager
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

object ChatInMemoryCache {

    private val BOTTOM_CHAT_POSITION: Pair<Int, Int> = 0 to 0
    private val SP_KEY_CHAT_CACHE = "sp_key_chat_cache"

    private val chatInputMessage = mutableMapOf<String, CharSequence>()
    private val chatPeerMessagesCount = mutableMapOf<String, Int>()
    private val chatScrollPosition = mutableMapOf<String, Pair<Int, Int>>()

    // ------------------------------------------
    fun getInputMessage(profileId: String): CharSequence? = chatInputMessage[profileId]

    fun setInputMessage(profileId: String, text: CharSequence) {
        if (hasProfile(profileId)) {
            chatInputMessage[profileId] = text
        }
    }

    // ------------------------------------------
    fun getPeerMessagesCount(profileId: String): Int = chatPeerMessagesCount[profileId] ?: 0

    fun setPeerMessagesCount(profileId: String, count: Int) {
        if (hasProfile(profileId)) {
            chatPeerMessagesCount[profileId] = count
            dropPositionForProfile(profileId)
        }
    }

    fun setPeerMessagesCountIfChanged(profileId: String, count: Int) {
        if (!hasProfile(profileId) || getPeerMessagesCount(profileId) == count) {
            return
        }

        chatPeerMessagesCount[profileId] = count
        dropPositionForProfile(profileId)
    }

    // ------------------------------------------
    fun hasProfile(profileId: String): Boolean = chatScrollPosition.containsKey(profileId)

    fun getProfilePosition(profileId: String): Pair<Int, Int> =
            chatScrollPosition[profileId] ?: BOTTOM_CHAT_POSITION

    fun addProfileIfNotExists(profileId: String): Boolean {
        val result = hasProfile(profileId)
        if (!result) {
            chatScrollPosition[profileId] = BOTTOM_CHAT_POSITION
        }
        return result
    }

    fun addProfileWithPosition(profileId: String, position: Pair<Int, Int>): Boolean {
        val result = hasProfile(profileId)
        chatScrollPosition[profileId] = position
        return result
    }

    fun addProfileWithPositionIfNotExists(profileId: String, position: Pair<Int, Int>): Boolean {
        val result = hasProfile(profileId)
        if (!result) {
            chatScrollPosition[profileId] = position
        }
        return result
    }

    fun dropPositionForProfile(profileId: String) {
        if (hasProfile(profileId)) {
            chatScrollPosition[profileId] = BOTTOM_CHAT_POSITION
        }
    }

    // ------------------------------------------
    fun clear() {
        chatInputMessage.clear()
        chatPeerMessagesCount.clear()
        chatScrollPosition.clear()
    }

    fun persist(spm: ISharedPrefsManager) {
        Timber.v("Saving cached chat data... ${if (BuildConfig.DEBUG) toJson() else ""}")
        spm.saveByKey(SP_KEY_CHAT_CACHE, toJson())
    }

    /**
     * Sample json:
     *
     *  {
     *    "chatInputMessage":[{"c18bc052e88cc41672dba3fc1c1e3dbbb9e6d46a":""}],
     *    "chatPeerMessagesCount":[{"c18bc052e88cc41672dba3fc1c1e3dbbb9e6d46a":31}],
     *    "chatScrollPosition":[{"c18bc052e88cc41672dba3fc1c1e3dbbb9e6d46a":[0,96]}]
     *  }
     */
    fun restore(spm: ISharedPrefsManager) {
        spm.getByKey(SP_KEY_CHAT_CACHE)?.let {
            Timber.v("Restored cached chat data: $it")
            try {
                val json = JSONObject(it.replace('"', '\"'))
                json.optJSONArray("chatInputMessage")?.let {
                    val length = it.length()
                    for (i in 0 until length) {
                        it.optJSONObject(i)?.let { json ->
                            json.keys().forEach { key -> chatInputMessage[key] = json.optString(key) ?: "" }
                        }
                    }
                }
                json.optJSONArray("chatPeerMessagesCount")?.let {
                    val length = it.length()
                    for (i in 0 until length) {
                        it.optJSONObject(i)?.let { json ->
                            json.keys().forEach { key -> chatPeerMessagesCount[key] = json.optInt(key) }
                        }
                    }
                }
                json.optJSONArray("chatScrollPosition")?.let {
                    val length = it.length()
                    for (i in 0 until length) {
                        it.optJSONObject(i)?.let { json ->
                            json.keys().forEach { key ->
                                json.optJSONArray(key)?.let { chatScrollPosition[key] = it.optInt(0) to it.optInt(1) }
                            }
                        }
                    }
                }
                if (BuildConfig.DEBUG) {
                    Timber.v("Parsed restored cached chat dara: ${toJson()}")
                    if (toJson() != it) Timber.e("Parsing was incorrect!")
                }
            } catch (e: JSONException) {
                DebugLogUtil.e(e, "Failed to parse json: $it")
            }
        }
    }

    private fun toJson(): String =
        "{\"chatInputMessage\":${chatInputMessage.entries.joinToString(",", "[", "]", transform = { "{\"${it.key}\":\"${it.value}\"}" })}," +
         "\"chatPeerMessagesCount\":${chatPeerMessagesCount.entries.joinToString(",", "[", "]", transform = { "{\"${it.key}\":${it.value}}" })}," +
         "\"chatScrollPosition\":${chatScrollPosition.entries.joinToString(",", "[", "]", transform = { "{\"${it.key}\":[${it.value.first},${it.value.second}]}" })}}"
}
