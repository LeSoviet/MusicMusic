package com.musicmusic.ui.screens.library

import com.musicmusic.data.repository.MusicRepository
import com.musicmusic.domain.model.Album
import com.musicmusic.domain.model.Artist
import com.musicmusic.domain.model.Song
import com.musicmusic.ui.screens.player.PlayerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de biblioteca musical.
 * 
 * Gestiona:
 * - Vista de canciones, álbumes, artistas
 * - Búsqueda y filtrado
 * - Escaneo de directorios
 * - Reproducción desde biblioteca
 */
class LibraryViewModel(
    private val musicRepository: MusicRepository,
    private val playerViewModel: PlayerViewModel,
    private val viewModelScope: CoroutineScope
) {
    
    // ========== Estados observables ==========
    
    val allSongs: StateFlow<List<Song>> = musicRepository.allSongs
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    
    val albums: StateFlow<List<Album>> = musicRepository.albums
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    
    val artists: StateFlow<List<Artist>> = musicRepository.artists
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    
    val isScanning: StateFlow<Boolean> = musicRepository.isScanning
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    
    val scanProgress: StateFlow<Float> = musicRepository.scanProgress
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0f)
    
    // ========== Estado local ==========
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedTab = MutableStateFlow(LibraryTab.SONGS)
    val selectedTab: StateFlow<LibraryTab> = _selectedTab.asStateFlow()
    
    private val _selectedGenre = MutableStateFlow<String?>(null)
    val selectedGenre: StateFlow<String?> = _selectedGenre.asStateFlow()
    
    private val _selectedYear = MutableStateFlow<Int?>(null)
    val selectedYear: StateFlow<Int?> = _selectedYear.asStateFlow()
    
    private val _sortOrder = MutableStateFlow(SortOrder.TITLE_ASC)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()
    
    // ========== Listas filtradas ==========
    
    val filteredSongs: StateFlow<List<Song>> = combine(
        allSongs,
        _searchQuery,
        _selectedGenre,
        _selectedYear,
        _sortOrder
    ) { songs, query, genre, year, order ->
        var filtered = songs
        
        // Aplicar búsqueda
        if (query.isNotBlank()) {
            filtered = musicRepository.searchSongs(query)
        }
        
        // Aplicar filtro de género
        if (genre != null) {
            filtered = filtered.filter { it.genre.equals(genre, ignoreCase = true) }
        }
        
        // Aplicar filtro de año
        if (year != null) {
            filtered = filtered.filter { it.year == year }
        }
        
        // Aplicar ordenamiento
        when (order) {
            SortOrder.TITLE_ASC -> filtered.sortedBy { it.title.lowercase() }
            SortOrder.TITLE_DESC -> filtered.sortedByDescending { it.title.lowercase() }
            SortOrder.ARTIST_ASC -> filtered.sortedBy { it.artist.lowercase() }
            SortOrder.ARTIST_DESC -> filtered.sortedByDescending { it.artist.lowercase() }
            SortOrder.ALBUM_ASC -> filtered.sortedBy { it.album.lowercase() }
            SortOrder.ALBUM_DESC -> filtered.sortedByDescending { it.album.lowercase() }
            SortOrder.YEAR_ASC -> filtered.sortedBy { it.year }
            SortOrder.YEAR_DESC -> filtered.sortedByDescending { it.year }
            SortOrder.DURATION_ASC -> filtered.sortedBy { it.duration }
            SortOrder.DURATION_DESC -> filtered.sortedByDescending { it.duration }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    
    val filteredAlbums: StateFlow<List<Album>> = combine(
        albums,
        _searchQuery
    ) { albums, query ->
        if (query.isBlank()) albums else musicRepository.searchAlbums(query)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    
    val filteredArtists: StateFlow<List<Artist>> = combine(
        artists,
        _searchQuery
    ) { artists, query ->
        if (query.isBlank()) artists else musicRepository.searchArtists(query)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    
    // ========== Acciones ==========
    
    fun scanDirectory(path: String) {
        viewModelScope.launch {
            musicRepository.scanDirectory(path)
        }
    }
    
    /**
     * Agrega archivos individuales a la biblioteca
     */
    fun addFiles(files: List<java.io.File>) {
        viewModelScope.launch {
            musicRepository.addFiles(files)
        }
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun selectTab(tab: LibraryTab) {
        _selectedTab.value = tab
    }
    
    fun selectGenre(genre: String?) {
        _selectedGenre.value = genre
    }
    
    fun selectYear(year: Int?) {
        _selectedYear.value = year
    }
    
    fun changeSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }
    
    fun toggleFavorite(songId: String) {
        musicRepository.toggleFavorite(songId)
    }
    
    // ========== Reproducción ==========
    
    fun playSong(song: Song) {
        val queue = filteredSongs.value
        val index = queue.indexOfFirst { it.id == song.id }
        if (index >= 0) {
            playerViewModel.playQueue(queue, index)
        } else {
            playerViewModel.playSong(song)
        }
    }
    
    fun playAlbum(album: Album, startIndex: Int = 0) {
        val songs = allSongs.value.filter { it.album == album.title }
            .sortedBy { it.trackNumber }
        if (songs.isNotEmpty()) {
            playerViewModel.playQueue(songs, startIndex)
        }
    }
    
    fun playArtist(artist: Artist) {
        val songs = allSongs.value.filter { 
            it.artist == artist.name || it.albumArtist == artist.name 
        }
        if (songs.isNotEmpty()) {
            playerViewModel.playQueue(songs, 0)
        }
    }
    
    fun playAll() {
        val songs = filteredSongs.value
        if (songs.isNotEmpty()) {
            playerViewModel.playQueue(songs, 0)
        }
    }
    
    fun shuffleAll() {
        val songs = filteredSongs.value.shuffled()
        if (songs.isNotEmpty()) {
            playerViewModel.playQueue(songs, 0)
        }
    }
    
    fun addToQueue(song: Song) {
        playerViewModel.addToQueue(song)
    }
    
    fun addAlbumToQueue(album: Album) {
        val songs = allSongs.value.filter { it.album == album.title }
            .sortedBy { it.trackNumber }
        songs.forEach { song ->
            playerViewModel.addToQueue(song)
        }
    }
    
    // ========== Utilidades ==========
    
    fun getGenres(): List<String> = musicRepository.getGenres()
    
    fun getYears(): List<Int> = musicRepository.getYears()
    
    fun clearLibrary() {
        musicRepository.clearLibrary()
    }
}

/**
 * Tabs disponibles en la biblioteca
 */
enum class LibraryTab {
    SONGS,
    ALBUMS,
    ARTISTS
}

/**
 * Opciones de ordenamiento
 */
enum class SortOrder {
    TITLE_ASC,
    TITLE_DESC,
    ARTIST_ASC,
    ARTIST_DESC,
    ALBUM_ASC,
    ALBUM_DESC,
    YEAR_ASC,
    YEAR_DESC,
    DURATION_ASC,
    DURATION_DESC
}
