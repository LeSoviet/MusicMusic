package com.musicmusic.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.musicmusic.domain.model.PlaybackState
import com.musicmusic.domain.model.RepeatMode

/**
 * Controles de reproducción principales.
 * 
 * Incluye:
 * - Botón anterior
 * - Botón play/pause
 * - Botón siguiente
 * - Botones de shuffle y repeat
 * 
 * @param playbackState Estado actual de reproducción
 * @param isShuffleEnabled Si shuffle está activo
 * @param repeatMode Modo de repetición actual
 * @param onPlayPause Callback para play/pause
 * @param onPrevious Callback para canción anterior
 * @param onNext Callback para canción siguiente
 * @param onShuffle Callback para toggle shuffle
 * @param onRepeat Callback para toggle repeat
 * @param modifier Modificador de Compose
 * @param showSecondaryControls Si mostrar shuffle y repeat
 */
@Composable
fun PlayerControls(
    playbackState: PlaybackState,
    isShuffleEnabled: Boolean,
    repeatMode: RepeatMode,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    modifier: Modifier = Modifier,
    showSecondaryControls: Boolean = true
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showSecondaryControls) {
            // Shuffle button
            IconButton(
                onClick = onShuffle,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Shuffle,
                    contentDescription = "Shuffle",
                    modifier = Modifier.size(20.dp),
                    tint = if (isShuffleEnabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        // Previous button
        IconButton(
            onClick = onPrevious,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipPrevious,
                contentDescription = "Previous",
                modifier = Modifier.size(28.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Play/Pause button (grande y prominente)
        FilledIconButton(
            onClick = onPlayPause,
            modifier = Modifier.size(56.dp),
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
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Next button
        IconButton(
            onClick = onNext,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipNext,
                contentDescription = "Next",
                modifier = Modifier.size(28.dp)
            )
        }
        
        if (showSecondaryControls) {
            Spacer(modifier = Modifier.width(8.dp))
            
            // Repeat button
            IconButton(
                onClick = onRepeat,
                modifier = Modifier.size(40.dp)
            ) {
                val icon = when (repeatMode) {
                    RepeatMode.ONE -> Icons.Rounded.RepeatOne
                    else -> Icons.Rounded.Repeat
                }
                
                Icon(
                    imageVector = icon,
                    contentDescription = "Repeat",
                    modifier = Modifier.size(20.dp),
                    tint = if (repeatMode != RepeatMode.OFF) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

/**
 * Controles compactos para el mini player.
 */
@Composable
fun CompactPlayerControls(
    playbackState: PlaybackState,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Play/Pause compacto
        IconButton(
            onClick = onPlayPause,
            modifier = Modifier.size(40.dp)
        ) {
            val icon = when (playbackState) {
                PlaybackState.PLAYING, PlaybackState.BUFFERING -> Icons.Rounded.Pause
                else -> Icons.Rounded.PlayArrow
            }
            
            Icon(
                imageVector = icon,
                contentDescription = if (playbackState == PlaybackState.PLAYING) "Pause" else "Play"
            )
        }
        
        // Next compacto
        IconButton(
            onClick = onNext,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipNext,
                contentDescription = "Next"
            )
        }
    }
}
