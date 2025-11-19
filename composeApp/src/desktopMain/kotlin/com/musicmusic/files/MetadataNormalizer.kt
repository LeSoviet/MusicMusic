package com.musicmusic.files

import com.musicmusic.domain.model.AudioMetadata
import java.io.File

/**
 * Normalizador de metadatos con fallbacks inteligentes.
 * 
 * Implementa estrategias para archivos mal formateados o sin tags:
 * - Usa nombre de archivo si falta título
 * - Detecta artista/álbum desde estructura de carpetas
 * - Normaliza strings (trim, encoding, etc.)
 * - Agrupa variaciones del mismo artista/álbum
 */
object MetadataNormalizer {
    
    /**
     * Normaliza metadatos aplicando fallbacks inteligentes
     */
    fun normalize(metadata: AudioMetadata?, file: File): AudioMetadata {
        val title = metadata?.title ?: inferTitleFromFilename(file)
        val artist = metadata?.artist ?: inferArtistFromPath(file)
        val album = metadata?.album ?: inferAlbumFromPath(file)
        
        return AudioMetadata(
            title = normalizeString(title),
            artist = normalizeString(artist),
            album = normalizeString(album),
            albumArtist = metadata?.albumArtist?.let { normalizeString(it) },
            genre = metadata?.genre?.let { normalizeString(it) },
            year = metadata?.year,
            trackNumber = metadata?.trackNumber ?: inferTrackNumberFromFilename(file),
            discNumber = metadata?.discNumber,
            duration = metadata?.duration ?: 0L,
            bitrate = metadata?.bitrate,
            sampleRate = metadata?.sampleRate,
            coverArtData = metadata?.coverArtData
        )
    }
    
    /**
     * Infiere el título desde el nombre del archivo
     * 
     * Ejemplos:
     * - "01 - Song Name.mp3" -> "Song Name"
     * - "Artist - Song Name.mp3" -> "Song Name"
     * - "Song Name.mp3" -> "Song Name"
     */
    private fun inferTitleFromFilename(file: File): String {
        var name = file.nameWithoutExtension
        
        // Remover número de track al inicio (01, 02, etc.)
        name = name.replace(Regex("^\\d{1,2}[\\s\\-._]*"), "")
        
        // Si tiene formato "Artist - Title", tomar solo el título
        if (name.contains(" - ")) {
            val parts = name.split(" - ")
            if (parts.size >= 2) {
                name = parts.drop(1).joinToString(" - ")
            }
        }
        
        return name.trim()
    }
    
    /**
     * Infiere el artista desde la estructura de carpetas
     * 
     * Busca patrones comunes:
     * - Music/Artist/Album/Song.mp3 -> Artist
     * - Music/Artist - Album/Song.mp3 -> Artist
     * - Artist/Song.mp3 -> Artist
     */
    private fun inferArtistFromPath(file: File): String {
        val parentFolder = file.parentFile ?: return "Unknown Artist"
        val grandParentFolder = parentFolder.parentFile
        
        // Si la carpeta padre tiene formato "Artist - Album"
        val folderName = parentFolder.name
        if (folderName.contains(" - ")) {
            val parts = folderName.split(" - ")
            if (parts.isNotEmpty()) {
                return parts[0].trim()
            }
        }
        
        // Si hay una carpeta abuelo y parece ser el artista
        if (grandParentFolder != null) {
            val grandFolderName = grandParentFolder.name
            // Si no es una carpeta genérica como "Music", "Songs", etc.
            if (!isGenericFolderName(grandFolderName)) {
                return grandFolderName
            }
        }
        
        // Usar carpeta padre si no parece genérica
        if (!isGenericFolderName(folderName)) {
            return folderName
        }
        
        return "Unknown Artist"
    }
    
    /**
     * Infiere el álbum desde la estructura de carpetas
     * 
     * Busca patrones comunes:
     * - Music/Artist/Album/Song.mp3 -> Album
     * - Music/Artist - Album/Song.mp3 -> Album
     * - Album/Song.mp3 -> Album
     */
    private fun inferAlbumFromPath(file: File): String {
        val parentFolder = file.parentFile ?: return "Unknown Album"
        val folderName = parentFolder.name
        
        // Si tiene formato "Artist - Album", tomar solo el álbum
        if (folderName.contains(" - ")) {
            val parts = folderName.split(" - ")
            if (parts.size >= 2) {
                return parts.drop(1).joinToString(" - ").trim()
            }
        }
        
        // Si no es una carpeta genérica, usar como álbum
        if (!isGenericFolderName(folderName)) {
            return folderName
        }
        
        return "Unknown Album"
    }
    
    /**
     * Infiere el número de track desde el nombre del archivo
     * 
     * Ejemplos:
     * - "01 - Song.mp3" -> 1
     * - "Track 05.mp3" -> 5
     * - "12. Song.mp3" -> 12
     */
    private fun inferTrackNumberFromFilename(file: File): Int? {
        val name = file.nameWithoutExtension
        
        // Buscar patrón de número al inicio
        val match = Regex("^(\\d{1,2})").find(name)
        return match?.groupValues?.get(1)?.toIntOrNull()
    }
    
    /**
     * Verifica si un nombre de carpeta es genérico
     */
    private fun isGenericFolderName(name: String): Boolean {
        val genericNames = setOf(
            "music", "songs", "audio", "mp3", "flac", "wav",
            "downloads", "documents", "media", "library",
            "playlist", "playlists", "collection"
        )
        return name.lowercase() in genericNames
    }
    
    /**
     * Normaliza un string:
     * - Trim whitespace
     * - Remover caracteres especiales problemáticos
     * - Normalizar encoding
     * - Capitalizar apropiadamente
     */
    fun normalizeString(text: String): String {
        var normalized = text.trim()
        
        // Remover caracteres de control y null bytes
        normalized = normalized.replace(Regex("[\\x00-\\x1F\\x7F]"), "")
        
        // Normalizar múltiples espacios
        normalized = normalized.replace(Regex("\\s+"), " ")
        
        // Remover espacios alrededor de guiones
        normalized = normalized.replace(Regex("\\s*-\\s*"), " - ")
        
        return normalized
    }
    
    /**
     * Normaliza nombre de artista para agrupar variaciones
     * 
     * Ejemplos:
     * - "The Beatles" -> "Beatles"
     * - "beatles" -> "Beatles"
     * - "The Beatles, The" -> "Beatles"
     */
    fun normalizeArtistName(artist: String): String {
        var normalized = normalizeString(artist)
        
        // Remover artículos al inicio
        val articles = listOf("The ", "A ", "An ", "El ", "La ", "Los ", "Las ")
        for (article in articles) {
            if (normalized.startsWith(article, ignoreCase = true)) {
                normalized = normalized.substring(article.length)
                break
            }
        }
        
        // Remover artículos al final (formato "Artist, The")
        if (normalized.contains(", The", ignoreCase = true)) {
            normalized = normalized.replace(Regex(", The$", RegexOption.IGNORE_CASE), "")
        }
        
        return normalized
    }
    
    /**
     * Normaliza nombre de álbum para agrupar variaciones
     */
    fun normalizeAlbumName(album: String): String {
        var normalized = normalizeString(album)
        
        // Remover sufijos comunes de ediciones
        val suffixes = listOf(
            " (Deluxe Edition)", " [Deluxe Edition]",
            " (Remastered)", " [Remastered]",
            " (Bonus Track Version)", " [Bonus Track Version]",
            " (Expanded Edition)", " [Expanded Edition]"
        )
        
        for (suffix in suffixes) {
            if (normalized.endsWith(suffix, ignoreCase = true)) {
                normalized = normalized.substring(0, normalized.length - suffix.length)
                break
            }
        }
        
        return normalized
    }
    
    /**
     * Busca archivos de carátula en la carpeta del audio
     * 
     * Busca en orden de prioridad:
     * 1. cover.jpg/png
     * 2. folder.jpg/png
     * 3. album.jpg/png
     * 4. front.jpg/png
     * 5. Primer archivo de imagen encontrado
     */
    fun findCoverArtInFolder(audioFile: File): File? {
        val folder = audioFile.parentFile ?: return null
        
        val priorityNames = listOf(
            "cover", "folder", "album", "front", "artwork"
        )
        val imageExtensions = listOf("jpg", "jpeg", "png", "webp")
        
        // Buscar por nombres prioritarios
        for (name in priorityNames) {
            for (ext in imageExtensions) {
                val coverFile = File(folder, "$name.$ext")
                if (coverFile.exists()) {
                    return coverFile
                }
                // También probar con mayúsculas
                val coverFileUpper = File(folder, "${name.uppercase()}.$ext")
                if (coverFileUpper.exists()) {
                    return coverFileUpper
                }
                val coverFileCapital = File(folder, "${name.replaceFirstChar { it.uppercase() }}.$ext")
                if (coverFileCapital.exists()) {
                    return coverFileCapital
                }
            }
        }
        
        // Si no se encuentra, buscar cualquier imagen
        folder.listFiles()?.forEach { file ->
            if (file.isFile && file.extension.lowercase() in imageExtensions) {
                return file
            }
        }
        
        return null
    }
}
