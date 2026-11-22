package com.endlessbum.twitchpro.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
class NotificationUser(
    @PrimaryKey
    val channelId: String,
)