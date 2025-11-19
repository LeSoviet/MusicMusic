package com.musicmusic.domain.model

/**
 * Representa un álbum en la biblioteca.
 *
 * @property id Identificador único del álbum
 * @property title Título del álbum
 * @property artist Artista del álbum
 * @property year Año de lanzamiento
 * @property genre Género predominante
 * @property coverArtPath Ruta a la carátula del álbum
 * @property songCount Número de canciones en el álbum
 * @property totalDuration Duración total en milisegundos
 * @property dateAdded Fecha de agregado a la biblioteca
 */
data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val year: Int? = null,
    val genre: String? = null,
    val coverArtPath: String? = null,
    val songCount: Int = 0,
    val totalDuration: Long = 0,
    val dateAdded: Long = System.currentTimeMillis()
) {
    /**
     * Retorna la duración total formateada
     */
    fun getFormattedDuration(): String {
        val totalSeconds = totalDuration / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        
        return if (hours > 0) {
            "%d h %d min".format(hours, minutes)
        } else {
            "%d min".format(minutes)
        }
    }
}
