package com.ringoid.origin.feed.view.lc

import com.ringoid.base.view.Residual

data class PUSH_NEW_MESSAGES(val profileId: String) : Residual()

object ON_TRANSFER_PROFILE_COMPLETE : Residual()
