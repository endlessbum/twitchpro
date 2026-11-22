package com.endlessbum.twitchpro.model.helix.channel

import com.endlessbum.twitchpro.model.helix.Pagination
import kotlinx.serialization.Serializable

@Serializable
class ChannelSearchResponse(
    val data: List<ChannelSearch>,
    val pagination: Pagination? = null,
)