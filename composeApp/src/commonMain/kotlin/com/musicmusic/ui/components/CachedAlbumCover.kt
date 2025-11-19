package com.musicmusic.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.musicmusic.ui.cache.GlobalImageCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Imagen optimizada con lazy loading y caché
 * 
 * Features:
 * - Carga lazy (solo cuando es visible)
 * - Caché LRU para evitar recargas
 * - Placeholder mientras carga
 * - Fallback si no hay imagen
 */
@Composable
fun CachedAlbumCover(
    coverPath: String?,
    albumName: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    var imageState by remember(coverPath) { mutableStateOf<ImageLoadState>(ImageLoadState.Loading) }
    val scope = rememberCoroutineScope()
    
    // Cargar imagen
    LaunchedEffect(coverPath) {
        if (coverPath.isNullOrEmpty()) {
            imageState = ImageLoadState.Empty
            return@LaunchedEffect
        }
        
        scope.launch {
            try {
                val image = withContext(Dispatchers.IO) {
                    GlobalImageCache.instance.getOrLoad(coverPath) {
                        val file = File(coverPath)
                        if (file.exists()) file.readBytes() else null
                    }
                }
                
                imageState = if (image != null) {
                    ImageLoadState.Success(image)
                } else {
                    ImageLoadState.Empty
                }
            } catch (e: Exception) {
                imageState = ImageLoadState.Error(e)
            }
        }
    }
    
    // Renderizar según estado
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        when (val state = imageState) {
            is ImageLoadState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            is ImageLoadState.Success -> {
                Image(
                    bitmap = state.image,
                    contentDescription = "Carátula de $albumName",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale
                )
            }
            is ImageLoadState.Empty, is ImageLoadState.Error -> {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Sin carátula",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

/**
 * Estados de carga de imagen
 */
private sealed class ImageLoadState {
    object Loading : ImageLoadState()
    data class Success(val image: ImageBitmap) : ImageLoadState()
    object Empty : ImageLoadState()
    data class Error(val exception: Exception) : ImageLoadState()
}
