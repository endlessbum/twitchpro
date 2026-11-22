package com.endlessbum.twitchpro.model.helix.stream

import com.endlessbum.twitchpro.model.helix.Pagination
import kotlinx.serialization.Serializable

@Serializable
class StreamsResponse(
    val data: List<Stream>,
    val pagination: Pagination? = null,
)