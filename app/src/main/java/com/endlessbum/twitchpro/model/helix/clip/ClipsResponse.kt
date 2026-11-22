package com.endlessbum.twitchpro.model.helix.clip

import com.endlessbum.twitchpro.model.helix.Pagination
import kotlinx.serialization.Serializable

@Serializable
class ClipsResponse(
    val data: List<Clip>,
    val pagination: Pagination? = null,
)