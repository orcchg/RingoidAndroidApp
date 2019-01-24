package com.ringoid.origin.usersettings.view.debug

import android.os.Bundle
import android.view.View
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.view.BaseFragment
import com.ringoid.base.view.ViewState
import com.ringoid.origin.view.dialog.Dialogs
import com.ringoid.usersettings.R
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.clickDebounce
import kotlinx.android.synthetic.main.fragment_debug.*

class DebugFragment : BaseFragment<DebugViewModel>() {

    companion object {
        const val TAG = "DebugFragment_tag"

        fun newInstance(): DebugFragment = DebugFragment()
    }

    override fun getVmClass(): Class<DebugViewModel> = DebugViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_debug

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        fun onIdleState() {
            pb_debug.changeVisibility(isVisible = false)
        }

        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.IDLE -> onIdleState()
            is ViewState.LOADING -> pb_debug.changeVisibility(isVisible = true)
            is ViewState.ERROR -> {
                // TODO: analyze: newState.e
                Dialogs.errorDialog(this, newState.e)
                onIdleState()
            }
        }
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        item_error_http.clicks().compose(clickDebounce()).subscribe {  }
        item_error_token.clicks().compose(clickDebounce()).subscribe { vm.requestWithInvalidAccessToken() }
        item_error_token_expired.clicks().compose(clickDebounce()).subscribe { vm.requestWithExpiredAccessToken() }
        item_error_app_version.clicks().compose(clickDebounce()).subscribe { vm.requestWithStaledAppVersion() }
        item_error_server.clicks().compose(clickDebounce()).subscribe { vm.requestWithServerError() }
        item_error_request_params.clicks().compose(clickDebounce()).subscribe { vm.requestWithWrongParams() }
        item_error_timeout.clicks().compose(clickDebounce()).subscribe {  }
    }
}
