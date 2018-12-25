package com.ringoid.domain.repository.feed

import com.ringoid.domain.model.feed.Feed
import io.reactivex.Single

interface IFeedRepository {

    fun getNewFaces(resolution: String, limit: Int): Single<Feed>
}
