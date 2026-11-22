package com.endlessbum.twitchpro.model.helix.video

import com.endlessbum.twitchpro.model.helix.Pagination
import kotlinx.serialization.Serializable

@Serializable
class VideosResponse(
    val data: List<Video>,
    val pagination: Pagination? = null,
)