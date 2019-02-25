package com.ringoid.origin.view.main.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.origin.R
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.getAttributeDrawable
import kotlinx.android.synthetic.main.widget_bottom_bar.view.*

class BottomBar : LinearLayout {

    private lateinit var ivItemFeed: ImageView
    private lateinit var ivItemLmm: ImageView
    private lateinit var ivItemProfile: ImageView

    var prevSelectedItem: Int? = null
        private set
    var selectedItem: Int? = null
        set(newValue) {
            if (newValue == null) {
                return
            }

            prevSelectedItem = field
            field = newValue
            if (prevSelectedItem == newValue) {
                reSelectListener?.invoke(newValue)
            } else {
                changeItemAppearance()
                selectListener?.invoke(newValue)
            }
        }

    private var selectListener: ((itemName: Int) -> Unit)? = null
    private var reSelectListener: ((itemName: Int) -> Unit)? = null

    private var feedIcon: Drawable? = null
    private var feedSelectIcon: Drawable? = null
    private var lmmIcon: Drawable? = null
    private var lmmSelectIcon: Drawable? = null
    private var profileIcon: Drawable? = null
    private var profileSelectIcon: Drawable? = null

    constructor(context: Context): this(context, null)

    constructor(context: Context, attributes: AttributeSet?): this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int): super(context, attributes, defStyleAttr) {
        init(context, attributes, defStyleAttr)
    }

    // ------------------------------------------
    @Suppress("CheckResult")
    private fun init(context: Context, attributes: AttributeSet?, defStyleAttr: Int) {
        feedIcon = context.getAttributeDrawable(R.attr.refDrawableBottomBarExplore)
        feedSelectIcon = context.getAttributeDrawable(R.attr.refDrawableBottomBarExplorePressed)
        lmmIcon = context.getAttributeDrawable(R.attr.refDrawableBottomBarLmm)
        lmmSelectIcon = context.getAttributeDrawable(R.attr.refDrawableBottomBarLmmPressed)
        profileIcon = context.getAttributeDrawable(R.attr.refDrawableBottomBarProfile)
        profileSelectIcon = context.getAttributeDrawable(R.attr.refDrawableBottomBarProfilePressed)

        minimumHeight = resources.getDimensionPixelSize(R.dimen.main_bottom_bar_height)
        orientation = HORIZONTAL
        LayoutInflater.from(context).inflate(R.layout.widget_bottom_bar, this, true)
        fl_item_lmm.clicks().compose(clickDebounce()).subscribe { selectedItem = 0 }
        fl_item_profile.clicks().compose(clickDebounce()).subscribe { selectedItem = 1 }
        fl_item_feed.clicks().compose(clickDebounce()).subscribe { selectedItem = 2 }

        ivItemLmm = findViewById(R.id.iv_item_lmm)
        ivItemProfile = findViewById(R.id.iv_item_profile)
        ivItemFeed = findViewById(R.id.iv_item_feed)
    }

    /* API */
    // --------------------------------------------------------------------------------------------
    fun setOnNavigationItemSelectedListener(l: ((item: Int) -> Unit)?) {
        selectListener = l
    }

    fun setOnNavigationItemReselectedListener(l: ((item: Int) -> Unit)?) {
        reSelectListener = l
    }

    fun showBadgeOnLmm(isVisible: Boolean) {
        iv_item_badge_lmm.changeVisibility(isVisible, soft = true)
    }

    fun showWarningOnProfile(isVisible: Boolean) {
        iv_item_warning_profile.changeVisibility(isVisible, soft = true)
    }

    // --------------------------------------------------------------------------------------------
    private fun changeItemAppearance() {
        when (prevSelectedItem) {
            0 -> ivItemLmm.apply { setImageDrawable(lmmIcon) }
            1 -> ivItemProfile.apply { setImageDrawable(profileIcon) }
            2 -> ivItemFeed.apply { setImageDrawable(feedIcon) }
            else -> null
        }?.also { it.isSelected = false }

        when (selectedItem) {
            0 -> ivItemLmm.apply { setImageDrawable(lmmSelectIcon) }
            1 -> ivItemProfile.apply { setImageDrawable(profileSelectIcon) }
            2 -> ivItemFeed.apply { setImageDrawable(feedSelectIcon) }
            else -> null
        }?.also { it.isSelected = true }
    }
}
