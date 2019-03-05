package com.ringoid.origin.feed.adapter

import android.view.View
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.adapter.base.*
import com.ringoid.origin.feed.model.EmptyFeedItemVO
import com.ringoid.origin.feed.model.FeedItemVO
import com.ringoid.origin.feed.model.ProfileImageVO

class FeedAdapter : BaseFeedAdapter(FeedItemVODiffCallback()) {

    var onLikeImageListener: ((model: ProfileImageVO, position: Int) -> Unit)? = null

    override fun getLayoutId(): Int = R.layout.rv_item_feed_profile

    override fun instantiateViewHolder(view: View): OriginFeedViewHolder =
        FeedViewHolder(view, viewPool = imagesViewPool).also { vh ->
            vh.profileImageAdapter.itemClickListener = onLikeImageListener
        }

    override fun instantiateHeaderViewHolder(view: View) = HeaderFeedViewHolder(view)
    override fun instantiateFooterViewHolder(view: View) = FooterFeedViewHolder(view)

    // ------------------------------------------
    override fun getStubItem(): FeedItemVO = EmptyFeedItemVO

    override fun getFooterLayoutResId(): Int = R.layout.rv_item_feed_footer
}
