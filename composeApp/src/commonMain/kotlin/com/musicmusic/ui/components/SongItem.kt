package com.musicmusic.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.musicmusic.domain.model.Song
import com.musicmusic.utils.TimeUtils

/**
 * List item to display a song.
 *
 * Shows:
 * - Album cover thumbnail
 * - Title and artist
 * - Duration
 * - Favorite button
 * - Options menu
 *
 * Click behavior:
 * - Single click: Select
 * - Double click: Play
 * - Ctrl + Click: Multi-select
 * - Shift + Click: Range select
 */
@OptIn(ExperimentalFoundationApi::class)
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
    onCtrlClick: (() -> Unit)? = null,
    onShiftClick: (() -> Unit)? = null
) {
    var showMenu by remember { mutableStateOf(false) }
    var isCtrlPressed by remember { mutableStateOf(false) }
    var isShiftPressed by remember { mutableStateOf(false) }

    val backgroundColor = when {
        isPlaying -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        isSelected -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.surface
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .onPreviewKeyEvent { keyEvent ->
                // Track modifier keys
                when {
                    keyEvent.key == Key.CtrlLeft || keyEvent.key == Key.CtrlRight -> {
                        isCtrlPressed = keyEvent.type == KeyEventType.KeyDown
                        false
                    }
                    keyEvent.key == Key.ShiftLeft || keyEvent.key == Key.ShiftRight -> {
                        isShiftPressed = keyEvent.type == KeyEventType.KeyDown
                        false
                    }
                    else -> false
                }
            }
            .combinedClickable(
                onClick = {
                    // Single click: handle based on modifiers
                    when {
                        isCtrlPressed -> onCtrlClick?.invoke()
                        isShiftPressed -> onShiftClick?.invoke()
                        else -> onSelect?.invoke()
                    }
                },
                onDoubleClick = {
                    // Double click: play
                    onDoubleClick?.invoke() ?: onClick()
                }
            ),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cover
            AlbumCoverThumbnail(
                coverArtPath = song.coverArtPath,
                size = 40.dp
            )

            // Song info
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

            // Duration
            Text(
                text = TimeUtils.formatDuration(song.duration),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Favorite button
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

            // Options menu
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
