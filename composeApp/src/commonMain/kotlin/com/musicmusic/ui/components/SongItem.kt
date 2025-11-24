package com.musicmusic.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.musicmusic.domain.model.Song

/**
 * Item de lista para mostrar una canción.
 *
 * Muestra:
 * - Miniatura de carátula
 * - Título y artista
 * - Duración
 * - Botón de favorito
 * - Menú de opciones
 */
@Composable
fun SongItem(
    song: Song,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    isSelected: Boolean = false,
    onAddToQueue: (() -> Unit)? = null,
    onShowAlbum: (() -> Unit)? = null,
    onShowArtist: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    onSelect: (() -> Unit)? = null,
    onToggleSelection: ((Boolean) -> Unit)? = null  // Nuevo: para Ctrl+click
) {
    var showMenu by remember { mutableStateOf(false) }

    val backgroundColor = when {
        isPlaying -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        isSelected -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.surface
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        // Single click: reproduce inmediatamente (sin delay)
                        onClick()
                    },
                    onDoubleTap = {
                        // Double click: reproduce
                        onDoubleClick?.invoke() ?: onClick()
                    }
                )
            },
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                    text = "${song.artist} • ${song.album}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Duración
            Text(
                text = formatDuration(song.duration),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Botón de favorito
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (song.isFavorite) {
                        Icons.Rounded.Favorite
                    } else {
                        Icons.Rounded.FavoriteBorder
                    },
                    contentDescription = "Toggle favorite",
                    tint = if (song.isFavorite) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // Menú de opciones
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "More options",
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    // Add to Queue
                    onAddToQueue?.let { addToQueue ->
                        DropdownMenuItem(
                            text = { Text("Add to Queue") },
                            onClick = {
                                addToQueue()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.QueueMusic,
                                    contentDescription = null
                                )
                            }
                        )
                    }

                    // Add to Favorites (alternative to button)
                    DropdownMenuItem(
                        text = { Text(if (song.isFavorite) "Remove from Favorites" else "Add to Favorites") },
                        onClick = {
                            onToggleFavorite()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (song.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = null
                            )
                        }
                    )

                    // Show Album
                    onShowAlbum?.let { showAlbum ->
                        DropdownMenuItem(
                            text = { Text("Show Album") },
                            onClick = {
                                showAlbum()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Album,
                                    contentDescription = null
                                )
                            }
                        )
                    }

                    // Show Artist
                    onShowArtist?.let { showArtist ->
                        DropdownMenuItem(
                            text = { Text("Show Artist") },
                            onClick = {
                                showArtist()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = null
                                )
                            }
                        )
                    }

                    // Delete from Library
                    onDelete?.let { delete ->
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Delete from Library") },
                            onClick = {
                                delete()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = MaterialTheme.colorScheme.error
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Formatea duración en milisegundos a formato MM:SS
 */
private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
