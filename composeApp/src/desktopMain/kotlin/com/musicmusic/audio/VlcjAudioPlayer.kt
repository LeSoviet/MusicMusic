package com.musicmusic.audio

import com.musicmusic.domain.audio.AudioPlayer
import com.musicmusic.domain.model.PlaybackState
import com.musicmusic.domain.model.RepeatMode
import com.musicmusic.domain.model.Song
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
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
 * Basado en las mejores prácticas de VLCJ 4.x
 */
class VlcjAudioPlayer(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) : AudioPlayer {
    
    // Factory para crear instancias de MediaPlayer
    private val mediaPlayerFactory: MediaPlayerFactory = MediaPlayerFactory()
    
    // MediaPlayer de VLCJ para reproducción de audio
    private val mediaPlayer: MediaPlayer = mediaPlayerFactory.mediaPlayers().newMediaPlayer()
    
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
        println("🎵 togglePlayPause - Estado actual: ${_playbackState.value}, isPlaying: ${mediaPlayer.status().isPlaying}")
        
        // Usar el estado real del reproductor de VLC en lugar del estado interno
        val isCurrentlyPlaying = mediaPlayer.status().isPlaying
        
        if (isCurrentlyPlaying) {
            println("⏸️ Pausando reproducción")
            mediaPlayer.controls().pause()
        } else {
            // Si no está reproduciendo, verificar si hay contenido para reproducir
            when (_playbackState.value) {
                PlaybackState.PAUSED, PlaybackState.BUFFERING -> {
                    println("▶️ Reanudando reproducción")
                    mediaPlayer.controls().play()
                }
                PlaybackState.STOPPED -> {
                    println("🆕 Iniciando reproducción desde STOPPED")
                    if (_currentSong.value != null) {
                        playCurrentSong()
                    } else if (queueList.isNotEmpty()) {
                        playAtIndex(0)
                    }
                }
                else -> {
                    println("⚠️ Intentando reproducir desde estado: ${_playbackState.value}")
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
        println("🔇 setMute: $mute")
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
    
    // ========== Lifecycle ==========
    
    override fun release() {
        stopPositionUpdates()
        scope.cancel()
        mediaPlayer.release()
        mediaPlayerFactory.release()
    }
}
