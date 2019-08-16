package com.ringoid.origin.error

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.ringoid.domain.exception.InvalidAccessTokenApiException
import com.ringoid.domain.exception.NetworkUnexpected
import com.ringoid.domain.exception.OldAppVersionApiException
import com.ringoid.origin.navigation.blockingErrorScreen
import com.ringoid.origin.navigation.logout
import com.ringoid.origin.navigation.noConnection
import com.ringoid.origin.view.dialog.Dialogs
import com.ringoid.utility.delay

fun Throwable.handleOnView(activity: FragmentActivity, onErrorState: () -> Unit = {}) {
    fun errorState(e: Throwable) {
        Dialogs.errorDialog(activity, e)
        onErrorState()  // handle error state on Screen  in screen-specific way
    }

    when (this) {
        is OldAppVersionApiException -> blockingErrorScreen(activity, path = "/old_version")
        is InvalidAccessTokenApiException -> logout(activity)
        is NetworkUnexpected -> {
            noConnection(activity)
            delay { onErrorState() }  // handle error state on Screen  in screen-specific way
        }
        else -> errorState(this)  // default error handling
    }
}

fun Throwable.handleOnView(fragment: Fragment, onErrorState: () -> Unit = {}) {
    fun errorState(e: Throwable) {
        Dialogs.errorDialog(fragment, e)
        onErrorState()  // handle error state on Screen  in screen-specific way
    }

    when (this) {
        is OldAppVersionApiException -> blockingErrorScreen(fragment, path = "/old_version")
        is InvalidAccessTokenApiException -> logout(fragment)
        is NetworkUnexpected -> {
            noConnection(fragment)
            delay { onErrorState() }  // handle error state on Screen  in screen-specific way
        }
        else -> errorState(this)  // default error handling
    }
}