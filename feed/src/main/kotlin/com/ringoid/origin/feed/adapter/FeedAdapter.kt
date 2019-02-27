package com.ringoid.origin.feed.adapter

import android.view.View
import com.ringoid.domain.model.feed.EmptyProfile
import com.ringoid.domain.model.feed.Profile
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.adapter.base.*
import com.ringoid.origin.feed.model.ProfileImageVO

class FeedAdapter : BaseFeedAdapter<Profile, OriginFeedViewHolder<Profile>>(ProfileDiffCallback()) {

    var onLikeImageListener: ((model: ProfileImageVO, position: Int) -> Unit)? = null

    override fun getLayoutId(): Int = R.layout.rv_item_feed_profile

    override fun instantiateViewHolder(view: View): OriginFeedViewHolder<Profile> =
        FeedViewHolder(view, viewPool = imagesViewPool).also { vh ->
            vh.profileImageAdapter.itemClickListener = onLikeImageListener
        }

    override fun instantiateHeaderViewHolder(view: View) = HeaderFeedViewHolder(view)
    override fun instantiateFooterViewHolder(view: View) = FooterFeedViewHolder(view)

    // ------------------------------------------
    override fun getStubItem(): Profile = EmptyProfile

    override fun getFooterLayoutResId(): Int = R.layout.rv_item_feed_footer
}
