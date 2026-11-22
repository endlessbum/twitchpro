package com.endlessbum.twitchpro.model.gql

import kotlinx.serialization.Serializable

@Serializable
class Error(
    val message: String? = null,
)