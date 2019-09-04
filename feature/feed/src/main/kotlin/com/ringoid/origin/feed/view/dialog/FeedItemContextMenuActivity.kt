package com.ringoid.origin.feed.view.dialog

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.gson.Gson
import com.ringoid.base.deeplink.AppNav
import com.ringoid.base.view.SimpleBaseActivity
import com.ringoid.imageloader.ImageLoader
import com.ringoid.origin.feed.R
import com.ringoid.origin.model.BlockReportPayload
import com.ringoid.origin.navigation.Extras
import kotlinx.android.synthetic.main.activity_bottom_sheet_block.*

@AppNav("block_dialog", "report_dialog")
class FeedItemContextMenuActivity : SimpleBaseActivity(), IBlockBottomSheetActivity {

    companion object {
        private const val BUNDLE_KEY_OUTPUT_DATA = "bundle_key_output_data"
    }

    private var blockDialog: BlockBottomSheetDialog? = null
    private var reportDialog: ReportBottomSheetDialog? = null
    private lateinit var outputData: Intent

    override fun getLayoutId(): Int = R.layout.activity_bottom_sheet_block

    // --------------------------------------------------------------------------------------------
    override fun onClose() {
        setResultExposed(currentResult, outputData)
        finish()
    }

    // ------------------------------------------
    override fun onBlock() {
        setResultExposed(Activity.RESULT_OK, outputData)
    }

    override fun onReport(reason: Int) {
        outputData.putExtra(Extras.OUT_EXTRA_REPORT_REASON, reason)
        setResultExposed(Activity.RESULT_OK, outputData)
    }

    override fun onReportSheetOpen() {
        createReportDialogIfNeed()
        reportDialog?.showNow(supportFragmentManager, ReportBottomSheetDialog.TAG)
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        outputData = savedInstanceState?.let { it.getParcelable<Intent>(BUNDLE_KEY_OUTPUT_DATA) }
            ?: Intent().putExtras(intent.extras!!)
        savedInstanceState ?: run {
            if (intent.dataString?.contains("block_dialog") == true) {
                createBlockDialogIfNeed()
                blockDialog?.showNow(supportFragmentManager, BlockBottomSheetDialog.TAG)
            }
            if (intent.dataString?.contains("report_dialog") == true) {
                createReportDialogIfNeed()
                reportDialog?.showNow(supportFragmentManager, ReportBottomSheetDialog.TAG)
            }
        }

        intent.extras?.let {
            val payloadJson = it.getString("payload") ?: "{}"
            val payload = Gson().fromJson(payloadJson, BlockReportPayload::class.java)
            ImageLoader.load(uri = payload.profileImageUri, thumbnailUri = payload.profileThumbnailUri,
                             iv = iv_profile_image)
        }
    }

    override fun onBackPressed() {
        onClose()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(BUNDLE_KEY_OUTPUT_DATA, outputData)
    }

    override fun onDestroy() {
        super.onDestroy()
        blockDialog = null
        reportDialog = null
    }

    // --------------------------------------------------------------------------------------------
    private fun createBlockDialogIfNeed() {
        if (blockDialog == null) {
            blockDialog = BlockBottomSheetDialog.newInstance()
        }
    }

    private fun createReportDialogIfNeed() {
        if (reportDialog == null) {
            val excludedReasons = intent.extras?.getString("excludedReasons")
            reportDialog = ReportBottomSheetDialog.newInstance(excludedReasons)
        }
    }
}
