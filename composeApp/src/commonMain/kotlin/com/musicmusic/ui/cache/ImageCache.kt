package com.musicmusic.ui.cache

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.skia.Image
import java.io.File
import java.util.LinkedHashMap

/**
 * Caché LRU (Least Recently Used) para carátulas de álbumes
 * 
 * Optimiza el rendimiento evitando cargar la misma imagen múltiples veces
 * desde disco. Usa una estrategia LRU para mantener en memoria solo las
 * imágenes más recientemente usadas.
 */
class ImageCache(
    private val maxSize: Int = 100 // Máximo 100 imágenes en caché
) {
    private val cache = object : LinkedHashMap<String, ImageBitmap>(maxSize, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, ImageBitmap>?): Boolean {
            return size > maxSize
        }
    }
    
    private val mutex = Mutex()
    private var hits = 0L
    private var misses = 0L
    
    /**
     * Obtiene una imagen del caché o la carga desde disco
     */
    suspend fun getOrLoad(key: String, loader: suspend () -> ByteArray?): ImageBitmap? {
        // Intentar obtener del caché
        mutex.withLock {
            cache[key]?.let {
                hits++
                return it
            }
            misses++
        }
        
        // Cargar desde disco
        val bytes = loader() ?: return null
        val image = try {
            Image.makeFromEncoded(bytes).toComposeImageBitmap()
        } catch (e: Exception) {
            null
        }
        
        // Guardar en caché
        if (image != null) {
            mutex.withLock {
                cache[key] = image
            }
        }
        
        return image
    }
    
    /**
     * Elimina una entrada del caché
     */
    suspend fun remove(key: String) {
        mutex.withLock {
            cache.remove(key)
        }
    }
    
    /**
     * Limpia todo el caché
     */
    suspend fun clear() {
        mutex.withLock {
            cache.clear()
            hits = 0
            misses = 0
        }
    }
    
    /**
     * Obtiene estadísticas del caché
     */
    suspend fun getStats(): CacheStats {
        return mutex.withLock {
            CacheStats(
                size = cache.size,
                maxSize = maxSize,
                hits = hits,
                misses = misses,
                hitRate = if (hits + misses > 0) hits.toFloat() / (hits + misses) else 0f
            )
        }
    }
}

/**
 * Estadísticas del caché
 */
data class CacheStats(
    val size: Int,
    val maxSize: Int,
    val hits: Long,
    val misses: Long,
    val hitRate: Float
)

/**
 * Singleton global del caché de imágenes
 */
object GlobalImageCache {
    val instance = ImageCache()
}
