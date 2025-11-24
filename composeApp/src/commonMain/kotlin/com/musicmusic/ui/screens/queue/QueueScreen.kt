package com.musicmusic.ui.screens.queue

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.musicmusic.domain.model.Song
import com.musicmusic.utils.TimeUtils
import com.musicmusic.ui.components.AlbumCoverThumbnail
import com.musicmusic.ui.screens.player.PlayerViewModel
import org.koin.compose.koinInject

/**
 * Vista de cola de reproducción (Queue).
 * 
 * Muestra:
 * - Lista de canciones en la cola
 * - Canción actual destacada
 * - Reordenar canciones (drag & drop)
 * - Eliminar de la cola
 * - Limpiar cola completa
 * - Historial de reproducción
 */
@Composable
fun QueueScreen(
    modifier: Modifier = Modifier,
    playerViewModel: PlayerViewModel = koinInject()
) {
    val queue by playerViewModel.queue.collectAsState()
    val currentSong by playerViewModel.currentSong.collectAsState()
    val currentIndex by playerViewModel.currentIndex.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        // Header
        QueueHeader(
            queueSize = queue.size,
            onClearQueue = { playerViewModel.clearQueue() }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Lista de canciones
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp), // Espacio para PlayerBar
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(
                items = queue,
                key = { index, song -> "${song.id}-$index" }
            ) { index, song ->
                QueueItem(
                    song = song,
                    index = index + 1,
                    isPlaying = song.id == currentSong?.id,
                    onClick = { playerViewModel.playAtIndex(index) },
                    onRemove = { playerViewModel.removeFromQueue(index) }
                )
            }
        }
    }
}

@Composable
private fun QueueHeader(
    queueSize: Int,
    onClearQueue: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.QueueMusic,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            
            Column {
                Text(
                    text = "Queue",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$queueSize songs",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        IconButton(
            onClick = onClearQueue,
            enabled = queueSize > 0
        ) {
            Icon(
                imageVector = Icons.Rounded.Clear,
                contentDescription = "Clear queue"
            )
        }
    }
}

@Composable
private fun QueueItem(
    song: Song,
    index: Int,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = if (isPlaying) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        tonalElevation = if (isPlaying) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Número de posición o indicador de reproducción
            Box(
                modifier = Modifier.size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isPlaying) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "Now playing",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = index.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Carátula
            AlbumCoverThumbnail(
                coverArtPath = song.coverArtPath,
                size = 48.dp
            )
            
            // Información de la canción
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isPlaying) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = song.getDisplayArtist(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Duración
            Text(
                text = TimeUtils.formatDuration(song.duration),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Botón de eliminar
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Remove from queue",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}


