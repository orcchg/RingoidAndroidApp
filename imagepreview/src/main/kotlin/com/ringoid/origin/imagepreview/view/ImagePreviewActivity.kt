package com.ringoid.origin.imagepreview.view

import android.content.Intent
import android.os.Bundle
import com.ringoid.origin.imagepreview.R
import com.ringoid.origin.navigation.CONTENT_URI
import com.ringoid.origin.navigation.Extras
import com.ringoid.origin.navigation.NavigateFrom
import com.ringoid.origin.navigation.navigate
import com.ringoid.origin.view.base.BaseHostActivity

class ImagePreviewActivity : BaseHostActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState ?: run {
            val fragment = ImagePreviewFragment.newInstance(
                uri = intent.getParcelableExtra(CONTENT_URI),
                navigateFrom = intent.getStringExtra(Extras.EXTRA_NAVIGATE_FROM))
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fl_container, fragment, ImagePreviewFragment.TAG)
                .commitNow()
        }
    }

    override fun onBackPressed() {
        intent.getStringExtra(Extras.EXTRA_NAVIGATE_FROM)
              ?.takeIf { it == NavigateFrom.SCREEN_LOGIN }
              ?.let {
                  val data = Intent().putExtra(Extras.EXTRA_MAIN_TAB, NavigateFrom.MAIN_TAB_PROFILE)
                  navigate(this, path = "/main", payload = data)
              }
        super.onBackPressed()
    }
}
