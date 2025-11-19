package com.musicmusic.data.repository

import com.musicmusic.domain.model.Album
import com.musicmusic.domain.model.Artist
import com.musicmusic.domain.model.Song
import com.musicmusic.files.FileScanner
import com.musicmusic.files.MetadataReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Repositorio para gestionar la biblioteca de música.
 * 
 * Responsabilidades:
 * - Escanear directorios en busca de archivos de audio
 * - Extraer metadata de los archivos
 * - Organizar canciones por artista, álbum, género
 * - Proporcionar búsqueda y filtrado
 */
class MusicRepository(
    private val fileScanner: FileScanner,
    private val metadataReader: MetadataReader
) {
    
    private val _allSongs = MutableStateFlow<List<Song>>(emptyList())
    val allSongs: StateFlow<List<Song>> = _allSongs.asStateFlow()
    
    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()
    
    private val _artists = MutableStateFlow<List<Artist>>(emptyList())
    val artists: StateFlow<List<Artist>> = _artists.asStateFlow()
    
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()
    
    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress: StateFlow<Float> = _scanProgress.asStateFlow()
    
    /**
     * Escanea un directorio y carga las canciones en la biblioteca
     */
    suspend fun scanDirectory(directoryPath: String) = withContext(Dispatchers.IO) {
        _isScanning.value = true
        _scanProgress.value = 0f
        
        try {
            val directory = File(directoryPath)
            if (!directory.exists() || !directory.isDirectory) {
                throw IllegalArgumentException("Invalid directory: $directoryPath")
            }
            
            // Escanear archivos
            val audioFiles = fileScanner.findAudioFiles(directory)
            val totalFiles = audioFiles.size
            
            // Extraer metadata
            val songs = mutableListOf<Song>()
            for ((index, file) in audioFiles.withIndex()) {
                try {
                    val metadata = metadataReader.readMetadata(file) ?: continue
                    val song = Song(
                        id = file.absolutePath,
                        title = metadata.title ?: file.nameWithoutExtension,
                        artist = metadata.artist ?: "Unknown Artist",
                        album = metadata.album ?: "Unknown Album",
                        albumArtist = metadata.albumArtist,
                        genre = metadata.genre,
                        year = metadata.year,
                        trackNumber = metadata.trackNumber,
                        duration = metadata.duration,
                        filePath = file.absolutePath,
                        coverArtPath = null, // Will be implemented later
                        isFavorite = false
                    )
                    songs.add(song)
                } catch (e: Exception) {
                    println("Error reading metadata for ${file.name}: ${e.message}")
                }
                
                _scanProgress.value = (index + 1).toFloat() / totalFiles
            }
            
            // Actualizar biblioteca
            _allSongs.value = songs.sortedBy { it.title.lowercase() }
            
            // Organizar por álbumes y artistas
            organizeLibrary()
            
        } finally {
            _isScanning.value = false
            _scanProgress.value = 1f
        }
    }
    
    /**
     * Organiza las canciones en álbumes y artistas
     */
    private fun organizeLibrary() {
        val songs = _allSongs.value
        
        // Agrupar por álbum
        val albumsMap = songs
            .groupBy { it.album }
            .mapNotNull { (albumName, songs) ->
                if (albumName.isEmpty()) return@mapNotNull null
                
                val firstSong = songs.first()
                Album(
                    id = albumName.lowercase().replace(" ", "_"),
                    title = albumName,
                    artist = firstSong.albumArtist?.ifEmpty { firstSong.artist } ?: firstSong.artist,
                    year = firstSong.year,
                    coverArtPath = firstSong.coverArtPath,
                    songCount = songs.size,
                    totalDuration = songs.sumOf { it.duration }
                )
            }
            .sortedBy { it.title.lowercase() }
        
        _albums.value = albumsMap
        
        // Agrupar por artista
        val artistsMap = songs
            .flatMap { song ->
                val artists = mutableListOf<String>()
                if (song.artist.isNotEmpty()) artists.add(song.artist)
                if (song.albumArtist?.isNotEmpty() == true && song.albumArtist != song.artist) {
                    artists.add(song.albumArtist)
                }
                artists.distinct().map { it to song }
            }
            .groupBy({ it.first }, { it.second })
            .map { (artistName, artistSongs) ->
                val distinctSongs = artistSongs.distinct()
                val artistAlbums = albumsMap.filter { 
                    it.artist.equals(artistName, ignoreCase = true) 
                }
                Artist(
                    id = artistName.lowercase().replace(" ", "_"),
                    name = artistName,
                    albumCount = artistAlbums.size,
                    songCount = distinctSongs.size
                )
            }
            .sortedBy { it.name.lowercase() }
        
        _artists.value = artistsMap
    }
    
    /**
     * Busca canciones por texto
     */
    fun searchSongs(query: String): List<Song> {
        if (query.isBlank()) return _allSongs.value
        
        val lowerQuery = query.lowercase()
        return _allSongs.value.filter { song ->
            song.title.lowercase().contains(lowerQuery) ||
            song.artist.lowercase().contains(lowerQuery) ||
            song.album.lowercase().contains(lowerQuery) ||
            song.genre?.lowercase()?.contains(lowerQuery) == true
        }
    }
    
    /**
     * Busca álbumes por texto
     */
    fun searchAlbums(query: String): List<Album> {
        if (query.isBlank()) return _albums.value
        
        val lowerQuery = query.lowercase()
        return _albums.value.filter { album ->
            album.title.lowercase().contains(lowerQuery) ||
            album.artist.lowercase().contains(lowerQuery)
        }
    }
    
    /**
     * Busca artistas por texto
     */
    fun searchArtists(query: String): List<Artist> {
        if (query.isBlank()) return _artists.value
        
        val lowerQuery = query.lowercase()
        return _artists.value.filter { artist ->
            artist.name.lowercase().contains(lowerQuery)
        }
    }
    
    /**
     * Filtra canciones por género
     */
    fun getSongsByGenre(genre: String): List<Song> {
        return _allSongs.value.filter { 
            it.genre?.equals(genre, ignoreCase = true) == true
        }
    }
    
    /**
     * Filtra canciones por año
     */
    fun getSongsByYear(year: Int): List<Song> {
        return _allSongs.value.filter { it.year == year }
    }
    
    /**
     * Obtiene todas las canciones favoritas
     */
    fun getFavorites(): List<Song> {
        return _allSongs.value.filter { it.isFavorite }
    }
    
    /**
     * Marca/desmarca una canción como favorita
     */
    fun toggleFavorite(songId: String) {
        _allSongs.value = _allSongs.value.map { song ->
            if (song.id == songId) {
                song.copy(isFavorite = !song.isFavorite)
            } else {
                song
            }
        }
    }
    
    /**
     * Obtiene lista de géneros únicos
     */
    fun getGenres(): List<String> {
        return _allSongs.value
            .mapNotNull { it.genre?.takeIf { genre -> genre.isNotEmpty() } }
            .distinct()
            .sorted()
    }
    
    /**
     * Obtiene lista de años únicos
     */
    fun getYears(): List<Int> {
        return _allSongs.value
            .mapNotNull { it.year?.takeIf { year -> year > 0 } }
            .distinct()
            .sortedDescending()
    }
    
    /**
     * Limpia la biblioteca
     */
    fun clearLibrary() {
        _allSongs.value = emptyList()
        _albums.value = emptyList()
        _artists.value = emptyList()
    }
}
