package com.endlessbum.twitchpro.model.helix.user

import kotlinx.serialization.Serializable

@Serializable
class UsersResponse(
    val data: List<User>,
)