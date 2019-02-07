package com.ringoid.origin.feed.adapter.lmm

import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.feed.adapter.base.BaseFeedViewHolder
import com.ringoid.origin.feed.adapter.base.OriginFeedViewHolder
import com.ringoid.origin.feed.model.ProfileImageVO
import kotlinx.android.synthetic.main.rv_item_lmm_profile.view.*

open class MatchFeedAdapter(imagesViewPool: RecyclerView.RecycledViewPool? = null)
    : BaseLmmAdapter(imagesViewPool) {

    var onImageToOpenChatClickListener: ((model: ProfileImageVO, feedItemPosition: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OriginFeedViewHolder<FeedItem> {
        val viewHolder = super.onCreateViewHolder(parent, viewType)
        viewHolder.takeIf { it is BaseFeedViewHolder<FeedItem> }
            ?.let { it as BaseFeedViewHolder<FeedItem> }
            ?.also { vh ->
                vh.profileImageAdapter.also { adapter ->
                    adapter.isLikeEnabled = false  // hide like button on matches feed items
                    adapter.itemClickListener = wrapOnImageClickListenerByFeedItem(vh, onImageToOpenChatClickListener)
                }
                (vh.itemView.ibtn_message.layoutParams as? ConstraintLayout.LayoutParams)
                    ?.apply { verticalBias = 0.28f }?.let { vh.itemView.ibtn_message.layoutParams = it }
            }
        return viewHolder
    }
}
