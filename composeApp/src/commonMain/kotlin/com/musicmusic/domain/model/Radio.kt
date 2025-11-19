package com.musicmusic.domain.model

import kotlinx.serialization.Serializable

/**
 * Representa una estación de radio online.
 *
 * @property id Identificador único de la radio
 * @property name Nombre de la estación
 * @property url URL del stream de audio
 * @property genre Género musical
 * @property country País de origen
 * @property logoUrl URL o ruta al logo de la radio
 * @property description Descripción de la radio
 * @property bitrate Bitrate del stream en kbps
 * @property isFavorite Si está marcada como favorita
 * @property tags Etiquetas adicionales (ej: "rock", "news", "talk")
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
     * Convierte la radio a un objeto Song para reproducción.
     */
    fun toSong(): Song {
        return Song(
            id = "radio_$id",
            title = name,
            artist = country ?: "Radio",
            album = genre ?: "Internet Radio",
            duration = 0L, // Las radios no tienen duración
            filePath = url, // URL como "path"
            coverArtPath = logoUrl,
            genre = genre
        )
    }
}
