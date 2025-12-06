package com.musicmusic.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
 * Persistent player bar (mini player) at the bottom.
 *
 * Layout:
 * - Left: Cover + Artist
 * - Center: Controls (shuffle, previous, play/pause, next, repeat, favorite)
 * - Right: Song title + Volume control
 * - Top: Interactive progress bar for seeking
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

    // Local state for slider while user is dragging
    var isUserSeeking by remember { mutableStateOf(false) }
    var seekPosition by remember { mutableStateOf(0f) }

    // Calculate current progress
    val progress = if (duration > 0) {
        currentPosition.toFloat() / duration.toFloat()
    } else {
        0f
    }

    // Update seekPosition when not dragging
    LaunchedEffect(progress, isUserSeeking) {
        if (!isUserSeeking) {
            seekPosition = progress
        }
    }

    // Always use seekPosition to avoid visual jumps
    val displayProgress = seekPosition

    // Only show if there's a song
    AnimatedVisibility(
        visible = currentSong != null,
        enter = slideInVertically(tween(300)) { it } + fadeIn(tween(300)),
        exit = slideOutVertically(tween(300)) { it } + fadeOut(tween(300))
    ) {
        Column(modifier = modifier.fillMaxWidth()) {
            // Progress bar at the top - interactive seek bar with spacing
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Slider(
                    value = displayProgress,
                    onValueChange = { newProgress ->
                        isUserSeeking = true
                        seekPosition = newProgress
                    },
                    onValueChangeFinished = {
                        isUserSeeking = false
                        val newPositionMs = (seekPosition * duration).toLong()
                        playerViewModel.seekTo(newPositionMs)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                // Artist & Cover (left)
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Album cover thumbnail
                    AlbumCoverThumbnail(
                        coverArtPath = currentSong?.coverArtPath,
                        size = 40.dp
                    )

                    Column {
                        // Song title
                        Text(
                            text = currentSong?.title ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 180.dp)
                        )
                        // Artist
                        Text(
                            text = currentSong?.getDisplayArtist() ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 180.dp)
                        )
                    }
                }

                // Player controls (center)
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous
                    IconButton(
                        onClick = { playerViewModel.previous() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipPrevious,
                            contentDescription = "Previous",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Play/Pause
                    IconButton(
                        onClick = { playerViewModel.togglePlayPause() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        val icon = when (playbackState) {
                            PlaybackState.PLAYING, PlaybackState.BUFFERING -> Icons.Rounded.Pause
                            else -> Icons.Rounded.PlayArrow
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = if (playbackState == PlaybackState.PLAYING) "Pause" else "Play",
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Next
                    IconButton(
                        onClick = { playerViewModel.next() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = "Next",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Secondary controls (right)
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Shuffle
                    IconButton(
                        onClick = { playerViewModel.toggleShuffle() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (isShuffleEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Repeat
                    IconButton(
                        onClick = { playerViewModel.toggleRepeatMode() },
                        modifier = Modifier.size(28.dp)
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
                            },
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Volume control
                    IconButton(
                        onClick = { playerViewModel.toggleMute() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        val volumeIcon = when {
                            volume == 0f -> Icons.Rounded.VolumeOff
                            volume < 0.3f -> Icons.Rounded.VolumeMute
                            volume < 0.7f -> Icons.Rounded.VolumeDown
                            else -> Icons.Rounded.VolumeUp
                        }
                        Icon(
                            imageVector = volumeIcon,
                            contentDescription = "Volume",
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Slider(
                        value = volume,
                        onValueChange = { playerViewModel.setVolume(it) },
                        modifier = Modifier.width(100.dp).height(20.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }
        }
    }
}