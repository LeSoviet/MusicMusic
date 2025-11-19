package com.musicmusic.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.musicmusic.domain.model.PlaybackState
import com.musicmusic.ui.screens.player.PlayerViewModel
import org.koin.compose.koinInject

/**
 * Barra de reproductor persistente (mini player) en la parte inferior.
 * 
 * Muestra:
 * - Miniatura de carátula
 * - Información básica de la canción
 * - Controles compactos
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
                .height(80.dp)
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
                
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Carátula + Info
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Miniatura de carátula
                        AlbumCoverThumbnail(
                            coverArtPath = currentSong?.coverArtPath,
                            size = 56.dp
                        )
                        
                        // Información de la canción
                        Column(
                            modifier = Modifier.weight(1f),
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
                    
                    // Icono de expansión
                    IconButton(
                        onClick = onClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowUp,
                            contentDescription = "Expand player",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Controles compactos
                    CompactPlayerControls(
                        playbackState = playbackState,
                        onPlayPause = { playerViewModel.togglePlayPause() },
                        onNext = { playerViewModel.next() }
                    )
                }
            }
        }
    }
}
