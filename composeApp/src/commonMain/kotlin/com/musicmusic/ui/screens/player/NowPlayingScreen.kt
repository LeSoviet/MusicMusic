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
import com.musicmusic.ui.components.*
import com.musicmusic.domain.model.PlaybackState
import com.musicmusic.domain.model.RepeatMode
import com.musicmusic.ui.components.EqualizerPanel
import org.koin.compose.koinInject

@Composable
fun NowPlayingScreen(
    onBack: () -> Unit,
    onShowQueue: () -> Unit,
    modifier: Modifier = Modifier,
    playerViewModel: PlayerViewModel = koinInject()
) {
    val currentSong by playerViewModel.currentSong.collectAsState()
    val playbackState by playerViewModel.playbackState.collectAsState()
    val isShuffleEnabled by playerViewModel.isShuffleEnabled.collectAsState()
    val repeatMode by playerViewModel.repeatMode.collectAsState()
    val volume by playerViewModel.volume.collectAsState()
    val progress = playerViewModel.getProgress()
    
    // Estado para mostrar/ocultar el panel del ecualizador
    var showEqualizer by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = { 
            NowPlayingTopBar(
                onBack = onBack,
                onToggleEqualizer = { showEqualizer = !showEqualizer }
            ) 
        }
    ) { paddingValues ->

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val isCompact = maxWidth < 1280.dp || maxHeight < 720.dp
            val coverSize = if (isCompact) 180.dp else 280.dp
            val verticalSpacing = if (isCompact) 12.dp else 24.dp
            val horizontalPadding = if (isCompact) 16.dp else 32.dp
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = horizontalPadding)
                    .padding(vertical = if (isCompact) 12.dp else 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(verticalSpacing, Alignment.CenterVertically)
            ) {
                // Spacer flexible arriba
                Spacer(modifier = Modifier.weight(1f, fill = false))
                
                // Carátula
                AlbumCoverWithBlur(
                    coverArtPath = currentSong?.coverArtPath,
                    coverSize = coverSize
                )

                // Info canción
                SongInfo(
                    title = currentSong?.title ?: "No song playing",
                    artist = currentSong?.getDisplayArtist() ?: "",
                    album = currentSong?.album ?: ""
                )

                // Barra de progreso
                Column(modifier = Modifier.fillMaxWidth()) {
                    SeekBar(
                        progress = progress,
                        onSeekStart = { newProgress ->
                            val targetPosition = (playerViewModel.duration.value * newProgress).toLong()
                            playerViewModel.startSeeking(targetPosition)
                        },
                        onSeekChange = { newProgress ->
                            val targetPosition = (playerViewModel.duration.value * newProgress).toLong()
                            playerViewModel.updateSeekPosition(targetPosition)
                        },
                        onSeekEnd = {
                            playerViewModel.endSeeking()
                        },
                        enabled = currentSong != null
                    )

                    Spacer(modifier = Modifier.height(8.dp))

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

                // Barra de controles unificada
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (isCompact) 8.dp else 16.dp)
                ) {
                    // Controles principales centrados
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isCompact) {
                            // Playlist button (solo en modo normal)
                            IconButton(
                                onClick = { /* TODO: Implement playlist functionality */ },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PlaylistPlay,
                                    contentDescription = "Playlist",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        // Shuffle button
                        IconButton(
                            onClick = { playerViewModel.toggleShuffle() },
                            modifier = Modifier.size(if (isCompact) 36.dp else 40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Shuffle,
                                contentDescription = "Shuffle",
                                modifier = Modifier.size(if (isCompact) 18.dp else 20.dp),
                                tint = if (isShuffleEnabled) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }

                        Spacer(modifier = Modifier.width(if (isCompact) 4.dp else 8.dp))

                        // Previous button
                        IconButton(
                            onClick = { playerViewModel.previous() },
                            modifier = Modifier.size(if (isCompact) 40.dp else 48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipPrevious,
                                contentDescription = "Previous",
                                modifier = Modifier.size(if (isCompact) 24.dp else 28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(if (isCompact) 8.dp else 16.dp))

                        // Play/Pause button (grande y prominente)
                        FilledIconButton(
                            onClick = { playerViewModel.togglePlayPause() },
                            modifier = Modifier.size(if (isCompact) 48.dp else 56.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            val icon = when (playbackState) {
                                PlaybackState.PLAYING, PlaybackState.BUFFERING -> Icons.Rounded.Pause
                                else -> Icons.Rounded.PlayArrow
                            }

                            Icon(
                                imageVector = icon,
                                contentDescription = if (playbackState == PlaybackState.PLAYING) "Pause" else "Play",
                                modifier = Modifier.size(if (isCompact) 28.dp else 32.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(if (isCompact) 8.dp else 16.dp))

                        // Next button
                        IconButton(
                            onClick = { playerViewModel.next() },
                            modifier = Modifier.size(if (isCompact) 40.dp else 48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipNext,
                                contentDescription = "Next",
                                modifier = Modifier.size(if (isCompact) 24.dp else 28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(if (isCompact) 4.dp else 8.dp))

                        // Repeat button
                        IconButton(
                            onClick = { playerViewModel.toggleRepeatMode() },
                            modifier = Modifier.size(if (isCompact) 36.dp else 40.dp)
                        ) {
                            val icon = when (repeatMode) {
                                RepeatMode.ONE -> Icons.Rounded.RepeatOne
                                else -> Icons.Rounded.Repeat
                            }

                            Icon(
                                imageVector = icon,
                                contentDescription = "Repeat",
                                modifier = Modifier.size(if (isCompact) 18.dp else 20.dp),
                                tint = if (repeatMode != RepeatMode.OFF) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }

                        if (!isCompact) {
                            Spacer(modifier = Modifier.width(8.dp))

                            // Favorite button (solo en modo normal)
                            IconButton(
                                onClick = {
                                    currentSong?.let { song ->
                                        playerViewModel.toggleFavorite(song.id)
                                    }
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = if (currentSong?.isFavorite == true) {
                                        Icons.Rounded.Favorite
                                    } else {
                                        Icons.Rounded.FavoriteBorder
                                    },
                                    contentDescription = "Toggle favorite",
                                    modifier = Modifier.size(20.dp),
                                    tint = if (currentSong?.isFavorite == true) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                    
                    // Controles secundarios (volumen y queue) a la derecha
                    if (!isCompact) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Volume control with mute button
                            IconButton(
                                onClick = { playerViewModel.toggleMute() },
                                modifier = Modifier.size(40.dp)
                            ) {
                                val volumeIcon = when {
                                    volume == 0f -> Icons.Rounded.VolumeOff
                                    volume < 0.3f -> Icons.Rounded.VolumeMute
                                    volume < 0.7f -> Icons.Rounded.VolumeDown
                                    else -> Icons.Rounded.VolumeUp
                                }
                                Icon(
                                    imageVector = volumeIcon,
                                    contentDescription = "Toggle mute",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Slider(
                                value = volume,
                                onValueChange = { playerViewModel.setVolume(it) },
                                modifier = Modifier.width(120.dp)
                            )

                            // Queue button
                            IconButton(
                                onClick = { onShowQueue() },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.QueueMusic,
                                    contentDescription = "Show queue",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                // Panel del ecualizador (solo si está activo)
                if (showEqualizer) {
                    EqualizerPanel(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    )
                }
                
                // Spacer flexible abajo
                Spacer(modifier = Modifier.weight(1f, fill = false))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NowPlayingTopBar(
    onBack: () -> Unit,
    onToggleEqualizer: () -> Unit
) {
    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Collapse player"
                )
            }
        },
        actions = {
            IconButton(onClick = onToggleEqualizer) {
                Icon(
                    imageVector = Icons.Rounded.Equalizer,
                    contentDescription = "Toggle equalizer"
                )
            }
            IconButton(onClick = { /* TODO */ }) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "More options"
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
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        if (artist.isNotEmpty()) {
            Text(
                text = artist,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (album.isNotEmpty()) {
            Text(
                text = album,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}