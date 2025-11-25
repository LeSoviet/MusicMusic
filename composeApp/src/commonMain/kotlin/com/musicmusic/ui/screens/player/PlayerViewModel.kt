package com.musicmusic.ui.screens.player

import com.musicmusic.domain.model.PlaybackState
import com.musicmusic.domain.model.RepeatMode
import com.musicmusic.domain.model.Song
import com.musicmusic.domain.model.EqualizerSettings
import com.musicmusic.domain.model.EqualizerPreset
import com.musicmusic.domain.model.VolumeNormalizerSettings
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel para la pantalla de reproducción.
 * 
 * Gestiona el estado de reproducción y proporciona acciones
 * para controlar el reproductor de audio.
 * 
 * Utiliza expect/actual para implementación específica de plataforma
 * que incluye persistencia de preferencias con DataStore.
 */
expect class PlayerViewModel {
    val playbackState: StateFlow<PlaybackState>
    val currentSong: StateFlow<Song?>
    val currentPosition: StateFlow<Long>
    val duration: StateFlow<Long>
    val volume: StateFlow<Float>
    val isShuffleEnabled: StateFlow<Boolean>
    val repeatMode: StateFlow<RepeatMode>
    val queue: StateFlow<List<Song>>
    val currentIndex: StateFlow<Int>
    
    val equalizerSettings: EqualizerSettings
    val availablePresets: List<String>
    val equalizerBands: List<Float>
    val volumeNormalizerSettings: VolumeNormalizerSettings
    
    val isVolumeSliderVisible: Boolean
    val isSeeking: Boolean
    val seekPosition: Long
    
    fun playSong(song: Song)
    fun playQueue(songs: List<Song>, startIndex: Int = 0)
    fun togglePlayPause()
    fun pause()
    fun resume()
    fun stop()
    fun next()
    fun previous()
    
    fun seekTo(positionMs: Long)
    fun startSeeking(position: Long)
    fun updateSeekPosition(position: Long)
    fun endSeeking()
    fun seekForward(seconds: Int = 10)
    fun seekBackward(seconds: Int = 10)
    fun seekBy(milliseconds: Long)
    
    fun setVolume(volume: Float)
    fun increaseVolume()
    fun decreaseVolume()
    fun toggleMute()
    fun toggleVolumeSlider()
    fun hideVolumeSlider()
    
    fun toggleShuffle()
    fun toggleRepeatMode()
    fun setRepeatMode(mode: RepeatMode)
    
    fun getQueue(): List<Song>
    fun addToQueue(song: Song)
    fun addToQueue(songs: List<Song>)
    fun clearQueue()
    fun playAtIndex(index: Int)
    fun removeFromQueue(index: Int)
    
    // ========== Control de ecualizador ==========
    fun setEqualizerSettings(settings: EqualizerSettings)
    fun applyEqualizerPreset(preset: EqualizerPreset)
    fun toggleEqualizer()
    fun setEqualizerPreamp(preamp: Float)
    fun setEqualizerBand(bandIndex: Int, gain: Float)
    
    // ========== Control de normalización de volumen ==========
    fun setVolumeNormalizerSettings(settings: VolumeNormalizerSettings)
    fun toggleVolumeNormalizer()
    fun setVolumeNormalizerLevel(level: Float)
    
    fun getFormattedPosition(): String
    fun getFormattedDuration(): String
    fun getProgress(): Float

    fun toggleFavorite(songId: String)

    fun onCleared()
}