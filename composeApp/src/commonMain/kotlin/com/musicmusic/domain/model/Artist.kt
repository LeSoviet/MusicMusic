package com.musicmusic.domain.model

/**
 * Representa un artista en la biblioteca.
 *
 * @property id Identificador único del artista
 * @property name Nombre del artista
 * @property albumCount Número de álbumes
 * @property songCount Número de canciones
 * @property imageUrl URL o ruta a imagen del artista
 */
data class Artist(
    val id: String,
    val name: String,
    val albumCount: Int = 0,
    val songCount: Int = 0,
    val imageUrl: String? = null
)
