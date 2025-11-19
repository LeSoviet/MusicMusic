package com.musicmusic.domain.audio

import com.musicmusic.domain.model.PlaybackState
import com.musicmusic.domain.model.RepeatMode
import com.musicmusic.domain.model.Song
import kotlinx.coroutines.flow.StateFlow

/**
 * Interfaz para el reproductor de audio.
 * 
 * Esta interfaz define el contrato para reproducción de audio en la aplicación.
 * Es independiente de la implementación específica (VLCJ, MediaPlayer, etc.)
 */
interface AudioPlayer {
    
    // ========== Estados observables ==========
    
    /** Estado actual del reproductor */
    val playbackState: StateFlow<PlaybackState>
    
    /** Canción actual en reproducción (null si no hay ninguna) */
    val currentSong: StateFlow<Song?>
    
    /** Posición actual de reproducción en milisegundos */
    val currentPosition: StateFlow<Long>
    
    /** Duración total de la canción actual en milisegundos */
    val duration: StateFlow<Long>
    
    /** Volumen actual (0.0f a 1.0f) */
    val volume: StateFlow<Float>
    
    /** Si está en shuffle */
    val isShuffleEnabled: StateFlow<Boolean>
    
    /** Modo de repetición actual */
    val repeatMode: StateFlow<RepeatMode>
    
    /** Cola de reproducción actual */
    val queue: StateFlow<List<Song>>
    
    /** Índice de la canción actual en la cola */
    val currentIndex: StateFlow<Int>
    
    // ========== Controles de reproducción ==========
    
    /**
     * Reproduce una canción.
     * 
     * @param song Canción a reproducir
     */
    suspend fun play(song: Song)
    
    /**
     * Reproduce una lista de canciones desde un índice específico.
     * 
     * @param songs Lista de canciones
     * @param startIndex Índice de la canción inicial (default: 0)
     */
    suspend fun playQueue(songs: List<Song>, startIndex: Int = 0)
    
    /**
     * Pausa la reproducción actual.
     */
    suspend fun pause()
    
    /**
     * Resume la reproducción pausada.
     */
    suspend fun resume()
    
    /**
     * Alterna entre play y pause.
     */
    suspend fun togglePlayPause()
    
    /**
     * Detiene completamente la reproducción.
     */
    suspend fun stop()
    
    /**
     * Salta a la siguiente canción en la cola.
     * 
     * @return true si hay una siguiente canción, false si no
     */
    suspend fun next(): Boolean
    
    /**
     * Vuelve a la canción anterior en la cola.
     * 
     * @return true si hay una canción anterior, false si no
     */
    suspend fun previous(): Boolean
    
    // ========== Control de posición ==========
    
    /**
     * Salta a una posición específica en la canción actual.
     * 
     * @param positionMs Posición en milisegundos
     */
    suspend fun seekTo(positionMs: Long)
    
    /**
     * Avanza la reproducción por un número de segundos.
     * 
     * @param seconds Segundos a avanzar
     */
    suspend fun seekForward(seconds: Int = 10)
    
    /**
     * Retrocede la reproducción por un número de segundos.
     * 
     * @param seconds Segundos a retroceder
     */
    suspend fun seekBackward(seconds: Int = 10)
    
    // ========== Control de volumen ==========
    
    /**
     * Establece el volumen del reproductor.
     * 
     * @param volume Volumen de 0.0f (silencio) a 1.0f (máximo)
     */
    suspend fun setVolume(volume: Float)
    
    /**
     * Aumenta el volumen en un porcentaje.
     * 
     * @param amount Cantidad a aumentar (default: 0.1f = 10%)
     */
    suspend fun increaseVolume(amount: Float = 0.1f)
    
    /**
     * Disminuye el volumen en un porcentaje.
     * 
     * @param amount Cantidad a disminuir (default: 0.1f = 10%)
     */
    suspend fun decreaseVolume(amount: Float = 0.1f)
    
    /**
     * Silencia o activa el audio.
     * 
     * @param mute true para silenciar, false para activar
     */
    suspend fun setMute(mute: Boolean)
    
    // ========== Control de cola y modos ==========
    
    /**
     * Activa o desactiva el modo shuffle.
     * 
     * @param enabled true para activar shuffle
     */
    suspend fun setShuffle(enabled: Boolean)
    
    /**
     * Cambia el modo de repetición.
     * 
     * @param mode Nuevo modo de repetición
     */
    suspend fun setRepeatMode(mode: RepeatMode)
    
    /**
     * Obtiene la cola actual de reproducción.
     * 
     * @return Lista de canciones en la cola
     */
    fun getQueue(): List<Song>
    
    /**
     * Agrega una canción al final de la cola.
     * 
     * @param song Canción a agregar
     */
    suspend fun addToQueue(song: Song)
    
    /**
     * Agrega múltiples canciones al final de la cola.
     * 
     * @param songs Canciones a agregar
     */
    suspend fun addToQueue(songs: List<Song>)
    
    /**
     * Limpia la cola de reproducción.
     */
    suspend fun clearQueue()
    
    /**
     * Reproduce una canción específica de la cola.
     * 
     * @param index Índice de la canción en la cola
     */
    suspend fun playAtIndex(index: Int)
    
    /**
     * Remueve una canción de la cola.
     * 
     * @param index Índice de la canción a remover
     */
    suspend fun removeFromQueue(index: Int)
    
    // ========== Lifecycle ==========
    
    /**
     * Libera los recursos del reproductor.
     * Debe llamarse cuando el reproductor ya no se necesita.
     */
    fun release()
}
