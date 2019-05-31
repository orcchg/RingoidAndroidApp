package com.ringoid.origin.usersettings.view.profile

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.ringoid.base.view.BaseFragment
import com.ringoid.base.view.ViewState
import com.ringoid.origin.model.EducationProfileProperty
import com.ringoid.origin.model.IncomeProfileProperty
import com.ringoid.origin.model.PropertyProfileProperty
import com.ringoid.origin.model.TransportProfileProperty
import com.ringoid.origin.usersettings.OriginR_string
import com.ringoid.origin.usersettings.R
import com.ringoid.utility.changeVisibility
import kotlinx.android.synthetic.main.fragment_settings_profile.*
import kotlinx.android.synthetic.main.fragment_settings_push.*
import kotlinx.android.synthetic.main.fragment_settings_push.pb_loading
import kotlinx.android.synthetic.main.fragment_settings_push.toolbar

class SettingsProfileFragment : BaseFragment<SettingsProfileViewModel>() {

    companion object {
        internal const val TAG = "SettingsProfileFragment_tag"

        fun newInstance(): SettingsProfileFragment = SettingsProfileFragment()
    }

    override fun getVmClass(): Class<SettingsProfileViewModel> = SettingsProfileViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_settings_profile

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.IDLE -> pb_loading.changeVisibility(isVisible = false, soft = true)
            is ViewState.LOADING -> pb_loading.changeVisibility(isVisible = true)
        }
    }

    // --------------------------------------------------------------------------------------------
    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (toolbar as Toolbar).apply {
            setNavigationOnClickListener { activity?.onBackPressed() }
            setTitle(OriginR_string.settings_profile)
        }

        with (item_profile_property_hair_color) {
            // TODO:
        }
        with (item_profile_property_education) {
            setItems(EducationProfileProperty.values.toList())
        }
        with (item_profile_property_income) {
            setItems(IncomeProfileProperty.values.toList())
        }
        with (item_profile_property_property) {
            setItems(PropertyProfileProperty.values.toList())
        }
        with (item_profile_property_transport) {
            setItems(TransportProfileProperty.values.toList())
        }
    }
}
