package com.ringoid.base.eventbus

import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.debug.DebugOnly
import org.greenrobot.eventbus.EventBus

object Bus {

    fun post(event: Any) {
        DebugLogUtil.b("Bus: $event")
        EventBus.getDefault().post(event)
    }

    fun subscribeOnBusEvents(subscriber: Any) {
        if (!EventBus.getDefault().isRegistered(subscriber)) {
            EventBus.getDefault().register(subscriber)
        }
    }

    fun unsubscribeFromBusEvents(subscriber: Any) {
        if (EventBus.getDefault().isRegistered(subscriber)) {
            EventBus.getDefault().unregister(subscriber)
        }
    }

    fun isSubscribed(subscriber: Any): Boolean = EventBus.getDefault().isRegistered(subscriber)
}

sealed class BusEvent {

    override fun toString(): String = javaClass.simpleName

    @DebugOnly
    object CloseDebugView : BusEvent()

    object Stub : BusEvent()
    object NoImagesOnProfile : BusEvent()
    object RefreshOnExplore : BusEvent()
    object RefreshOnLmm : BusEvent()
    object RefreshOnProfile : BusEvent()
    object RefreshOnPush : BusEvent()
    object ReOpenApp: BusEvent()
    data class ReStartWithTime(val msElapsed: Long): BusEvent()

    data class PushNewLike(val peerId: String) : BusEvent()
    data class PushNewMatch(val peerId: String) : BusEvent()
    data class PushNewMessage(val peerId: String) : BusEvent()
}
