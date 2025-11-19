package com.musicmusic.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.musicmusic.domain.model.PlaybackState
import com.musicmusic.domain.model.RepeatMode
import com.musicmusic.ui.screens.player.PlayerViewModel
import org.koin.compose.koinInject

/**
 * Barra de reproductor persistente (mini player) en la parte inferior.
 * 
 * Muestra:
 * - Miniatura de carátula
 * - Información básica de la canción
 * - Controles completos (shuffle, previous, play/pause, next, repeat, favorite)
 * - Volumen y queue
 * - Barra de progreso sutil
 * - Click para expandir a NowPlayingScreen
 */
@Composable
fun PlayerBar(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    playerViewModel: PlayerViewModel = koinInject()
) {
    val currentSong by playerViewModel.currentSong.collectAsState()
    val playbackState by playerViewModel.playbackState.collectAsState()
    val isShuffleEnabled by playerViewModel.isShuffleEnabled.collectAsState()
    val repeatMode by playerViewModel.repeatMode.collectAsState()
    val volume by playerViewModel.volume.collectAsState()
    val progress = playerViewModel.getProgress()
    
    // Solo mostrar si hay una canción
    AnimatedVisibility(
        visible = currentSong != null,
        enter = slideInVertically(tween(300)) { it } + fadeIn(tween(300)),
        exit = slideOutVertically(tween(300)) { it } + fadeOut(tween(300))
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .height(100.dp)
                .clickable(onClick = onClick),
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Barra de progreso sutil en la parte superior
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Carátula + Info (izquierda)
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .fillMaxHeight()
                            .widthIn(max = 300.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Miniatura de carátula
                        AlbumCoverThumbnail(
                            coverArtPath = currentSong?.coverArtPath,
                            size = 64.dp
                        )
                        
                        // Información de la canción
                        Column(
                            modifier = Modifier.weight(1f, fill = false),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = currentSong?.title ?: "",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = currentSong?.getDisplayArtist() ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    
                    // Controles del reproductor (centro)
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Shuffle
                        IconButton(
                            onClick = { playerViewModel.toggleShuffle() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Shuffle,
                                contentDescription = "Shuffle",
                                modifier = Modifier.size(18.dp),
                                tint = if (isShuffleEnabled) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Previous
                        IconButton(
                            onClick = { playerViewModel.previous() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipPrevious,
                                contentDescription = "Previous",
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Play/Pause (prominente)
                        FilledIconButton(
                            onClick = { playerViewModel.togglePlayPause() },
                            modifier = Modifier.size(48.dp),
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
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Next
                        IconButton(
                            onClick = { playerViewModel.next() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipNext,
                                contentDescription = "Next",
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Repeat
                        IconButton(
                            onClick = { playerViewModel.toggleRepeatMode() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            val icon = when (repeatMode) {
                                RepeatMode.ONE -> Icons.Rounded.RepeatOne
                                else -> Icons.Rounded.Repeat
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = "Repeat",
                                modifier = Modifier.size(18.dp),
                                tint = if (repeatMode != RepeatMode.OFF) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Favorite
                        IconButton(
                            onClick = {
                                currentSong?.let { song ->
                                    playerViewModel.toggleFavorite(song.id)
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (currentSong?.isFavorite == true) {
                                    Icons.Rounded.Favorite
                                } else {
                                    Icons.Rounded.FavoriteBorder
                                },
                                contentDescription = "Toggle favorite",
                                modifier = Modifier.size(18.dp),
                                tint = if (currentSong?.isFavorite == true) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }

                    // Controles secundarios (derecha)
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Volume control with mute button
                        IconButton(
                            onClick = { playerViewModel.toggleMute() },
                            modifier = Modifier.size(36.dp)
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
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Slider(
                            value = volume,
                            onValueChange = { playerViewModel.setVolume(it) },
                            modifier = Modifier.width(100.dp)
                        )

                        // Expand button
                        IconButton(
                            onClick = onClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowUp,
                                contentDescription = "Expand player",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
