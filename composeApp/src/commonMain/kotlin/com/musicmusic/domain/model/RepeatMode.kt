package com.musicmusic.domain.model

/**
 * Modos de repetición para el reproductor.
 */
enum class RepeatMode {
    /** No repetir */
    OFF,
    
    /** Repetir toda la cola */
    ALL,
    
    /** Repetir solo la canción actual */
    ONE
}
