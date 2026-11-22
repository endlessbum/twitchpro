package com.endlessbum.twitchpro.model.helix.follow

import com.endlessbum.twitchpro.model.helix.Pagination
import kotlinx.serialization.Serializable

@Serializable
class FollowsResponse(
    val data: List<Follow>,
    val pagination: Pagination? = null,
)