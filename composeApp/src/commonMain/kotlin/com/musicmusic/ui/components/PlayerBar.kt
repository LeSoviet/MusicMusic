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
 * Layout:
 * - Izquierda: Carátula + Artista
 * - Centro: Controles (shuffle, previous, play/pause, next, repeat, favorite)
 * - Derecha: Título de la canción + Control de volumen
 * - Superior: Barra de progreso interactiva para seek
 */
@Composable
fun PlayerBar(
    modifier: Modifier = Modifier,
    playerViewModel: PlayerViewModel = koinInject()
) {
    val currentSong by playerViewModel.currentSong.collectAsState()
    val playbackState by playerViewModel.playbackState.collectAsState()
    val isShuffleEnabled by playerViewModel.isShuffleEnabled.collectAsState()
    val repeatMode by playerViewModel.repeatMode.collectAsState()
    val volume by playerViewModel.volume.collectAsState()
    val currentPosition by playerViewModel.currentPosition.collectAsState()
    val duration by playerViewModel.duration.collectAsState()

    // Estado local para el slider mientras el usuario lo arrastra
    var isUserSeeking by remember { mutableStateOf(false) }
    var seekPosition by remember { mutableStateOf(0f) }

    // Calcular el progreso actual
    val progress = if (duration > 0) {
        currentPosition.toFloat() / duration.toFloat()
    } else {
        0f
    }

    // Actualizar seekPosition cuando no está arrastrando
    LaunchedEffect(progress, isUserSeeking) {
        if (!isUserSeeking) {
            seekPosition = progress
        }
    }

    // Usar siempre seekPosition para evitar saltos visuales
    val displayProgress = seekPosition

    // Solo mostrar si hay una canción
    AnimatedVisibility(
        visible = currentSong != null,
        enter = slideInVertically(tween(300)) { it } + fadeIn(tween(300)),
        exit = slideOutVertically(tween(300)) { it } + fadeOut(tween(300))
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .height(120.dp),
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Slider interactivo para seek
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Slider(
                        value = displayProgress.coerceIn(0f, 1f),
                        onValueChange = { newValue ->
                            isUserSeeking = true
                            seekPosition = newValue
                        },
                        onValueChangeFinished = {
                            isUserSeeking = false
                            val targetPosition = (seekPosition * duration).toLong()
                            playerViewModel.seekTo(targetPosition)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 16.dp)
                ) {
                    // Artista (izquierda)
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Miniatura de carátula
                        AlbumCoverThumbnail(
                            coverArtPath = currentSong?.coverArtPath,
                            size = 64.dp
                        )

                        // Artista
                        Text(
                            text = currentSong?.getDisplayArtist() ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 200.dp)
                        )
                    }
                    
                    // Controles del reproductor (centro)
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Shuffle
                        IconButton(
                            onClick = { playerViewModel.toggleShuffle() }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Shuffle,
                                contentDescription = "Shuffle",
                                tint = if (isShuffleEnabled) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }

                        // Previous
                        IconButton(
                            onClick = { playerViewModel.previous() }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipPrevious,
                                contentDescription = "Previous"
                            )
                        }

                        // Play/Pause (prominente)
                        FilledIconButton(
                            onClick = { playerViewModel.togglePlayPause() },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.size(48.dp)
                        ) {
                            val icon = when (playbackState) {
                                PlaybackState.PLAYING, PlaybackState.BUFFERING -> Icons.Rounded.Pause
                                else -> Icons.Rounded.PlayArrow
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = if (playbackState == PlaybackState.PLAYING) "Pause" else "Play",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Next
                        IconButton(
                            onClick = { playerViewModel.next() }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipNext,
                                contentDescription = "Next"
                            )
                        }

                        // Repeat
                        IconButton(
                            onClick = { playerViewModel.toggleRepeatMode() }
                        ) {
                            val icon = when (repeatMode) {
                                RepeatMode.ONE -> Icons.Rounded.RepeatOne
                                else -> Icons.Rounded.Repeat
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = "Repeat",
                                tint = if (repeatMode != RepeatMode.OFF) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }

                        // Favorite
                        IconButton(
                            onClick = {
                                currentSong?.let { song ->
                                    playerViewModel.toggleFavorite(song.id)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (currentSong?.isFavorite == true) {
                                    Icons.Rounded.Favorite
                                } else {
                                    Icons.Rounded.FavoriteBorder
                                },
                                contentDescription = "Toggle favorite",
                                tint = if (currentSong?.isFavorite == true) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }

                    // Título de la canción y controles secundarios (derecha)
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Título de la canción
                        Text(
                            text = currentSong?.title ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 250.dp)
                        )
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
                    }
                }
            }
        }
    }
}