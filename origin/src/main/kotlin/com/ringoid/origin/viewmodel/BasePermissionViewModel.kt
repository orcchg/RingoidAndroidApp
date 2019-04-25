package com.ringoid.origin.viewmodel

import android.app.Application
import com.ringoid.base.manager.location.ILocationProvider
import com.ringoid.base.manager.location.LocationPrecision
import com.ringoid.base.manager.location.LocationServiceUnavailableException
import com.ringoid.base.manager.location.LocationUtils
import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.misc.GpsLocation
import com.ringoid.domain.model.actions.LocationActionObject
import com.ringoid.origin.view.base.ASK_TO_ENABLE_LOCATION_SERVICE
import io.reactivex.Maybe
import timber.log.Timber
import javax.inject.Inject

abstract class BasePermissionViewModel(app: Application) : BaseViewModel(app) {

    @Inject lateinit var locationProvider: ILocationProvider

    protected open fun onLocationReceived() {}

    /* Permission */
    // --------------------------------------------------------------------------------------------
    fun onLocationPermissionGranted() {
        fun onLocationChanged(location: GpsLocation) {
            LocationUtils.onLocationChanged(location, spm) {
                val aobj = LocationActionObject(location.latitude, location.longitude)
                actionObjectPool.put(aobj)
            }
            onLocationReceived()
        }

        locationProvider
            .getLocation(LocationPrecision.COARSE)
            .filter { it.latitude != 0.0 && it.longitude != 0.0 }
            .onErrorResumeNext { e: Throwable ->
                when (e) {
                    is LocationServiceUnavailableException -> spm.getLocation()?.let { Maybe.just(it) } ?: Maybe.error(e)
                    else -> Maybe.error(e)
                }
            }
            .subscribe(::onLocationChanged) {
                Timber.e(it)
                when (it) {
                    is LocationServiceUnavailableException -> viewState.value = ViewState.DONE(ASK_TO_ENABLE_LOCATION_SERVICE)
                }
            }
    }
}
