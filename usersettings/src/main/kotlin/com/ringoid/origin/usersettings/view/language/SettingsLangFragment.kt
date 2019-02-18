package com.ringoid.origin.usersettings.view.language

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.ringoid.base.view.BaseFragment
import com.ringoid.origin.usersettings.OriginR_string
import com.ringoid.origin.usersettings.R
import com.ringoid.origin.usersettings.view.language.adapter.LanguageItemVO
import com.ringoid.origin.usersettings.view.language.adapter.SettingsLangAdapter
import kotlinx.android.synthetic.main.fragment_settings_language.*

class SettingsLangFragment : BaseFragment<SettingsLangViewModel>() {

    companion object {
        internal const val TAG = "LanguageFragment_tag"

        fun newInstance(): SettingsLangFragment = SettingsLangFragment()
    }

    private val langAdapter = SettingsLangAdapter().apply {
        itemClickListener = { model, _ -> vm.selectLanguage(model.language) }
    }

    override fun getVmClass(): Class<SettingsLangViewModel> = SettingsLangViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_settings_language

    // --------------------------------------------------------------------------------------------
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (toolbar as Toolbar).apply {
            setNavigationOnClickListener { activity?.onBackPressed() }
            setTitle(OriginR_string.settings_language)
        }

        rv_items.apply {
            adapter = langAdapter
            layoutManager = LinearLayoutManager(context)
        }
        // TODO: fill langAdapter with available langs
        langAdapter.submitList(listOf(LanguageItemVO("English"), LanguageItemVO("Russian")))
    }
}
