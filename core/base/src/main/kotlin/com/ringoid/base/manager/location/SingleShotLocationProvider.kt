package com.ringoid.base.manager.location

import android.content.Context
import android.location.*
import android.os.Bundle
import android.os.HandlerThread
import com.ringoid.base.manager.location.LocationUtils.LocationManager_FUSED_PROVIDER
import com.ringoid.domain.action_storage.IActionObjectPool
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.manager.ISharedPrefsManager
import com.ringoid.domain.misc.GpsLocation
import com.ringoid.domain.model.actions.LocationActionObject
import io.reactivex.Single
import io.reactivex.SingleTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SingleShotLocationProvider @Inject constructor(
    private val context: Context, private val spm: ISharedPrefsManager,
    private val actionObjectPool: IActionObjectPool)
    : ILocationProvider {

    private val backgroundLooper = HandlerThread("LocationGetThread")

    /**
     * Get saved location and obtain location from provider eagerly.
     * If saved location is absent - fallback to get location from any available provider.
     */
    override fun location(): Single<GpsLocation> =
        spm.getLocation()
            ?.let { Single.just(it) }
            ?.doOnSubscribe {
                getLocation()  // this will silently update location in a cache, if changed significantly
                    .doOnSubscribe { DebugLogUtil.d("Location: get from cache, update eagerly") }
                    .subscribeOn(AndroidSchedulers.from(backgroundLooper.looper, true))
                    .subscribe({}, Timber::e)  // obtain location eagerly
            }
            ?: run {
                DebugLogUtil.d("Location: no location has found in cache")
                getLocation()
            }

    /**
     * Get location from any available provider.
     */
    private fun getLocation(): Single<GpsLocation> =
        getLocationImpl()
            .doOnSubscribe {
                if (!backgroundLooper.isAlive) {
                    DebugLogUtil.d("Location: start background looper thread")
                    backgroundLooper.start()
                }
            }
            .compareAndSaveLocation()

    @SuppressWarnings("MissingPermission")
    private fun getLocationImpl(): Single<GpsLocation> =
        (context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager)
            ?.let { locationManager ->
                getLocationProvider(locationManager)  // get available location provider
                    .also { DebugLogUtil.v("Location: using provider '$it'") }
                    ?.let { provider ->
                        try {
                            locationManager.getLastKnownLocation(provider)
                                ?.also { DebugLogUtil.v("Location: last known location for provider '$provider' is: $it") }
                                ?.let { Single.just(GpsLocation.from(it)) }
                                ?: requestLocation(provider)  // no last location found in cache - request for location
                        } catch (e: Throwable) {
                            DebugLogUtil.e(e, "Location: failed get last known location")
                            Single.error(e)
                        }
                    }
                    ?: Single.error(LocationServiceUnavailableException("any", status = -4))
            } ?: Single.error(NullPointerException("No location service available"))

    /**
     * Get location from provider that corresponds to given [precision].
     */
    private fun getLocation(precision: LocationPrecision): Single<GpsLocation> =
        getLocationImpl(precision).compareAndSaveLocation()

    @SuppressWarnings("MissingPermission")
    private fun getLocationImpl(precision: LocationPrecision): Single<GpsLocation> =
        (context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager)
            ?.let { locationManager ->
                getLocationProviderForPrecision(precision)
                    .also { DebugLogUtil.v("Location: using provider '$it' for precision '$precision'") }
                    .let {
                        try {
                            locationManager.getLastKnownLocation(it)
                                ?.also { DebugLogUtil.v("Location: last known location for precision '$precision' is: $it") }
                                ?.let { Single.just(GpsLocation.from(it)) }
                                ?: requestLocation(precision)  // no last location found in cache - request for location
                        } catch (e: Throwable) {
                            DebugLogUtil.e(e, "Location: failed get last known location for precision: $precision")
                            Single.error(e)
                        }
                    }
            } ?: Single.error(NullPointerException("No location service available"))

    // ------------------------------------------
    /**
     * Requests for single location update. Make sure permissions are granted and geoIP / GPS
     * services are enabled, depending on [precision].
     */
    @SuppressWarnings("MissingPermission")
    private fun requestLocation(precision: LocationPrecision): Single<GpsLocation> =
        (context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager)
            ?.let { locationManager ->
                getLocationCriteriaByPrecision(locationManager, precision)
                    ?.also { DebugLogUtil.v("Location: request for location for precision '$precision' and criteria: $it") }
                    ?.let { criteria -> requestLocationWithManagerAndCriteria(locationManager, criteria) }
                    ?: run { Single.error<GpsLocation>(LocationServiceUnavailableException(getLocationProviderForPrecision(precision), status = -3)) }
            } ?: Single.error(NullPointerException("No location service available"))

    private fun requestLocation(provider: String): Single<GpsLocation> =
        (context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager)
            ?.let { locationManager ->
                getLocationCriteriaByProvider(provider)
                    ?.also { DebugLogUtil.v("Location: request for location with provider '$provider' and criteria: $it") }
                    ?.let { criteria -> requestLocationWithManagerAndCriteria(locationManager, criteria) }
                    ?: run { Single.error<GpsLocation>(LocationServiceUnavailableException(provider, status = -2)) }
            } ?: Single.error(NullPointerException("No location service available"))

    // --------------------------------------------------------------------------------------------
    private fun getLocationCriteriaByPrecision(locationManager: LocationManager, precision: LocationPrecision): Criteria? =
        when (precision) {
            LocationPrecision.ANY ->
                locationManager
                    .takeIf { it.isProviderEnabled(LocationManager_FUSED_PROVIDER) }
                    ?.let { Criteria().apply { accuracy = Criteria.ACCURACY_COARSE } }
            LocationPrecision.COARSE ->
                locationManager
                    .takeIf { it.isProviderEnabled(LocationManager.NETWORK_PROVIDER) }
                    ?.let { Criteria().apply { accuracy = Criteria.ACCURACY_COARSE } }
            // --------------------------
            LocationPrecision.FINE -> {
                locationManager
                    .takeIf { it.isProviderEnabled(LocationManager.GPS_PROVIDER) }
                    ?.let { Criteria().apply { accuracy = Criteria.ACCURACY_FINE } }
            }
        }

    private fun getLocationCriteriaByProvider(provider: String): Criteria? =
        when (provider) {
            LocationManager_FUSED_PROVIDER,
            LocationManager.NETWORK_PROVIDER -> Criteria().apply { accuracy = Criteria.ACCURACY_COARSE }
            LocationManager.GPS_PROVIDER -> Criteria().apply { accuracy = Criteria.ACCURACY_FINE }
            else -> null
        }

    private fun getLocationProvider(locationManager: LocationManager): String? {
        for (provider in LocationProviderType.values) {
            if (provider.isEnabled(locationManager)) {
                return provider.provider  // pick first available location provider, order matters
            }
        }
        return null
    }

    private fun getLocationProviderForPrecision(precision: LocationPrecision): String =
        when (precision) {
            LocationPrecision.ANY -> LocationManager_FUSED_PROVIDER
            LocationPrecision.COARSE -> LocationManager.NETWORK_PROVIDER
            LocationPrecision.FINE -> LocationManager.GPS_PROVIDER
        }

    @SuppressWarnings("MissingPermission")
    private fun requestLocationWithManagerAndCriteria(locationManager: LocationManager, criteria: Criteria): Single<GpsLocation> =
        Single.create { emitter ->
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    DebugLogUtil.v("Location: obtained (${location.latitude}, ${location.longitude})")
                    emitter.onSuccess(GpsLocation.from(location))
                }

                override fun onStatusChanged(provider: String, status: Int, extras: Bundle?) {
                    DebugLogUtil.v("Location: status has changed to $status for provider '$provider': $extras".trim())
                    if (status != LocationProvider.AVAILABLE) {
                        emitter.onError(LocationServiceUnavailableException(provider, status))
                    }
                }

                override fun onProviderDisabled(provider: String) {
                    DebugLogUtil.v("Location service has been disabled by user, provider: '$provider'")
                    emitter.onError(LocationServiceUnavailableException(provider, status = -1))
                }

                override fun onProviderEnabled(provider: String) {
                    DebugLogUtil.v("Location service has been enabled by user, provider: '$provider'")
                }
            }
            emitter.setCancellable { locationManager.removeUpdates(listener) }
            locationManager.requestSingleUpdate(criteria, listener, null)
        }

    // ------------------------------------------
    private fun Single<GpsLocation>.compareAndSaveLocation(): Single<GpsLocation> =
        compose(compareAndSaveLocationImpl())

    private fun compareAndSaveLocationImpl(): SingleTransformer<GpsLocation, GpsLocation> =
        SingleTransformer {
            it.map { location ->
                val changed = LocationUtils.diffLocation(oldLocation = spm.getLocation(), newLocation = location)
                val aobj = if (changed) {
                    DebugLogUtil.d("Location has changed enough, update saved location")
                    LocationActionObject(location.latitude, location.longitude)
                } else {
                    DebugLogUtil.v("Location has not changed")
                    null
                }
                location to aobj
            }
            .flatMap { (location, aobj) ->
                aobj?.let { aObj ->
                    actionObjectPool
                        .commitNow(aObj)
                        .subscribeOn(Schedulers.io())
                        .map { location }
                }
                ?: Single.just(location)
            }
            .doOnSuccess { location -> spm.saveLocation(location) }
            .observeOn(AndroidSchedulers.mainThread())
        }
}
