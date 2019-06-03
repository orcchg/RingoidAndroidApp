package com.ringoid.origin

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat

object AppRes {

    var BUTTON_HEIGHT: Int = -1
        private set
    var BUTTON_FLAT_TEXT_SIZE: Int = -1
        private set
    var BUTTON_FLAT_INC_TEXT_SIZE: Int = -1
        private set
    var BLOCK_BOTTOM_SHEET_DIALOG_HEIGHT: Int = -1
        private set
    var ICON_SIZE_36: Int = -1
        private set
    var ICON_SIZE_96: Int = -1
        private set
    var ICON_SIZE_HALF_96: Int = -1
        private set
    var ICON_SIZE_HALF2_96: Int = -1
        private set
    var FEED_ITEM_FOOTER_LABEL_BOTTOM: Int = -1
        private set
    var FEED_IMAGE_HEIGHT: Int = -1
        private set
    var FEED_IMAGE_HALF_HEIGHT: Int = -1
        private set
    var FEED_ITEM_BIAS_BTN_BOTTOM: Int = -1
        private set
    var FEED_ITEM_BIAS_BTN_TOP: Int = -1
        private set
    var FEED_ITEM_AGE_BOTTOM: Int = -1
        private set
    var FEED_ITEM_AGE_TOP: Int = -1
        private set
    var FEED_ITEM_DISTANCE_BOTTOM: Int = -1
        private set
    var FEED_ITEM_DISTANCE_TOP: Int = -1
        private set
    var FEED_ITEM_MID_BTN_BOTTOM: Int = -1
        private set
    var FEED_ITEM_MID_BTN_TOP: Int = -1
        private set
    var FEED_ITEM_ONLINE_STATUS_TOP: Int = -1
        private set
    var FEED_ITEM_PROPERTY_EDUCATION_TOP: Int = -1
        private set
    var FEED_ITEM_PROPERTY_EDUCATION_BOTTOM: Int = -1
        private set
    var FEED_ITEM_PROPERTY_HAIR_COLOR_TOP: Int = -1
        private set
    var FEED_ITEM_PROPERTY_HAIR_COLOR_BOTTOM: Int = -1
        private set
    var FEED_ITEM_PROPERTY_HEIGHT_TOP: Int = -1
        private set
    var FEED_ITEM_PROPERTY_HEIGHT_BOTTOM: Int = -1
        private set
    var FEED_ITEM_PROPERTY_INCOME_TOP: Int = -1
        private set
    var FEED_ITEM_PROPERTY_INCOME_BOTTOM: Int = -1
        private set
    var FEED_ITEM_PROPERTY_PROPERTY_TOP: Int = -1
        private set
    var FEED_ITEM_PROPERTY_PROPERTY_BOTTOM: Int = -1
        private set
    var FEED_ITEM_PROPERTY_TRANSPORT_TOP: Int = -1
        private set
    var FEED_ITEM_PROPERTY_TRANSPORT_BOTTOM: Int = -1
        private set
    var FEED_ITEM_SETTINGS_BTN_BOTTOM: Int = -1
        private set
    var FEED_ITEM_SETTINGS_BTN_TOP: Int = -1
        private set
    var FEED_ITEM_TABS_INDICATOR_BOTTOM: Int = -1
        private set
    var FEED_ITEM_TABS_INDICATOR_BOTTOM2: Int = -1
        private set
    var FEED_ITEM_TABS_INDICATOR_TOP: Int = -1
        private set
    var FEED_ITEM_TABS_INDICATOR_TOP2: Int = -1
        private set
    var LMM_TOP_TAB_BAR_HIDE_AREA_HEIGHT: Int = -1
        private set
    var MAIN_BOTTOM_BAR_HEIGHT: Int = -1
        private set
    var MAIN_BOTTOM_BAR_HEIGHT_HALF: Int = -1
        private set
    var STD_MARGIN_8: Int = -1
        private set
    var STD_MARGIN_16: Int = -1
        private set
    var STD_MARGIN_18: Int = -1
        private set
    var STD_MARGIN_24: Int = -1
        private set
    var STD_MARGIN_32: Int = -1
        private set
    var STD_MARGIN_40: Int = -1
        private set
    var STD_MARGIN_48: Int = -1
        private set
    var STD_MARGIN_64: Int = -1
        private set
    var SCREEN_WIDTH: Int = -1  // set by Activity

    // ------------------------------------------
    lateinit var UNDERSCORE_DRAWABLE: Drawable

    /* Translatable */
    // ------------------------------------------
    var LENGTH_CM = ""
        private set

    // ------------------------------------------
    var EMAIL_OFFICER_MAIL_SUBJECT = ""
        private set
    var EMAIL_SUPPORT_MAIL_SUBJECT = ""
        private set

    var WEB_URL_LICENSES: String = ""
        private set
    var WEB_URL_PRIVACY: String = ""
        private set
    var WEB_URL_TERMS: String = ""
        private set

    fun init(context: Context) {
        context.resources.apply {
            STD_MARGIN_8 = getDimensionPixelSize(R.dimen.std_margin_8)
            STD_MARGIN_16 = getDimensionPixelSize(R.dimen.std_margin_16)
            STD_MARGIN_18 = getDimensionPixelSize(R.dimen.std_margin_18)
            STD_MARGIN_24 = getDimensionPixelSize(R.dimen.std_margin_24)
            STD_MARGIN_32 = getDimensionPixelSize(R.dimen.std_margin_32)
            STD_MARGIN_40 = getDimensionPixelSize(R.dimen.std_margin_40)
            STD_MARGIN_48 = getDimensionPixelSize(R.dimen.std_margin_48)
            STD_MARGIN_64 = getDimensionPixelSize(R.dimen.std_margin_64)

            BUTTON_HEIGHT = getDimensionPixelSize(R.dimen.std_btn_height)
            BUTTON_FLAT_TEXT_SIZE = getDimensionPixelSize(R.dimen.std_text_18)
            BUTTON_FLAT_INC_TEXT_SIZE = getDimensionPixelSize(R.dimen.std_text_20)
            BLOCK_BOTTOM_SHEET_DIALOG_HEIGHT = getDimensionPixelSize(R.dimen.dialog_bottom_sheet_block_height)
            ICON_SIZE_36 = getDimensionPixelSize(R.dimen.std_icon_36)
            ICON_SIZE_96 = getDimensionPixelSize(R.dimen.std_icon_96)
            ICON_SIZE_HALF_96 = ICON_SIZE_96 / 2
            ICON_SIZE_HALF2_96 = ICON_SIZE_HALF_96 / 2

            FEED_ITEM_FOOTER_LABEL_BOTTOM = getDimensionPixelSize(R.dimen.std_margin_16)
            FEED_IMAGE_HEIGHT = getDimensionPixelSize(R.dimen.std_image_height)
            FEED_IMAGE_HALF_HEIGHT = getDimensionPixelSize(R.dimen.std_image_height_half)
            FEED_ITEM_BIAS_BTN_BOTTOM = (FEED_IMAGE_HEIGHT * 0.34f).toInt()
            FEED_ITEM_BIAS_BTN_TOP = (FEED_ITEM_BIAS_BTN_BOTTOM - ICON_SIZE_36 * 0.6f).toInt()
            FEED_ITEM_AGE_BOTTOM = FEED_IMAGE_HEIGHT - STD_MARGIN_24 - STD_MARGIN_16
            FEED_ITEM_AGE_TOP = FEED_IMAGE_HEIGHT - STD_MARGIN_40 - STD_MARGIN_16
            FEED_ITEM_DISTANCE_BOTTOM = FEED_IMAGE_HEIGHT - STD_MARGIN_24 - STD_MARGIN_16
            FEED_ITEM_DISTANCE_TOP = FEED_IMAGE_HEIGHT - STD_MARGIN_40 - STD_MARGIN_16
            FEED_ITEM_MID_BTN_BOTTOM = (FEED_IMAGE_HALF_HEIGHT + ICON_SIZE_36 * 0.5f).toInt()
            FEED_ITEM_MID_BTN_TOP = (FEED_IMAGE_HALF_HEIGHT - ICON_SIZE_36 * 0.5f).toInt()
            FEED_ITEM_ONLINE_STATUS_TOP = STD_MARGIN_8 + ICON_SIZE_36

            val pivot = (0.3 * (FEED_IMAGE_HEIGHT + STD_MARGIN_32)).toInt()
            FEED_ITEM_PROPERTY_EDUCATION_TOP = pivot + STD_MARGIN_18 * 3
            FEED_ITEM_PROPERTY_EDUCATION_BOTTOM = FEED_ITEM_PROPERTY_EDUCATION_TOP + STD_MARGIN_18 * 2
            FEED_ITEM_PROPERTY_HAIR_COLOR_BOTTOM = FEED_ITEM_DISTANCE_TOP - STD_MARGIN_18
            FEED_ITEM_PROPERTY_HAIR_COLOR_TOP = FEED_ITEM_PROPERTY_HAIR_COLOR_BOTTOM - STD_MARGIN_18 * 2
            FEED_ITEM_PROPERTY_HEIGHT_BOTTOM = FEED_ITEM_PROPERTY_HAIR_COLOR_TOP - STD_MARGIN_18
            FEED_ITEM_PROPERTY_HEIGHT_TOP = FEED_ITEM_PROPERTY_HEIGHT_BOTTOM - STD_MARGIN_18 * 2
            FEED_ITEM_PROPERTY_INCOME_TOP = pivot + STD_MARGIN_18 * 5 + STD_MARGIN_16
            FEED_ITEM_PROPERTY_INCOME_BOTTOM = FEED_ITEM_PROPERTY_INCOME_TOP + STD_MARGIN_18 * 2
            FEED_ITEM_PROPERTY_PROPERTY_TOP = pivot - STD_MARGIN_18
            FEED_ITEM_PROPERTY_PROPERTY_BOTTOM = pivot + STD_MARGIN_18
            FEED_ITEM_PROPERTY_TRANSPORT_TOP = pivot + STD_MARGIN_18
            FEED_ITEM_PROPERTY_TRANSPORT_BOTTOM = FEED_ITEM_PROPERTY_TRANSPORT_TOP + STD_MARGIN_18 * 2

            FEED_ITEM_TABS_INDICATOR_TOP = STD_MARGIN_16
            FEED_ITEM_TABS_INDICATOR_TOP2 = FEED_IMAGE_HEIGHT - STD_MARGIN_32
            FEED_ITEM_TABS_INDICATOR_BOTTOM = (FEED_ITEM_TABS_INDICATOR_TOP * 1.5f).toInt()
            FEED_ITEM_TABS_INDICATOR_BOTTOM2 = FEED_IMAGE_HEIGHT - STD_MARGIN_16
            FEED_ITEM_SETTINGS_BTN_TOP = FEED_ITEM_TABS_INDICATOR_TOP  // getDimensionPixelSize(R.dimen.std_margin_24)
            FEED_ITEM_SETTINGS_BTN_BOTTOM = (FEED_ITEM_SETTINGS_BTN_TOP * 1.5f).toInt()
            LMM_TOP_TAB_BAR_HIDE_AREA_HEIGHT = (BUTTON_HEIGHT * 0.67f).toInt()
            MAIN_BOTTOM_BAR_HEIGHT = getDimensionPixelSize(R.dimen.main_bottom_bar_height)
            MAIN_BOTTOM_BAR_HEIGHT_HALF = getDimensionPixelSize(R.dimen.main_bottom_bar_height_half)

            // ----------------------------------
            UNDERSCORE_DRAWABLE = ContextCompat.getDrawable(context, WidgetR_drawable.underscore)!!

            // ----------------------------------
            initTranslatableStrings(this)

            // ----------------------------------
            EMAIL_OFFICER_MAIL_SUBJECT = getString(R.string.settings_info_email_officer_mail_subject)
            EMAIL_SUPPORT_MAIL_SUBJECT = getString(R.string.settings_support_mail_subject)

            WEB_URL_LICENSES = getString(R.string.web_url_licenses)
            WEB_URL_PRIVACY = getString(R.string.web_url_privacy)
            WEB_URL_TERMS = getString(R.string.web_url_terms)
        }
    }

    fun initTranslatableStrings(resources: Resources) {
        with (resources) {
            LENGTH_CM = getString(R.string.value_cm)
        }
    }
}
