package com.endlessbum.twitchpro.model.helix.follower

import com.endlessbum.twitchpro.model.helix.Pagination
import kotlinx.serialization.Serializable

@Serializable
class FollowersResponse(
    val data: List<Follower>,
    val pagination: Pagination? = null,
)