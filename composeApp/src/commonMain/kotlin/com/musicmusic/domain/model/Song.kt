package com.musicmusic.domain.model

/**
 * Representa una canción en la biblioteca local o desde una fuente de streaming.
 *
 * @property id Identificador único de la canción
 * @property title Título de la canción
 * @property artist Nombre del artista
 * @property album Nombre del álbum
 * @property albumArtist Artista del álbum (puede diferir del artista de la canción)
 * @property genre Género musical
 * @property year Año de lanzamiento
 * @property duration Duración en milisegundos
 * @property trackNumber Número de pista en el álbum
 * @property discNumber Número de disco (para álbumes multi-disco)
 * @property filePath Ruta absoluta al archivo de audio (null para streaming)
 * @property coverArtPath Ruta al archivo de carátula (null si no tiene)
 * @property bitrate Bitrate en kbps
 * @property sampleRate Sample rate en Hz
 * @property fileSize Tamaño del archivo en bytes (0 para streaming)
 * @property dateAdded Fecha en que se agregó a la biblioteca
 * @property playCount Número de veces reproducida
 * @property isFavorite Si está marcada como favorita
 */
data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val albumArtist: String? = null,
    val genre: String? = null,
    val year: Int? = null,
    val duration: Long, // milliseconds
    val trackNumber: Int? = null,
    val discNumber: Int? = null,
    val filePath: String? = null,
    val coverArtPath: String? = null,
    val bitrate: Int? = null,
    val sampleRate: Int? = null,
    val fileSize: Long = 0,
    val dateAdded: Long = System.currentTimeMillis(),
    val playCount: Int = 0,
    val isFavorite: Boolean = false
) {
    /**
     * Retorna el URI para reproducir esta canción
     */
    fun getPlaybackUri(): String = filePath ?: ""
    
    /**
     * Retorna el nombre del artista a mostrar (prioriza album artist si existe)
     */
    fun getDisplayArtist(): String = albumArtist ?: artist
    
    /**
     * Retorna la duración formateada como "mm:ss"
     */
    fun getFormattedDuration(): String {
        val totalSeconds = duration / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%d:%02d".format(minutes, seconds)
    }
    
    /**
     * Retorna si es una canción local o de streaming
     */
    fun isLocal(): Boolean = filePath != null
}
