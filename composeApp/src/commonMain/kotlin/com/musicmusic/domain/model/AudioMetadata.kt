package com.musicmusic.domain.model

/**
 * Metadatos de un archivo de audio extraídos del sistema de archivos.
 *
 * @property title Título de la canción
 * @property artist Artista
 * @property album Álbum
 * @property albumArtist Artista del álbum
 * @property genre Género
 * @property year Año
 * @property trackNumber Número de pista
 * @property discNumber Número de disco
 * @property duration Duración en milisegundos
 * @property bitrate Bitrate en kbps
 * @property sampleRate Sample rate en Hz
 * @property coverArtData Datos binarios de la carátula embebida
 */
data class AudioMetadata(
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val albumArtist: String? = null,
    val genre: String? = null,
    val year: Int? = null,
    val trackNumber: Int? = null,
    val discNumber: Int? = null,
    val duration: Long = 0,
    val bitrate: Int? = null,
    val sampleRate: Int? = null,
    val coverArtData: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as AudioMetadata

        if (title != other.title) return false
        if (artist != other.artist) return false
        if (album != other.album) return false
        if (albumArtist != other.albumArtist) return false
        if (genre != other.genre) return false
        if (year != other.year) return false
        if (trackNumber != other.trackNumber) return false
        if (discNumber != other.discNumber) return false
        if (duration != other.duration) return false
        if (bitrate != other.bitrate) return false
        if (sampleRate != other.sampleRate) return false
        if (coverArtData != null) {
            if (other.coverArtData == null) return false
            if (!coverArtData.contentEquals(other.coverArtData)) return false
        } else if (other.coverArtData != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title?.hashCode() ?: 0
        result = 31 * result + (artist?.hashCode() ?: 0)
        result = 31 * result + (album?.hashCode() ?: 0)
        result = 31 * result + (albumArtist?.hashCode() ?: 0)
        result = 31 * result + (genre?.hashCode() ?: 0)
        result = 31 * result + (year ?: 0)
        result = 31 * result + (trackNumber ?: 0)
        result = 31 * result + (discNumber ?: 0)
        result = 31 * result + duration.hashCode()
        result = 31 * result + (bitrate ?: 0)
        result = 31 * result + (sampleRate ?: 0)
        result = 31 * result + (coverArtData?.contentHashCode() ?: 0)
        return result
    }
}
