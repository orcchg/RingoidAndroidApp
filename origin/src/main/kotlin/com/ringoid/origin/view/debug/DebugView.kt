package com.ringoid.origin.view.debug

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.LayoutRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.debug.DebugOnly
import com.ringoid.domain.debug.EmptyDebugLogItem
import com.ringoid.origin.R
import com.ringoid.origin.WidgetR_drawable
import com.ringoid.utility.*
import com.uber.autodispose.AutoDispose.autoDisposable
import com.uber.autodispose.android.scope
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.widget_debug.view.*
import timber.log.Timber

@DebugOnly
class DebugView : ConstraintLayout {

    private val debugLogItemAdapter = DebugLogItemAdapter { postDelayed({ rv_debug_items.linearLayoutManager()?.scrollToPositionWithOffset(it - 1, 0) }, 100L) }
    private var sizeToggle = false

    companion object {
        private var MIN_HEIGHT = -1
    }

    constructor(context: Context): this(context, null)

    constructor(context: Context, attributes: AttributeSet?): this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int): super(context, attributes, defStyleAttr) {
        init(context)
    }

    @LayoutRes private fun getLayoutId(): Int = R.layout.widget_debug

    @Suppress("CheckResult")
    private fun init(context: Context) {
        if (MIN_HEIGHT == -1) MIN_HEIGHT = resources.getDimensionPixelSize(R.dimen.widget_debug_height)

        background = ContextCompat.getDrawable(context, WidgetR_drawable.rect_white_80_opaque)
        LayoutInflater.from(context).inflate(getLayoutId(), this, true)

        rv_debug_items.apply {
            adapter = debugLogItemAdapter
            layoutManager = LinearLayoutManager(context).also { it.stackFromEnd = true }
        }

        ibtn_clear_debug.clicks().compose(clickDebounce()).subscribe { clear() }
        ibtn_resize_debug.clicks().compose(clickDebounce()).subscribe {
            if (sizeToggle) minimize() else maximize()
        }
        ibtn_share_debug.clicks().compose(clickDebounce()).subscribe {
            context.copyToClipboard(key = DomainUtil.CLIPBOARD_KEY_DEBUG, value = debugLogItemAdapter.getContentText())
            context.toast(R.string.common_clipboard)
        }
        minimize()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        DebugLogUtil.logger
            .observeOn(AndroidSchedulers.mainThread())
            .`as`(autoDisposable(scope()))
            .subscribe({
                if (it == EmptyDebugLogItem) {
                    clear()
                } else {
                    debugLogItemAdapter.append(DebugLogItemVO.from(it))
                }
            }, Timber::e)
    }

    private fun clear() {
        debugLogItemAdapter.clear()
    }

    private fun maximize() {
        sizeToggle = true
        minimumHeight = (context as? Activity)?.getScreenHeight() ?: MIN_HEIGHT * 3
    }

    private fun minimize() {
        sizeToggle = false
        minimumHeight = MIN_HEIGHT
    }
}
