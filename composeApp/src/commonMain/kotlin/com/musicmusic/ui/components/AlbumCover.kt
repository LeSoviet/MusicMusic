package com.musicmusic.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import java.io.File

/**
 * Componente para mostrar la carátula de un álbum.
 * 
 * Características:
 * - Placeholder si no hay imagen
 * - Bordes redondeados
 * - Opcional: fondo blur
 * 
 * @param coverArtPath Ruta a la imagen de carátula
 * @param modifier Modificador de Compose
 * @param size Tamaño de la carátula
 * @param cornerRadius Radio de las esquinas
 */
@Composable
fun AlbumCover(
    coverArtPath: String?,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    cornerRadius: Dp = 16.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        if (coverArtPath != null && File(coverArtPath).exists()) {
            AsyncImage(
                model = File(coverArtPath),
                contentDescription = "Album cover",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            CoverPlaceholder(size = size)
        }
    }
}

/**
 * Carátula grande con fondo blur para NowPlayingScreen.
 */
@Composable
fun AlbumCoverWithBlur(
    coverArtPath: String?,
    modifier: Modifier = Modifier,
    coverSize: Dp = 320.dp
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Fondo blur
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .blur(50.dp)
        ) {
            // Gradient overlay para suavizar
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background.copy(alpha = 0.3f),
                                MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
                            )
                        )
                    )
            )
        }
        
        // Carátula principal
        AlbumCover(
            coverArtPath = coverArtPath,
            size = coverSize,
            cornerRadius = 24.dp,
            modifier = Modifier.padding(top = 32.dp)
        )
    }
}

/**
 * Placeholder cuando no hay carátula.
 */
@Composable
private fun CoverPlaceholder(
    size: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.MusicNote,
            contentDescription = "No cover art",
            modifier = Modifier.size(size * 0.4f),
            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
        )
    }
}

/**
 * Carátula miniatura para listas y mini player.
 */
@Composable
fun AlbumCoverThumbnail(
    coverArtPath: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    AlbumCover(
        coverArtPath = coverArtPath,
        modifier = modifier,
        size = size,
        cornerRadius = 8.dp
    )
}