package com.musicmusic.data.repository

import com.musicmusic.domain.model.Album
import com.musicmusic.domain.model.Artist
import com.musicmusic.domain.model.Song
import com.musicmusic.files.FileScanner
import com.musicmusic.files.MetadataReader
import com.musicmusic.files.MetadataNormalizer
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
            processFiles(audioFiles)
            
        } finally {
            _isScanning.value = false
            _scanProgress.value = 1f
        }
    }
    
    /**
     * Agrega archivos individuales a la biblioteca
     */
    suspend fun addFiles(files: List<File>) = withContext(Dispatchers.IO) {
        _isScanning.value = true
        _scanProgress.value = 0f
        
        try {
            // Filtrar solo archivos de audio
            val audioFiles = files.filter { metadataReader.isAudioFile(it) }
            processFiles(audioFiles)
            
        } finally {
            _isScanning.value = false
            _scanProgress.value = 1f
        }
    }
    
    /**
     * Procesa una lista de archivos de audio
     */
    private suspend fun processFiles(audioFiles: List<File>) = withContext(Dispatchers.IO) {
        val totalFiles = audioFiles.size
        
        // Extraer metadata
        val newSongs = mutableListOf<Song>()
        for ((index, file) in audioFiles.withIndex()) {
            try {
                // Leer metadatos del archivo
                val rawMetadata = metadataReader.readMetadata(file)
                
                // Normalizar metadatos con fallbacks inteligentes
                val metadata = MetadataNormalizer.normalize(rawMetadata, file)
                
                // Buscar carátula en la carpeta si no está embebida
                val coverArtPath = if (metadata.coverArtData != null) {
                    // TODO: Guardar cover art embebida
                    null
                } else {
                    MetadataNormalizer.findCoverArtInFolder(file)?.absolutePath
                }
                
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
                    coverArtPath = coverArtPath,
                    isFavorite = false
                )
                newSongs.add(song)
                
                println("✅ ${file.name} -> ${song.artist} - ${song.title}")
            } catch (e: Exception) {
                println("⚠️ Error processing ${file.name}: ${e.message}")
            }
            
            _scanProgress.value = (index + 1).toFloat() / totalFiles
        }
        
        // Merge con biblioteca existente (evitar duplicados por path)
        val existingSongs = _allSongs.value
        val existingPaths = existingSongs.map { it.filePath }.toSet()
        val songsToAdd = newSongs.filter { it.filePath !in existingPaths }
        
        // Actualizar biblioteca
        _allSongs.value = (existingSongs + songsToAdd).sortedBy { it.title.lowercase() }
        
        // Reorganizar por álbumes y artistas
        organizeLibrary()
        
        println("✅ Added ${songsToAdd.size} new songs (${newSongs.size - songsToAdd.size} duplicates skipped)")
    }
    
    /**
     * Organiza las canciones en álbumes y artistas
     */
    private fun organizeLibrary() {
        val songs = _allSongs.value
        
        // Agrupar por álbum (usando normalización)
        val albumsMap = songs
            .groupBy { MetadataNormalizer.normalizeAlbumName(it.album) }
            .mapNotNull { (normalizedAlbum, albumSongs) ->
                if (normalizedAlbum.isEmpty() || normalizedAlbum == "unknown album") return@mapNotNull null
                
                val firstSong = albumSongs.first()
                // Usar el nombre original del primer álbum encontrado
                val albumName = albumSongs.first().album
                
                Album(
                    id = normalizedAlbum.lowercase().replace(" ", "_"),
                    title = albumName,
                    artist = firstSong.albumArtist?.ifEmpty { firstSong.artist } ?: firstSong.artist,
                    year = firstSong.year,
                    coverArtPath = albumSongs.firstOrNull { it.coverArtPath != null }?.coverArtPath,
                    songCount = albumSongs.size,
                    totalDuration = albumSongs.sumOf { it.duration }
                )
            }
            .sortedBy { it.title.lowercase() }
        
        _albums.value = albumsMap
        
        // Agrupar por artista (usando normalización)
        val artistsMap = songs
            .flatMap { song ->
                val artists = mutableListOf<Pair<String, String>>() // Pair of (normalized, original)
                if (song.artist.isNotEmpty()) {
                    artists.add(
                        MetadataNormalizer.normalizeArtistName(song.artist) to song.artist
                    )
                }
                if (song.albumArtist?.isNotEmpty() == true && song.albumArtist != song.artist) {
                    artists.add(
                        MetadataNormalizer.normalizeArtistName(song.albumArtist) to song.albumArtist
                    )
                }
                artists.distinct().map { (normalized, original) -> Triple(normalized, original, song) }
            }
            .groupBy({ it.first }, { Triple(it.second, it.third, Unit) })
            .mapNotNull { (normalizedArtist, entries) ->
                if (normalizedArtist.isEmpty() || normalizedArtist.lowercase() == "unknown artist") return@mapNotNull null
                
                // Usar el nombre original más común
                val originalName = entries
                    .groupBy { it.first }
                    .maxByOrNull { it.value.size }
                    ?.key ?: normalizedArtist
                
                val artistSongs = entries.map { it.second }.distinct()
                val artistAlbums = albumsMap.filter { album ->
                    MetadataNormalizer.normalizeArtistName(album.artist) == normalizedArtist
                }
                
                Artist(
                    id = normalizedArtist.lowercase().replace(" ", "_"),
                    name = originalName,
                    albumCount = artistAlbums.size,
                    songCount = artistSongs.size
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
