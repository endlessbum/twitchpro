package com.endlessbum.twitchpro.ui.tv

/** TV: Twitch gives the picture "404_processing" (a "?") for a video that records or is processed. */
object TvVideoThumbnail {

    fun isMissing(url: String?) = url.isNullOrBlank() || url.contains("404_processing")
}
