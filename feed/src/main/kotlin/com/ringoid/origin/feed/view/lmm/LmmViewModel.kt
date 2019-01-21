package com.ringoid.origin.feed.view.lmm

import android.app.Application
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.feed.GetLmmUseCase
import com.ringoid.origin.ScreenHelper
import com.uber.autodispose.lifecycle.autoDisposable
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import javax.inject.Inject

class LmmViewModel @Inject constructor(
    val getLmmUseCase: GetLmmUseCase, app: Application) : BaseViewModel(app) {

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventRefreshOnProfile(event: BusEvent.RefreshOnProfile) {
        Timber.d("Received bus event: $event")
        // refresh on Profile screen leads Lmm screen to refresh
        getLmm()
    }

    private fun getLmm() {
        val params = Params().put(ScreenHelper.getLargestPossibleImageResolution(context))
        getLmmUseCase.source(params = params)
            .autoDisposable(this)
            .subscribe({ Timber.v("Lmm has been refreshed") }, Timber::e)
    }
}
