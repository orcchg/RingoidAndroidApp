package com.ringoid.origin.feed.view.lmm

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.log.SentryUtil
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.feed.GetLmmUseCase
import com.ringoid.domain.model.feed.Lmm
import com.ringoid.origin.utils.ScreenHelper
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import javax.inject.Inject

class LmmViewModel @Inject constructor(val getLmmUseCase: GetLmmUseCase, app: Application)
    : BaseViewModel(app) {

    val badgeLikes by lazy { MutableLiveData<Boolean>() }
    val badgeMatches by lazy { MutableLiveData<Boolean>() }
    val badgeMessenger by lazy { MutableLiveData<Boolean>() }
    val listScrolls by lazy { MutableLiveData<Int>() }
    var cachedLmm: Lmm? = null
        private set

    init {
        getLmmUseCase.repository.badgeLikes
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({ badgeLikes.value = it }, Timber::e)

        getLmmUseCase.repository.badgeMatches
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({ badgeMatches.value = it }, Timber::e)

        getLmmUseCase.repository.badgeMessenger
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({ badgeMessenger.value = it }, Timber::e)
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onFreshStart() {
        super.onFreshStart()
        getLmm()
    }

    // --------------------------------------------------------------------------------------------
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventRefreshOnProfile(event: BusEvent.RefreshOnProfile) {
        Timber.d("Received bus event: $event")
        SentryUtil.breadcrumb("Bus Event", "event" to "$event")
        // refresh on Profile screen leads Lmm screen to refresh
        getLmm()
    }

    private fun getLmm() {
        val params = Params().put(ScreenHelper.getLargestPossibleImageResolution(context))
        getLmmUseCase.source(params = params)
            .autoDisposable(this)
            .subscribe({ cachedLmm = it }, Timber::e)
    }

    // --------------------------------------------------------------------------------------------
    fun onTabReselect() {
        actionObjectPool.trigger()
        listScrolls.value = 0  // scroll to top position
    }
}
