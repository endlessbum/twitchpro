package com.endlessbum.twitchpro.model.gql

import kotlinx.serialization.Serializable

@Serializable
class PageInfo(
    val hasNextPage: Boolean? = null,
)