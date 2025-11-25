package com.musicmusic.ui.screens.player

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.musicmusic.data.preferences.UserPreferences
import com.musicmusic.domain.audio.AudioPlayer
import com.musicmusic.domain.model.PlaybackState
import com.musicmusic.domain.model.RepeatMode
import com.musicmusic.domain.model.Song
import com.musicmusic.domain.model.EqualizerSettings
import com.musicmusic.domain.model.EqualizerPreset
import com.musicmusic.domain.model.VolumeNormalizerSettings
import com.musicmusic.utils.TimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.min

/**
 * Implementación Desktop de PlayerViewModel con persistencia de preferencias.
 * 
 * Observa cambios en volume, shuffle y repeat mode para persistirlos automáticamente.
 * 
 * Cada instancia tiene su propio CoroutineScope que se limpia al destruir el ViewModel.
 */
actual class PlayerViewModel(
    private val audioPlayer: AudioPlayer,
    private val userPreferences: UserPreferences,
    private val musicRepository: com.musicmusic.data.repository.MusicRepository? = null
) {
    
    // ViewModel-specific coroutine scope
    private val viewModelScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    
    // ========== Estados observables desde AudioPlayer ==========
    
    actual val playbackState: StateFlow<PlaybackState> = audioPlayer.playbackState
        .stateIn(viewModelScope, SharingStarted.Eagerly, PlaybackState.STOPPED)
    
    // Combinar currentSong del audioPlayer con el estado de favoritos para reactividad
    actual val currentSong: StateFlow<Song?> = if (musicRepository != null) {
        combine(
            audioPlayer.currentSong,
            musicRepository.allSongs
        ) { song, allSongs ->
            song?.let { currentSong ->
                // Encontrar la canción actualizada en el repositorio para obtener el estado de favorito actual
                allSongs.find { it.id == currentSong.id } ?: currentSong
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    } else {
        audioPlayer.currentSong
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    }
    
    actual val currentPosition: StateFlow<Long> = audioPlayer.currentPosition
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0L)
    
    actual val duration: StateFlow<Long> = audioPlayer.duration
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0L)
    
    actual val volume: StateFlow<Float> = audioPlayer.volume
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0.5f)
    
    actual val isShuffleEnabled: StateFlow<Boolean> = audioPlayer.isShuffleEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    
    actual val repeatMode: StateFlow<RepeatMode> = audioPlayer.repeatMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, RepeatMode.OFF)
    
    actual val queue: StateFlow<List<Song>> = audioPlayer.queue
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    
    actual val currentIndex: StateFlow<Int> = audioPlayer.currentIndex
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    
    // ========== Estados del ecualizador ==========
    
    private val _equalizerSettings = mutableStateOf(com.musicmusic.domain.model.EqualizerSettings())
    actual val equalizerSettings: com.musicmusic.domain.model.EqualizerSettings
        get() = _equalizerSettings.value
    
    private val _availablePresets = mutableStateOf<List<String>>(emptyList())
    actual val availablePresets: List<String>
        get() = _availablePresets.value
    
    private val _equalizerBands = mutableStateOf<List<Float>>(emptyList())
    actual val equalizerBands: List<Float>
        get() = _equalizerBands.value
    
    // ========== Estados del normalizador de volumen ==========
    
    private val _volumeNormalizerSettings = mutableStateOf(com.musicmusic.domain.model.VolumeNormalizerSettings())
    actual val volumeNormalizerSettings: com.musicmusic.domain.model.VolumeNormalizerSettings
        get() = _volumeNormalizerSettings.value
    
    // ========== Estado local del UI ==========
    
    private var _isVolumeSliderVisible by mutableStateOf(false)
    actual val isVolumeSliderVisible: Boolean
        get() = _isVolumeSliderVisible
    
    private var _isSeeking by mutableStateOf(false)
    actual val isSeeking: Boolean
        get() = _isSeeking
    
    private var _seekPosition by mutableStateOf(0L)
    actual val seekPosition: Long
        get() = _seekPosition
    
    // ========== Inicialización y observadores ==========
    
    init {
        // Observar cambios en volumen y persistir
        viewModelScope.launch {
            volume.collect { newVolume ->
                userPreferences.setVolume(newVolume)
            }
        }
        
        // Observar cambios en shuffle y persistir
        viewModelScope.launch {
            isShuffleEnabled.collect { enabled ->
                userPreferences.setShuffleEnabled(enabled)
            }
        }
        
        // Observar cambios en repeat mode y persistir
        viewModelScope.launch {
            repeatMode.collect { mode ->
                userPreferences.setRepeatMode(mode.ordinal)
            }
        }
        
        // Restaurar volumen inicial desde preferencias
        viewModelScope.launch {
            userPreferences.volume.first().let { savedVolume ->
                audioPlayer.setVolume(savedVolume)
            }
        }
        
        // Restaurar shuffle inicial desde preferencias
        viewModelScope.launch {
            userPreferences.shuffleEnabled.first().let { enabled ->
                audioPlayer.setShuffle(enabled)
            }
        }
        
        // Restaurar repeat mode inicial desde preferencias
        viewModelScope.launch {
            userPreferences.repeatMode.first().let { modeOrdinal ->
                val mode = RepeatMode.values().getOrElse(modeOrdinal) { RepeatMode.OFF }
                audioPlayer.setRepeatMode(mode)
            }
        }
        
        // Cargar configuración del ecualizador
        loadEqualizerSettings()
    }
    
    // ========== Acciones de reproducción ==========
    
    actual fun playSong(song: Song) {
        viewModelScope.launch {
            audioPlayer.play(song)
        }
    }
    
    actual fun playQueue(songs: List<Song>, startIndex: Int) {
        viewModelScope.launch {
            audioPlayer.playQueue(songs, startIndex)
        }
    }
    
    actual fun togglePlayPause() {
        viewModelScope.launch {
            audioPlayer.togglePlayPause()
        }
    }
    
    actual fun pause() {
        viewModelScope.launch {
            audioPlayer.pause()
        }
    }
    
    actual fun resume() {
        viewModelScope.launch {
            audioPlayer.resume()
        }
    }
    
    actual fun stop() {
        viewModelScope.launch {
            audioPlayer.stop()
        }
    }
    
    actual fun next() {
        viewModelScope.launch {
            audioPlayer.next()
        }
    }
    
    actual fun previous() {
        viewModelScope.launch {
            audioPlayer.previous()
        }
    }
    
    // ========== Control de posición ==========
    
    actual fun seekTo(positionMs: Long) {
        viewModelScope.launch {
            audioPlayer.seekTo(positionMs)
        }
    }
    
    actual fun startSeeking(position: Long) {
        _isSeeking = true
        _seekPosition = position
    }
    
    actual fun updateSeekPosition(position: Long) {
        if (_isSeeking) {
            _seekPosition = position
        }
    }
    
    actual fun endSeeking() {
        if (_isSeeking) {
            seekTo(_seekPosition)
            _isSeeking = false
        }
    }
    
    actual fun seekForward(seconds: Int) {
        viewModelScope.launch {
            audioPlayer.seekForward(seconds)
        }
    }
    
    actual fun seekBackward(seconds: Int) {
        viewModelScope.launch {
            audioPlayer.seekBackward(seconds)
        }
    }
    
    actual fun seekBy(milliseconds: Long) {
        viewModelScope.launch {
            if (milliseconds > 0) {
                audioPlayer.seekForward((milliseconds / 1000).toInt())
            } else {
                audioPlayer.seekBackward(((-milliseconds) / 1000).toInt())
            }
        }
    }
    
    // ========== Control de volumen ==========
    
    actual fun setVolume(volume: Float) {
        viewModelScope.launch {
            audioPlayer.setVolume(volume)
        }
    }
    
    actual fun increaseVolume() {
        viewModelScope.launch {
            audioPlayer.increaseVolume()
        }
    }
    
    actual fun decreaseVolume() {
        viewModelScope.launch {
            audioPlayer.decreaseVolume()
        }
    }
    
    private var isMuted by mutableStateOf(false)
    private var volumeBeforeMute = 0.5f
    
    actual fun toggleMute() {
        viewModelScope.launch {
            if (isMuted) {
                // Unmute: restaurar volumen anterior
                audioPlayer.setMute(false)
                // Solo restaurar si el volumen guardado es diferente del actual
                if (volumeBeforeMute != volume.value) {
                    audioPlayer.setVolume(volumeBeforeMute)
                }
                isMuted = false
            } else {
                // Mute: guardar volumen actual y silenciar
                volumeBeforeMute = volume.value
                audioPlayer.setMute(true)
                isMuted = true
            }
        }
    }
    
    actual fun toggleVolumeSlider() {
        _isVolumeSliderVisible = !_isVolumeSliderVisible
    }
    
    actual fun hideVolumeSlider() {
        _isVolumeSliderVisible = false
    }
    
    // ========== Control de modos ==========
    
    actual fun toggleShuffle() {
        viewModelScope.launch {
            audioPlayer.setShuffle(!isShuffleEnabled.value)
        }
    }
    
    actual fun toggleRepeatMode() {
        viewModelScope.launch {
            val nextMode = when (repeatMode.value) {
                RepeatMode.OFF -> RepeatMode.ALL
                RepeatMode.ALL -> RepeatMode.ONE
                RepeatMode.ONE -> RepeatMode.OFF
            }
            audioPlayer.setRepeatMode(nextMode)
        }
    }
    
    actual fun setRepeatMode(mode: RepeatMode) {
        viewModelScope.launch {
            audioPlayer.setRepeatMode(mode)
        }
    }
    
    // ========== Control de cola ==========
    
    actual fun getQueue(): List<Song> {
        return audioPlayer.getQueue()
    }
    
    actual fun addToQueue(song: Song) {
        viewModelScope.launch {
            audioPlayer.addToQueue(song)
        }
    }
    
    actual fun addToQueue(songs: List<Song>) {
        viewModelScope.launch {
            audioPlayer.addToQueue(songs)
        }
    }
    
    actual fun clearQueue() {
        viewModelScope.launch {
            audioPlayer.clearQueue()
        }
    }
    
    actual fun playAtIndex(index: Int) {
        viewModelScope.launch {
            audioPlayer.playAtIndex(index)
        }
    }
    
    actual fun removeFromQueue(index: Int) {
        viewModelScope.launch {
            audioPlayer.removeFromQueue(index)
        }
    }
    
    // ========== Utilidades ==========
    
    actual fun getFormattedPosition(): String {
        val position = if (_isSeeking) _seekPosition else currentPosition.value
        return formatTime(position)
    }
    
    actual fun getFormattedDuration(): String {
        return formatTime(duration.value)
    }
    
    actual fun getProgress(): Float {
        val dur = duration.value
        if (dur == 0L) return 0f
        
        val position = if (_isSeeking) _seekPosition else currentPosition.value
        return (position.toFloat() / dur.toFloat()).coerceIn(0f, 1f)
    }
    
    private fun formatTime(timeMs: Long): String {
        return TimeUtils.formatDuration(timeMs)
    }

    // ========== Control de ecualizador ==========
    
    /**
     * Carga la configuración del ecualizador desde el reproductor.
     */
    private fun loadEqualizerSettings() {
        viewModelScope.launch {
            try {
                // Obtener configuración actual
                val settings = audioPlayer.getEqualizerSettings()
                _equalizerSettings.value = settings
                
                // Obtener presets disponibles
                val presets = audioPlayer.getAvailablePresets()
                _availablePresets.value = presets
                
                // Obtener bandas del ecualizador
                val bands = audioPlayer.getEqualizerBands()
                _equalizerBands.value = bands
            } catch (e: Exception) {
                // Manejar errores de carga
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Establece la configuración del ecualizador.
     */
    actual fun setEqualizerSettings(settings: EqualizerSettings) {
        viewModelScope.launch {
            try {
                audioPlayer.setEqualizerSettings(settings)
                _equalizerSettings.value = settings
            } catch (e: Exception) {
                // Manejar errores de configuración
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Aplica un preset predefinido del ecualizador.
     */
    actual fun applyEqualizerPreset(preset: EqualizerPreset) {
        viewModelScope.launch {
            try {
                audioPlayer.applyEqualizerPreset(preset)
                
                // Actualizar configuración local
                val newSettings = EqualizerSettings(
                    isEnabled = true,
                    preamp = preset.preamp,
                    bands = preset.bands
                )
                _equalizerSettings.value = newSettings
            } catch (e: Exception) {
                // Manejar errores de aplicación de preset
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Alterna el estado del ecualizador (activado/desactivado).
     */
    actual fun toggleEqualizer() {
        val currentSettings = _equalizerSettings.value
        val newSettings = currentSettings.copy(isEnabled = !currentSettings.isEnabled)
        setEqualizerSettings(newSettings)
    }
    
    /**
     * Establece el valor de preamplificación del ecualizador.
     */
    actual fun setEqualizerPreamp(preamp: Float) {
        val currentSettings = _equalizerSettings.value
        val newSettings = currentSettings.copy(preamp = preamp)
        setEqualizerSettings(newSettings)
    }
    
    /**
     * Establece el valor de ganancia para una banda específica del ecualizador.
     */
    actual fun setEqualizerBand(bandIndex: Int, gain: Float) {
        val currentSettings = _equalizerSettings.value
        val newBands = currentSettings.bands.toMutableList()
        
        // Asegurarse de que el índice sea válido
        if (bandIndex in newBands.indices) {
            newBands[bandIndex] = gain
            val newSettings = currentSettings.copy(bands = newBands)
            setEqualizerSettings(newSettings)
        }
    }
    
    // ========== Control de normalización de volumen ==========
    
    /**
     * Establece la configuración del normalizador de volumen.
     */
    actual fun setVolumeNormalizerSettings(settings: VolumeNormalizerSettings) {
        viewModelScope.launch {
            try {
                audioPlayer.setVolumeNormalizerSettings(settings)
                _volumeNormalizerSettings.value = settings
            } catch (e: Exception) {
                // Manejar errores de configuración
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Alterna el estado del normalizador de volumen (activado/desactivado).
     */
    actual fun toggleVolumeNormalizer() {
        val currentSettings = _volumeNormalizerSettings.value
        val newSettings = currentSettings.copy(isEnabled = !currentSettings.isEnabled)
        setVolumeNormalizerSettings(newSettings)
    }
    
    /**
     * Establece el nivel del normalizador de volumen.
     */
    actual fun setVolumeNormalizerLevel(level: Float) {
        val currentSettings = _volumeNormalizerSettings.value
        val newSettings = currentSettings.copy(level = level.coerceIn(0f, 1f))
        setVolumeNormalizerSettings(newSettings)
    }
    
    // ========== Favorites ==========

    actual fun toggleFavorite(songId: String) {
        musicRepository?.toggleFavorite(songId)
    }

    // ========== Lifecycle ==========
    
    actual fun onCleared() {
        // Guardar última canción reproducida y su posición
        viewModelScope.launch {
            currentSong.value?.let { song ->
                userPreferences.setLastPlayedSong(song.id, currentPosition.value)
            }
        }
        
        // Cancelar todas las coroutines del ViewModel
        viewModelScope.cancel()
        
        // Liberar recursos del AudioPlayer
        audioPlayer.release()
    }
}
