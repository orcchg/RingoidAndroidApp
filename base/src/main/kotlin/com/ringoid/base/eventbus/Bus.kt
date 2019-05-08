package com.ringoid.base.eventbus

import com.ringoid.domain.debug.DebugLogUtil
import org.greenrobot.eventbus.EventBus
import timber.log.Timber

object Bus {

    fun post(event: Any) {
        Timber.d("Post bus event: $event")
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

    object Stub : BusEvent()
    object NoImagesOnProfile : BusEvent()
    object RefreshOnExplore : BusEvent()
    object RefreshOnLmm : BusEvent()
    object RefreshOnProfile : BusEvent()
    data class ReOpenApp(val minutesElapsed: Long): BusEvent()
}
