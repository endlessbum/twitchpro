package com.endlessbum.twitchpro.model.helix.chat

import com.endlessbum.twitchpro.model.helix.Pagination
import kotlinx.serialization.Serializable

@Serializable
class UserEmotesResponse(
    val template: String,
    val data: List<EmoteTemplate>,
    val pagination: Pagination? = null,
)