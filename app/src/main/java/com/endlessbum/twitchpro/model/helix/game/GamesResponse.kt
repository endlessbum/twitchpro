package com.endlessbum.twitchpro.model.helix.game

import com.endlessbum.twitchpro.model.helix.Pagination
import kotlinx.serialization.Serializable

@Serializable
class GamesResponse(
    val data: List<Game>,
    val pagination: Pagination? = null,
)