package com.musicmusic.audio

import com.musicmusic.domain.audio.AudioPlayer
import com.musicmusic.domain.error.AppError
import com.musicmusic.domain.error.ErrorHandler
import com.musicmusic.domain.model.PlaybackState
import com.musicmusic.domain.model.RepeatMode
import com.musicmusic.domain.model.Song
import com.musicmusic.domain.model.EqualizerSettings
import com.musicmusic.domain.model.EqualizerPreset
import com.musicmusic.domain.model.VolumeNormalizerSettings
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import uk.co.caprica.vlcj.player.base.Equalizer
import java.io.File

/**
 * Implementación de AudioPlayer usando VLCJ.
 * 
 * VLCJ es un wrapper de libVLC para Java que proporciona:
 * - Soporte para todos los formatos de audio (MP3, FLAC, OGG, AAC, etc.)
 * - Streaming de audio desde URLs
 * - Control completo de reproducción
 * - Ecualizador y efectos de audio
 * 
 * Basado en las mejores prácticas de VLCJ 4.x.
 * 
 * Tiene su propio CoroutineScope para operaciones asíncronas.
 */
class VlcjAudioPlayer(
    private val errorHandler: ErrorHandler? = null
) : AudioPlayer {
    
    // AudioPlayer-specific coroutine scope
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    // Factory para crear instancias de MediaPlayer
    private val mediaPlayerFactory: MediaPlayerFactory = MediaPlayerFactory()
    
    // MediaPlayer de VLCJ para reproducción de audio
    private val mediaPlayer: MediaPlayer = mediaPlayerFactory.mediaPlayers().newMediaPlayer()
    
    // Ecualizador de audio
    private var equalizer: Equalizer? = null
    
    // Configuración del ecualizador
    private var equalizerSettings = com.musicmusic.domain.model.EqualizerSettings()
    
    // ========== Estados internos ==========
    
    private val _playbackState = MutableStateFlow(PlaybackState.STOPPED)
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()
    
    private val _currentSong = MutableStateFlow<Song?>(null)
    override val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    override val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    override val duration: StateFlow<Long> = _duration.asStateFlow()
    
    private val _volume = MutableStateFlow(0.5f)
    override val volume: StateFlow<Float> = _volume.asStateFlow()
    
    private val _isShuffleEnabled = MutableStateFlow(false)
    override val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()
    
    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    override val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()
    
    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    override val queue: StateFlow<List<Song>> = _queue.asStateFlow()
    
    private val _currentIndex = MutableStateFlow(0)
    override val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()
    
    // Cola de reproducción interna
    private val queueList = mutableListOf<Song>()
    private var currentQueueIndex = -1

    // Orden original de la cola (para restaurar al desactivar shuffle)
    private val originalQueueOrder = mutableListOf<Song>()
    
    // Job para actualizar la posición actual
    private var positionUpdateJob: Job? = null
    
    init {
        setupMediaPlayerEvents()
        initializeVolume()
    }
    
    /**
     * Configura los listeners de eventos del MediaPlayer de VLCJ.
     */
    private fun setupMediaPlayerEvents() {
        mediaPlayer.events().addMediaPlayerEventListener(object : MediaPlayerEventAdapter() {
            
            override fun playing(mediaPlayer: MediaPlayer) {
                _playbackState.value = PlaybackState.PLAYING
                startPositionUpdates()
            }
            
            override fun paused(mediaPlayer: MediaPlayer) {
                _playbackState.value = PlaybackState.PAUSED
                stopPositionUpdates()
            }
            
            override fun stopped(mediaPlayer: MediaPlayer) {
                _playbackState.value = PlaybackState.STOPPED
                _currentPosition.value = 0L
                stopPositionUpdates()
            }
            
            override fun finished(mediaPlayer: MediaPlayer) {
                scope.launch {
                    handlePlaybackFinished()
                }
            }
            
            override fun error(mediaPlayer: MediaPlayer) {
                _playbackState.value = PlaybackState.ERROR
                stopPositionUpdates()
                
                // Notificar el error
                scope.launch {
                    val song = _currentSong.value
                    errorHandler?.handleError(
                        AppError.PlaybackError(
                            song?.filePath ?: "unknown",
                            "Media player error during playback"
                        )
                    )
                }
            }
            
            override fun buffering(mediaPlayer: MediaPlayer, newCache: Float) {
                if (newCache < 100f) {
                    _playbackState.value = PlaybackState.BUFFERING
                }
            }
            
            override fun lengthChanged(mediaPlayer: MediaPlayer, newLength: Long) {
                _duration.value = newLength
            }
            
            override fun timeChanged(mediaPlayer: MediaPlayer, newTime: Long) {
                _currentPosition.value = newTime
            }
        })
    }
    
    /**
     * Inicializa el volumen del reproductor.
     */
    private fun initializeVolume() {
        mediaPlayer.audio().setVolume((_volume.value * 100).toInt())
    }
    
    // ========== Implementación de controles de reproducción ==========
    
    override suspend fun play(song: Song) = withContext(Dispatchers.IO) {
        playQueue(listOf(song), 0)
    }
    
    override suspend fun playQueue(songs: List<Song>, startIndex: Int) = withContext(Dispatchers.IO) {
        if (songs.isEmpty()) return@withContext

        queueList.clear()
        queueList.addAll(songs)
        currentQueueIndex = startIndex.coerceIn(0, songs.size - 1)

        // Guardar orden original
        originalQueueOrder.clear()
        originalQueueOrder.addAll(songs)

        // Si shuffle está activado, mezclar inmediatamente
        if (_isShuffleEnabled.value) {
            shuffleQueue()
        }

        _queue.value = queueList.toList()
        _currentIndex.value = currentQueueIndex

        playCurrentSong()
    }
    
    override suspend fun pause() = withContext(Dispatchers.IO) {
        if (mediaPlayer.status().isPlaying) {
            mediaPlayer.controls().pause()
        }
    }
    
    override suspend fun resume() = withContext(Dispatchers.IO) {
        if (!mediaPlayer.status().isPlaying && _playbackState.value == PlaybackState.PAUSED) {
            mediaPlayer.controls().play()
        }
    }
    
    override suspend fun togglePlayPause() = withContext(Dispatchers.IO) {
        // Usar el estado real del reproductor de VLC en lugar del estado interno
        val isCurrentlyPlaying = mediaPlayer.status().isPlaying
        
        if (isCurrentlyPlaying) {
            mediaPlayer.controls().pause()
        } else {
            // Si no está reproduciendo, verificar si hay contenido para reproducir
            when (_playbackState.value) {
                PlaybackState.PAUSED, PlaybackState.BUFFERING -> {
                    mediaPlayer.controls().play()
                }
                PlaybackState.STOPPED -> {
                    if (_currentSong.value != null) {
                        playCurrentSong()
                    } else if (queueList.isNotEmpty()) {
                        playAtIndex(0)
                    }
                }
                else -> {
                    mediaPlayer.controls().play()
                }
            }
        }
    }
    
    override suspend fun stop() = withContext(Dispatchers.IO) {
        mediaPlayer.controls().stop()
        _currentSong.value = null
    }
    
    override suspend fun next(): Boolean {
        if (currentQueueIndex >= queueList.size - 1) {
            // Si estamos al final de la cola
            return when (_repeatMode.value) {
                RepeatMode.ALL -> {
                    currentQueueIndex = 0
                    _currentIndex.value = currentQueueIndex
                    playCurrentSong()
                    true
                }
                else -> false
            }
        }
        
        currentQueueIndex++
        _currentIndex.value = currentQueueIndex
        playCurrentSong()
        return true
    }
    
    override suspend fun previous(): Boolean {
        // Si llevamos más de 3 segundos en la canción, volver al inicio
        if (_currentPosition.value > 3000) {
            seekTo(0)
            return true
        }
        
        if (currentQueueIndex <= 0) {
            return false
        }
        
        currentQueueIndex--
        _currentIndex.value = currentQueueIndex
        playCurrentSong()
        return true
    }
    
    // ========== Control de posición ==========
    
    override suspend fun seekTo(positionMs: Long) = withContext(Dispatchers.IO) {
        if (_currentSong.value != null) {
            mediaPlayer.controls().setTime(positionMs)
        }
    }
    
    override suspend fun seekForward(seconds: Int) {
        val newPosition = (_currentPosition.value + (seconds * 1000))
            .coerceAtMost(_duration.value)
        seekTo(newPosition)
    }
    
    override suspend fun seekBackward(seconds: Int) {
        val newPosition = (_currentPosition.value - (seconds * 1000))
            .coerceAtLeast(0)
        seekTo(newPosition)
    }
    
    // ========== Control de volumen ==========
    
    override suspend fun setVolume(volume: Float) {
        withContext(Dispatchers.IO) {
            val clampedVolume = volume.coerceIn(0f, 1f)
            _volume.value = clampedVolume
            mediaPlayer.audio().setVolume((clampedVolume * 100).toInt())
        }
    }
    
    override suspend fun increaseVolume(amount: Float) {
        setVolume(_volume.value + amount)
    }
    
    override suspend fun decreaseVolume(amount: Float) {
        setVolume(_volume.value - amount)
    }
    
    override suspend fun setMute(mute: Boolean) = withContext(Dispatchers.IO) {
        mediaPlayer.audio().isMute = mute
    }
    
    // ========== Control de cola y modos ==========
    
    override suspend fun setShuffle(enabled: Boolean) {
        _isShuffleEnabled.value = enabled
        if (enabled) {
            // Guardar orden original si no lo hemos hecho
            if (originalQueueOrder.isEmpty() && queueList.isNotEmpty()) {
                originalQueueOrder.clear()
                originalQueueOrder.addAll(queueList)
            }
            shuffleQueue()
        } else {
            // Restaurar orden original
            restoreOriginalOrder()
        }
    }
    
    override suspend fun setRepeatMode(mode: RepeatMode) {
        _repeatMode.value = mode
    }
    
    override fun getQueue(): List<Song> = queueList.toList()
    
    override suspend fun addToQueue(song: Song) {
        queueList.add(song)
        _queue.value = queueList.toList()
    }
    
    override suspend fun addToQueue(songs: List<Song>) {
        queueList.addAll(songs)
        _queue.value = queueList.toList()
    }
    
    override suspend fun clearQueue() {
        stop()
        queueList.clear()
        currentQueueIndex = -1
        _queue.value = emptyList()
        _currentIndex.value = 0
    }
    
    override suspend fun playAtIndex(index: Int) = withContext(Dispatchers.IO) {
        if (index in queueList.indices) {
            currentQueueIndex = index
            _currentIndex.value = currentQueueIndex
            playCurrentSong()
        }
    }
    
    override suspend fun removeFromQueue(index: Int) {
        if (index in queueList.indices) {
            queueList.removeAt(index)
            _queue.value = queueList.toList()
            
            // Ajustar índice actual si es necesario
            if (index < currentQueueIndex) {
                currentQueueIndex--
                _currentIndex.value = currentQueueIndex
            } else if (index == currentQueueIndex) {
                // Si eliminamos la canción actual, reproducir la siguiente
                if (queueList.isNotEmpty()) {
                    currentQueueIndex = currentQueueIndex.coerceIn(0, queueList.size - 1)
                    _currentIndex.value = currentQueueIndex
                    playCurrentSong()
                } else {
                    stop()
                }
            }
        }
    }
    
    // ========== Métodos privados auxiliares ==========
    
    /**
     * Reproduce la canción actual según currentQueueIndex.
     */
    private suspend fun playCurrentSong() = withContext(Dispatchers.IO) {
        if (currentQueueIndex !in queueList.indices) return@withContext
        
        val song = queueList[currentQueueIndex]
        _currentSong.value = song
        _currentIndex.value = currentQueueIndex
        
        val uri = song.getPlaybackUri()
        
        // Verificar que el archivo existe (si es local)
        if (song.isLocal()) {
            val file = File(uri)
            if (!file.exists()) {
                _playbackState.value = PlaybackState.ERROR
                errorHandler?.handleError(
                    AppError.FileNotFound(uri, "Audio file does not exist")
                )
                return@withContext
            }
            
            if (!file.canRead()) {
                _playbackState.value = PlaybackState.ERROR
                errorHandler?.handleError(
                    AppError.PermissionError(uri, "Cannot read audio file")
                )
                return@withContext
            }
        }
        
        // Preparar y reproducir el medio
        mediaPlayer.media().prepare(uri)
        mediaPlayer.controls().play()
    }
    
    /**
     * Maneja el fin de reproducción de una canción.
     */
    private suspend fun handlePlaybackFinished() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                // Repetir la canción actual
                seekTo(0)
                withContext(Dispatchers.IO) {
                    mediaPlayer.controls().play()
                }
            }
            RepeatMode.ALL, RepeatMode.OFF -> {
                // Intentar siguiente canción
                if (!next()) {
                    // No hay más canciones
                    stop()
                }
            }
        }
    }
    
    /**
     * Mezcla la cola de reproducción (preservando la canción actual).
     */
    private fun shuffleQueue() {
        if (queueList.isEmpty()) return

        val currentSong = if (currentQueueIndex in queueList.indices) {
            queueList[currentQueueIndex]
        } else null

        queueList.shuffle()

        // Volver a poner la canción actual al inicio
        currentSong?.let { song ->
            queueList.remove(song)
            queueList.add(0, song)
            currentQueueIndex = 0
            _currentIndex.value = 0
        }

        _queue.value = queueList.toList()
    }

    /**
     * Restaura el orden original de la cola (preservando la canción actual).
     */
    private fun restoreOriginalOrder() {
        if (originalQueueOrder.isEmpty()) return

        val currentSong = if (currentQueueIndex in queueList.indices) {
            queueList[currentQueueIndex]
        } else null

        // Restaurar orden original
        queueList.clear()
        queueList.addAll(originalQueueOrder)

        // Encontrar la canción actual en el orden original
        currentSong?.let { song ->
            val newIndex = queueList.indexOf(song)
            if (newIndex >= 0) {
                currentQueueIndex = newIndex
                _currentIndex.value = newIndex
            }
        }

        _queue.value = queueList.toList()
    }
    
    /**
     * Inicia la actualización periódica de la posición.
     */
    private fun startPositionUpdates() {
        stopPositionUpdates()
        positionUpdateJob = scope.launch {
            while (isActive) {
                _currentPosition.value = mediaPlayer.status().time()
                delay(100) // Actualizar cada 100ms
            }
        }
    }
    
    /**
     * Detiene la actualización de la posición.
     */
    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }
    
    // ========== Control de ecualizador ==========
    
    /**
     * Establece la configuración del ecualizador.
     * 
     * @param settings Configuración del ecualizador
     */
    override suspend fun setEqualizerSettings(settings: EqualizerSettings) = withContext(Dispatchers.IO) {
        equalizerSettings = settings
        
        if (settings.isEnabled) {
            // Crear o actualizar el ecualizador
            if (equalizer == null) {
                equalizer = mediaPlayerFactory.equalizer().newEqualizer()
            }
            
            // Aplicar preamplificación
            equalizer?.setPreamp(settings.preamp)
            
            // Aplicar ganancias de las bandas
            val bandCount = equalizer?.bandCount() ?: 0
            for (i in 0 until minOf(bandCount, settings.bands.size)) {
                equalizer?.setAmp(i, settings.bands[i])
            }
            
            // Aplicar el ecualizador al reproductor
            mediaPlayer.audio().setEqualizer(equalizer)
        } else {
            // Desactivar el ecualizador
            mediaPlayer.audio().setEqualizer(null)
            equalizer = null
        }
    }
    
    /**
     * Obtiene la configuración actual del ecualizador.
     * 
     * @return Configuración actual del ecualizador
     */
    override fun getEqualizerSettings(): EqualizerSettings = equalizerSettings
    
    /**
     * Aplica un preset predefinido del ecualizador.
     * 
     * @param preset Preset a aplicar
     */
    override suspend fun applyEqualizerPreset(preset: EqualizerPreset) = withContext(Dispatchers.IO) {
        val settings = EqualizerSettings(
            isEnabled = true,
            preamp = preset.preamp,
            bands = preset.bands
        )
        setEqualizerSettings(settings)
    }
    
    /**
     * Obtiene la lista de presets disponibles.
     * 
     * @return Lista de nombres de presets
     */
    override fun getAvailablePresets(): List<String> {
        return try {
            mediaPlayerFactory.equalizer().presets()
        } catch (e: Exception) {
            listOf("Flat", "Classical", "Club", "Dance", "Full Bass", "Full Treble")
        }
    }
    
    /**
     * Obtiene las frecuencias de las bandas del ecualizador.
     * 
     * @return Lista de frecuencias en Hz
     */
    override fun getEqualizerBands(): List<Float> {
        return try {
            mediaPlayerFactory.equalizer().bands()
        } catch (e: Exception) {
            listOf(31.25f, 62.5f, 125f, 250f, 500f, 1000f, 2000f, 4000f, 8000f, 16000f)
        }
    }
    
    // ========== Control de normalización de volumen ==========
    
    /**
     * Establece la configuración del normalizador de volumen.
     * 
     * @param settings Configuración del normalizador
     */
    override suspend fun setVolumeNormalizerSettings(settings: VolumeNormalizerSettings) = withContext(Dispatchers.IO) {
        // La normalización de volumen en VLCJ se puede implementar a través de filtros
        // Por ahora, solo almacenamos la configuración
        // TODO: Implementar normalización de volumen real
    }
    
    /**
     * Obtiene la configuración actual del normalizador de volumen.
     * 
     * @return Configuración actual del normalizador
     */
    override fun getVolumeNormalizerSettings(): VolumeNormalizerSettings {
        // TODO: Implementar obtención real de configuración
        return VolumeNormalizerSettings()
    }
    
    // ========== Lifecycle ==========
    
    override fun release() {
        stopPositionUpdates()
        scope.cancel()
        mediaPlayer.release()
        mediaPlayerFactory.release()
    }
}
