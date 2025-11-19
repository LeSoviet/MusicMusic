package com.musicmusic.ui.screens.player

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.musicmusic.ui.components.*
import com.musicmusic.domain.model.PlaybackState
import org.koin.compose.koinInject

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
        topBar = { NowPlayingTopBar(onBack = onBack) }
    ) { paddingValues ->

        // 🟢 CONTENEDOR FIJO QUE NO SE DEFORMA
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds() // oculta lo que no entre si se achica la ventana
        ) {
            Column(
                modifier = Modifier
                    .width(1366.dp)        // 🔥 RESOLUCIÓN FIJA
                    .height(768.dp)
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Carátula
                AlbumCoverWithBlur(
                    coverArtPath = currentSong?.coverArtPath,
                    coverSize = 220.dp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Info canción
                SongInfo(
                    title = currentSong?.title ?: "No song playing",
                    artist = currentSong?.getDisplayArtist() ?: "",
                    album = currentSong?.album ?: ""
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Barra de progreso
                Column(modifier = Modifier.fillMaxWidth()) {
                    SeekBar(
                        progress = progress,
                        onSeekStart = {
                            playerViewModel.startSeeking(
                                (playerViewModel.duration.value * it).toLong()
                            )
                        },
                        onSeekChange = { newProgress ->
                            val newPosition =
                                (playerViewModel.duration.value * newProgress).toLong()
                            playerViewModel.updateSeekPosition(newPosition)
                        },
                        onSeekEnd = { playerViewModel.endSeeking() },
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

                Spacer(modifier = Modifier.height(12.dp))

                // Controles principales
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

                Spacer(modifier = Modifier.height(8.dp))

                // Acciones extra
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Favorito
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(
                            imageVector = if (currentSong?.isFavorite == true)
                                Icons.Rounded.Favorite
                            else Icons.Rounded.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (currentSong?.isFavorite == true)
                                MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Volumen
                    VolumeControl(
                        volume = volume,
                        onVolumeChange = { playerViewModel.setVolume(it) },
                        orientation = VolumeOrientation.Horizontal
                    )

                    // Cola
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(
                            imageVector = Icons.Rounded.QueueMusic,
                            contentDescription = "Queue"
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NowPlayingTopBar(onBack: () -> Unit) {
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
