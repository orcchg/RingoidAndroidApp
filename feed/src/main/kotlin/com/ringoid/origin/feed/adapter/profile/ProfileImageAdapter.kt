package com.ringoid.origin.feed.adapter.profile

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.adapter.BaseListAdapter
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.model.EmptyProfileImageVO
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.utility.clickDebounce
import kotlinx.android.synthetic.main.rv_item_profile_image.view.*

class ProfileImageAdapter : BaseListAdapter<ProfileImageVO, BaseProfileImageViewHolder>(ProfileImageDiffCallback()) {

    var tabsObserver: RecyclerView.AdapterDataObserver? = null
    var isLikeEnabled = true

    override fun onFailedToRecycleView(holder: BaseProfileImageViewHolder): Boolean {
        holder.cancelAnimations()
        return true
    }

    override fun getLayoutId(): Int = R.layout.rv_item_profile_image

    override fun instantiateViewHolder(view: View): BaseProfileImageViewHolder =
        ProfileImageViewHolder(view, isLikeEnabled).also { vh ->
            vh.itemView.ibtn_like.clicks().compose(clickDebounce()).subscribe { getOnLikeButtonClickListener(vh).onClick(vh.itemView.ibtn_like) }
        }

    override fun instantiateHeaderViewHolder(view: View) = HeaderProfileImageViewHolder(view)

    override fun onBindViewHolder(holder: BaseProfileImageViewHolder, position: Int) {
        holder.setOnClickListener(getOnItemClickListener(holder))
        super.onBindViewHolder(holder, position)
    }

    override fun onBindViewHolder(holder: BaseProfileImageViewHolder, position: Int, payloads: List<Any>) {
        holder.setOnClickListener(getOnItemClickListener(holder))
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun getExposedCb(): (() -> Unit)? = { tabsObserver?.onChanged() }

    // --------------------------------------------------------------------------------------------
    override fun getStubItem(): ProfileImageVO = EmptyProfileImageVO

    // ------------------------------------------
    override fun getOnItemClickListener(vh: BaseProfileImageViewHolder): View.OnClickListener =
        if (!isLikeEnabled) {
            super.getOnItemClickListener(vh)
        } else {
            val clickListener = wrapOnItemClickListener(vh, getLikeClickListener(vh, setAlwaysLiked = true))
//        vh.itemView.setOnTouchListener { _, event ->
//            if (event.action == MotionEvent.ACTION_DOWN) {
//                vh.itemView.iv_like_anim.apply {
//                    x = event.x - width
//                    y = event.y - height
//                }
//            }
//            false
//        }
        clickListener
    }

    private fun getOnLikeButtonClickListener(vh: BaseProfileImageViewHolder): View.OnClickListener =
        wrapOnItemClickListener(vh, getLikeClickListener(vh))

    private fun getLikeClickListener(vh: BaseProfileImageViewHolder, setAlwaysLiked: Boolean = false)
        : ((model: ProfileImageVO, position: Int) -> Unit)? =
            { model: ProfileImageVO, position: Int ->
                val isLiked = if (setAlwaysLiked) true else !model.isLiked
                vh.animateLike(isLiked = isLiked)
                model.isLiked = isLiked
                itemClickListener?.invoke(model, position)
            }
}
