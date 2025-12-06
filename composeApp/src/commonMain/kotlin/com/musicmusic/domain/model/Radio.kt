package com.musicmusic.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents an online radio station.
 *
 * @property id Unique radio identifier
 * @property name Station name
 * @property url Audio stream URL
 * @property genre Music genre
 * @property country Country of origin
 * @property logoUrl URL or path to radio logo
 * @property description Radio description
 * @property bitrate Stream bitrate in kbps
 * @property isFavorite Whether it's marked as favorite
 * @property tags Additional tags (e.g., "rock", "news", "talk")
 */
@Serializable
data class Radio(
    val id: String,
    val name: String,
    val url: String,
    val genre: String? = null,
    val country: String? = null,
    val logoUrl: String? = null,
    val description: String? = null,
    val bitrate: Int? = null,
    val isFavorite: Boolean = false,
    val tags: List<String> = emptyList()
) {
    /**
     * Converts radio to a Song object for playback.
     */
    fun toSong(): Song {
        return Song(
            id = "radio_$id",
            title = name,
            artist = country ?: "Radio",
            album = genre ?: "Internet Radio",
            duration = 0L, // Radios don't have duration
            filePath = url, // URL as "path"
            coverArtPath = logoUrl,
            genre = genre
        )
    }
}
