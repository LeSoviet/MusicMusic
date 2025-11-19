package com.musicmusic.ui.screens.player

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.musicmusic.domain.model.PlaybackState
import com.musicmusic.ui.components.*
import org.koin.compose.koinInject

/**
 * Pantalla principal de reproducción (Now Playing).
 * 
 * Muestra:
 * - Carátula grande con fondo blur
 * - Información de la canción
 * - Barra de progreso
 * - Controles de reproducción
 * - Control de volumen
 * - Botones de favorito y opciones
 */
@Composable
fun NowPlayingScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    playerViewModel: PlayerViewModel = koinInject()
) {
    val currentSong by playerViewModel.currentSong.collectAsState()
    val playbackState by playerViewModel.playbackState.collectAsState()
    val isShuffleEnabled by playerViewModel.isShuffleEnabled.collectAsState()
    val repeatMode by playerViewModel.repeatMode.collectAsState()
    val volume by playerViewModel.volume.collectAsState()
    val progress = playerViewModel.getProgress()
    
    Scaffold(
        topBar = {
            NowPlayingTopBar(onBack = onBack)
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Carátula con fondo blur
            AlbumCoverWithBlur(
                coverArtPath = currentSong?.coverArtPath,
                coverSize = 320.dp
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Información de la canción
            SongInfo(
                title = currentSong?.title ?: "No song playing",
                artist = currentSong?.getDisplayArtist() ?: "",
                album = currentSong?.album ?: ""
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Barra de progreso
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                SeekBar(
                    progress = progress,
                    onProgressChange = { newProgress ->
                        val duration = playerViewModel.duration.value
                        val newPosition = (duration * newProgress).toLong()
                        playerViewModel.seekTo(newPosition)
                    },
                    enabled = currentSong != null
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Tiempos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = playerViewModel.getFormattedPosition(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = playerViewModel.getFormattedDuration(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Controles de reproducción
            PlayerControls(
                playbackState = playbackState,
                isShuffleEnabled = isShuffleEnabled,
                repeatMode = repeatMode,
                onPlayPause = { playerViewModel.togglePlayPause() },
                onPrevious = { playerViewModel.previous() },
                onNext = { playerViewModel.next() },
                onShuffle = { playerViewModel.toggleShuffle() },
                onRepeat = { playerViewModel.toggleRepeatMode() },
                showSecondaryControls = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Fila de acciones adicionales
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón de favorito
                IconButton(onClick = { /* TODO: Toggle favorite */ }) {
                    Icon(
                        imageVector = if (currentSong?.isFavorite == true) {
                            Icons.Rounded.Favorite
                        } else {
                            Icons.Rounded.FavoriteBorder
                        },
                        contentDescription = "Favorite",
                        tint = if (currentSong?.isFavorite == true) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                
                // Control de volumen
                VolumeControl(
                    volume = volume,
                    onVolumeChange = { playerViewModel.setVolume(it) },
                    orientation = VolumeOrientation.Horizontal
                )
                
                // Botón de cola
                IconButton(onClick = { /* TODO: Show queue */ }) {
                    Icon(
                        imageVector = Icons.Rounded.QueueMusic,
                        contentDescription = "Queue"
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NowPlayingTopBar(
    onBack: () -> Unit
) {
    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            IconButton(onClick = { /* TODO: More options */ }) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "More"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f)
        )
    )
}

@Composable
private fun SongInfo(
    title: String,
    artist: String,
    album: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Artista
        if (artist.isNotEmpty()) {
            Text(
                text = artist,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Álbum
        if (album.isNotEmpty()) {
            Text(
                text = album,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
