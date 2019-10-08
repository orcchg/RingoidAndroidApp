package com.ringoid.domain.exception

import com.ringoid.domain.model.actions.OriginActionObject

class CommitActionsException(val failToCommit: Collection<OriginActionObject>, cause: Throwable)
    : RuntimeException("Failed to commit ${failToCommit.size} action objects", cause)
