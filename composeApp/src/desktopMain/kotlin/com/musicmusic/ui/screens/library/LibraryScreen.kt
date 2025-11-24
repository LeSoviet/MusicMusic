package com.musicmusic.ui.screens.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.musicmusic.ui.components.AlbumGrid
import com.musicmusic.ui.components.SongItem
import org.koin.compose.koinInject

/**
 * Pantalla principal de la biblioteca musical.
 * 
 * Muestra tabs para:
 * - Canciones
 * - Álbumes
 * - Artistas
 * 
 * Incluye:
 * - Búsqueda
 * - Filtros
 * - Ordenamiento
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onScanDirectory: () -> Unit,
    onAddFiles: () -> Unit,
    modifier: Modifier = Modifier,
    libraryViewModel: LibraryViewModel = koinInject()
) {
    val selectedTab by libraryViewModel.selectedTab.collectAsState()
    val searchQuery by libraryViewModel.searchQuery.collectAsState()
    val isScanning by libraryViewModel.isScanning.collectAsState()
    val scanProgress by libraryViewModel.scanProgress.collectAsState()
    
    val filteredSongs by libraryViewModel.filteredSongs.collectAsState()
    val filteredAlbums by libraryViewModel.filteredAlbums.collectAsState()
    val filteredArtists by libraryViewModel.filteredArtists.collectAsState()
    
    Scaffold(
        topBar = {
            LibraryTopBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { libraryViewModel.updateSearchQuery(it) },
                onScanDirectoryClick = onScanDirectory,
                onAddFilesClick = onAddFiles,
                selectedTab = selectedTab
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Indicador de escaneo
            if (isScanning) {
                LinearProgressIndicator(
                    progress = scanProgress,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Contenido según tab seleccionado
            when (selectedTab) {
                LibraryTab.SONGS -> {
                    SongsTab(
                        songs = filteredSongs,
                        onSongClick = { song -> libraryViewModel.playSong(song) },
                        onToggleFavorite = { song -> libraryViewModel.toggleFavorite(song.id) },
                        onPlayAll = { libraryViewModel.playAll() },
                        onShuffleAll = { libraryViewModel.shuffleAll() }
                    )
                }
                LibraryTab.ALBUMS -> {
                    AlbumsTab(
                        albums = filteredAlbums,
                        onAlbumClick = { album -> libraryViewModel.playAlbum(album) }
                    )
                }
                LibraryTab.ARTISTS -> {
                    ArtistsTab(
                        artists = filteredArtists,
                        onArtistClick = { artist -> libraryViewModel.playArtist(artist) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onScanDirectoryClick: () -> Unit,
    onAddFilesClick: () -> Unit,
    selectedTab: LibraryTab
) {
    var isSearchActive by remember { mutableStateOf(false) }
    var showImportMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            if (isSearchActive) {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search...") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                val tabName = when (selectedTab) {
                    LibraryTab.SONGS -> "Songs"
                    LibraryTab.ALBUMS -> "Albums"
                    LibraryTab.ARTISTS -> "Artists"
                }
                Text("Library > $tabName")
            }
        },
        actions = {
            if (isSearchActive) {
                IconButton(onClick = {
                    isSearchActive = false
                    onSearchQueryChange("")
                }) {
                    Icon(Icons.Rounded.Close, contentDescription = "")
                }
            } else {
                IconButton(onClick = { isSearchActive = true }) {
                    Icon(Icons.Rounded.Search, contentDescription = "")
                }

                // Import menu dropdown
                Box {
                    IconButton(onClick = { showImportMenu = true }) {
                        Icon(Icons.Rounded.Add, contentDescription = "")
                    }

                    DropdownMenu(
                        expanded = showImportMenu,
                        onDismissRequest = { showImportMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Add Files") },
                            leadingIcon = {
                                Icon(Icons.Rounded.AudioFile, contentDescription = "")
                            },
                            onClick = {
                                showImportMenu = false
                                onAddFilesClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Scan Folder") },
                            leadingIcon = {
                                Icon(Icons.Rounded.FolderOpen, contentDescription = "")
                            },
                            onClick = {
                                showImportMenu = false
                                onScanDirectoryClick()
                            }
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun SongsTab(
    songs: List<com.musicmusic.domain.model.Song>,
    onSongClick: (com.musicmusic.domain.model.Song) -> Unit,
    onToggleFavorite: (com.musicmusic.domain.model.Song) -> Unit,
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit,
    libraryViewModel: LibraryViewModel = koinInject()
) {
    var selectedSongs by remember { mutableStateOf<Set<String>>(emptySet()) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Lista de canciones
        if (songs.isEmpty()) {
            EmptyState(
                icon = Icons.Rounded.MusicNote,
                message = "No songs found"
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 120.dp  // Espacio extra para el PlayerBar
                ),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(
                    items = songs,
                    key = { it.id }
                ) { song ->
                    SongItem(
                        song = song,
                        onClick = {
                            // Single click: selecciona y reproduce inmediatamente
                            selectedSongs = setOf(song.id)
                            onSongClick(song)
                        },
                        isSelected = selectedSongs.contains(song.id),
                        onSelect = {
                            // Selecciona sin reproducir (no usado)
                            selectedSongs = setOf(song.id)
                        },
                        onDoubleClick = {
                            // Double click: reproduce la canción
                            onSongClick(song)
                        },
                        onToggleSelection = { shouldAdd ->
                            // Ctrl+Click: toggle multi-selección
                            selectedSongs = if (shouldAdd) {
                                selectedSongs + song.id
                            } else {
                                selectedSongs - song.id
                            }
                        },
                        onToggleFavorite = { onToggleFavorite(song) },
                        onMoreClick = { /* Handled by dropdown */ },
                        onAddToQueue = { libraryViewModel.addToQueue(song) },
                        onShowAlbum = {
                            libraryViewModel.selectTab(LibraryTab.ALBUMS)
                            libraryViewModel.updateSearchQuery(song.album)
                        },
                        onShowArtist = {
                            libraryViewModel.selectTab(LibraryTab.ARTISTS)
                            libraryViewModel.updateSearchQuery(song.artist)
                        },
                        onDelete = {
                            // TODO: Implement delete confirmation
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AlbumsTab(
    albums: List<com.musicmusic.domain.model.Album>,
    onAlbumClick: (com.musicmusic.domain.model.Album) -> Unit
) {
    if (albums.isEmpty()) {
        EmptyState(
            icon = Icons.Rounded.Album,
            message = "No albums found"
        )
    } else {
        AlbumGrid(
            albums = albums,
            onAlbumClick = onAlbumClick
        )
    }
}

@Composable
private fun ArtistsTab(
    artists: List<com.musicmusic.domain.model.Artist>,
    onArtistClick: (com.musicmusic.domain.model.Artist) -> Unit
) {
    if (artists.isEmpty()) {
        EmptyState(
            icon = Icons.Rounded.Person,
            message = "No artists found"
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = artists,
                key = { it.id }
            ) { artist ->
                ArtistItem(
                    artist = artist,
                    onClick = { onArtistClick(artist) }
                )
            }
        }
    }
}

@Composable
private fun ArtistItem(
    artist: com.musicmusic.domain.model.Artist,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(artist.name) },
        supportingContent = {
            Text("${artist.albumCount} albums • ${artist.songCount} songs")
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
