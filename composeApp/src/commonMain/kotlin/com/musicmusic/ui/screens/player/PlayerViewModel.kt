package com.musicmusic.ui.screens.player

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.musicmusic.domain.audio.AudioPlayer
import com.musicmusic.domain.model.PlaybackState
import com.musicmusic.domain.model.RepeatMode
import com.musicmusic.domain.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de reproducción.
 * 
 * Gestiona el estado de reproducción y proporciona acciones
 * para controlar el reproductor de audio.
 */
class PlayerViewModel(
    private val audioPlayer: AudioPlayer,
    private val viewModelScope: CoroutineScope
) {
    
    // ========== Estados observables desde AudioPlayer ==========
    
    val playbackState: StateFlow<PlaybackState> = audioPlayer.playbackState
        .stateIn(viewModelScope, SharingStarted.Eagerly, PlaybackState.STOPPED)
    
    val currentSong: StateFlow<Song?> = audioPlayer.currentSong
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    
    val currentPosition: StateFlow<Long> = audioPlayer.currentPosition
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0L)
    
    val duration: StateFlow<Long> = audioPlayer.duration
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0L)
    
    val volume: StateFlow<Float> = audioPlayer.volume
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0.5f)
    
    val isShuffleEnabled: StateFlow<Boolean> = audioPlayer.isShuffleEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    
    val repeatMode: StateFlow<RepeatMode> = audioPlayer.repeatMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, RepeatMode.OFF)
    
    val queue: StateFlow<List<Song>> = audioPlayer.queue
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    
    val currentIndex: StateFlow<Int> = audioPlayer.currentIndex
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    
    // ========== Estado local del UI ==========
    
    var isVolumeSliderVisible by mutableStateOf(false)
        private set
    
    var isSeeking by mutableStateOf(false)
        private set
    
    var seekPosition by mutableStateOf(0L)
        private set
    
    // ========== Acciones de reproducción ==========
    
    fun playSong(song: Song) {
        viewModelScope.launch {
            audioPlayer.play(song)
        }
    }
    
    fun playQueue(songs: List<Song>, startIndex: Int = 0) {
        viewModelScope.launch {
            audioPlayer.playQueue(songs, startIndex)
        }
    }
    
    fun togglePlayPause() {
        viewModelScope.launch {
            audioPlayer.togglePlayPause()
        }
    }
    
    fun pause() {
        viewModelScope.launch {
            audioPlayer.pause()
        }
    }
    
    fun resume() {
        viewModelScope.launch {
            audioPlayer.resume()
        }
    }
    
    fun stop() {
        viewModelScope.launch {
            audioPlayer.stop()
        }
    }
    
    fun next() {
        viewModelScope.launch {
            audioPlayer.next()
        }
    }
    
    fun previous() {
        viewModelScope.launch {
            audioPlayer.previous()
        }
    }
    
    // ========== Control de posición ==========
    
    fun seekTo(positionMs: Long) {
        viewModelScope.launch {
            audioPlayer.seekTo(positionMs)
        }
    }
    
    fun startSeeking(position: Long) {
        isSeeking = true
        seekPosition = position
    }
    
    fun updateSeekPosition(position: Long) {
        if (isSeeking) {
            seekPosition = position
        }
    }
    
    fun endSeeking() {
        if (isSeeking) {
            seekTo(seekPosition)
            isSeeking = false
        }
    }
    
    fun seekForward(seconds: Int = 10) {
        viewModelScope.launch {
            audioPlayer.seekForward(seconds)
        }
    }
    
    fun seekBackward(seconds: Int = 10) {
        viewModelScope.launch {
            audioPlayer.seekBackward(seconds)
        }
    }
    
    /**
     * Seek relativo (positivo = adelante, negativo = atrás)
     * @param milliseconds Cantidad de milisegundos a avanzar/retroceder
     */
    fun seekBy(milliseconds: Long) {
        viewModelScope.launch {
            if (milliseconds > 0) {
                audioPlayer.seekForward((milliseconds / 1000).toInt())
            } else {
                audioPlayer.seekBackward(((-milliseconds) / 1000).toInt())
            }
        }
    }
    
    // ========== Control de volumen ==========
    
    fun setVolume(volume: Float) {
        viewModelScope.launch {
            audioPlayer.setVolume(volume)
        }
    }
    
    fun increaseVolume() {
        viewModelScope.launch {
            audioPlayer.increaseVolume()
        }
    }
    
    fun decreaseVolume() {
        viewModelScope.launch {
            audioPlayer.decreaseVolume()
        }
    }
    
    fun toggleMute() {
        viewModelScope.launch {
            val isMuted = volume.value == 0f
            audioPlayer.setMute(!isMuted)
        }
    }
    
    fun toggleVolumeSlider() {
        isVolumeSliderVisible = !isVolumeSliderVisible
    }
    
    fun hideVolumeSlider() {
        isVolumeSliderVisible = false
    }
    
    // ========== Control de modos ==========
    
    fun toggleShuffle() {
        viewModelScope.launch {
            audioPlayer.setShuffle(!isShuffleEnabled.value)
        }
    }
    
    fun toggleRepeatMode() {
        viewModelScope.launch {
            val nextMode = when (repeatMode.value) {
                RepeatMode.OFF -> RepeatMode.ALL
                RepeatMode.ALL -> RepeatMode.ONE
                RepeatMode.ONE -> RepeatMode.OFF
            }
            audioPlayer.setRepeatMode(nextMode)
        }
    }
    
    fun setRepeatMode(mode: RepeatMode) {
        viewModelScope.launch {
            audioPlayer.setRepeatMode(mode)
        }
    }
    
    // ========== Control de cola ==========
    
    fun getQueue(): List<Song> {
        return audioPlayer.getQueue()
    }
    
    fun addToQueue(song: Song) {
        viewModelScope.launch {
            audioPlayer.addToQueue(song)
        }
    }
    
    fun addToQueue(songs: List<Song>) {
        viewModelScope.launch {
            audioPlayer.addToQueue(songs)
        }
    }
    
    fun clearQueue() {
        viewModelScope.launch {
            audioPlayer.clearQueue()
        }
    }
    
    fun playAtIndex(index: Int) {
        viewModelScope.launch {
            audioPlayer.playAtIndex(index)
        }
    }
    
    fun removeFromQueue(index: Int) {
        viewModelScope.launch {
            audioPlayer.removeFromQueue(index)
        }
    }
    
    // ========== Utilidades ==========
    
    /**
     * Retorna la posición actual formateada como "mm:ss"
     */
    fun getFormattedPosition(): String {
        val position = if (isSeeking) seekPosition else currentPosition.value
        return formatTime(position)
    }
    
    /**
     * Retorna la duración formateada como "mm:ss"
     */
    fun getFormattedDuration(): String {
        return formatTime(duration.value)
    }
    
    /**
     * Retorna el progreso de reproducción (0.0 a 1.0)
     */
    fun getProgress(): Float {
        val dur = duration.value
        if (dur == 0L) return 0f
        
        val position = if (isSeeking) seekPosition else currentPosition.value
        return (position.toFloat() / dur.toFloat()).coerceIn(0f, 1f)
    }
    
    private fun formatTime(timeMs: Long): String {
        val totalSeconds = timeMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%d:%02d".format(minutes, seconds)
    }
    
    // ========== Lifecycle ==========
    
    fun onCleared() {
        audioPlayer.release()
    }
}
