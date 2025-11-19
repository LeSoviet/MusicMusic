package com.musicmusic.domain.model

/**
 * Estados posibles del reproductor de audio.
 */
enum class PlaybackState {
    /** El reproductor está detenido */
    STOPPED,
    
    /** El reproductor está reproduciendo */
    PLAYING,
    
    /** El reproductor está pausado */
    PAUSED,
    
    /** El reproductor está cargando/bufferizando */
    BUFFERING,
    
    /** Error en la reproducción */
    ERROR
}
