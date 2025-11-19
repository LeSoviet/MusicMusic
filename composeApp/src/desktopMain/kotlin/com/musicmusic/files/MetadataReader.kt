package com.musicmusic.files

import com.musicmusic.domain.model.AudioMetadata
import org.jaudiotagger.audio.AudioFile
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.images.Artwork
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Lector de metadatos de archivos de audio usando JAudioTagger.
 * 
 * JAudioTagger soporta múltiples formatos:
 * - MP3 (ID3v1, ID3v2.2, ID3v2.3, ID3v2.4)
 * - MP4/M4A (iTunes)
 * - OGG Vorbis
 * - FLAC
 * - WMA
 * - WAV
 */
class MetadataReader {
    
    init {
        // Desactivar los logs verbosos de JAudioTagger
        Logger.getLogger("org.jaudiotagger").level = Level.OFF
    }
    
    /**
     * Extrae los metadatos de un archivo de audio.
     * 
     * @param file Archivo de audio
     * @return AudioMetadata con la información extraída, o null si hay error
     */
    fun readMetadata(file: File): AudioMetadata? {
        if (!file.exists() || !file.isFile) {
            return null
        }
        
        return try {
            val audioFile: AudioFile = AudioFileIO.read(file)
            val tag = audioFile.tag
            val header = audioFile.audioHeader
            
            // Extraer carátula si existe
            val coverArtData = try {
                tag?.firstArtwork?.binaryData
            } catch (e: Exception) {
                null
            }
            
            AudioMetadata(
                title = tag?.getFirst(FieldKey.TITLE)?.takeIf { it.isNotBlank() },
                artist = tag?.getFirst(FieldKey.ARTIST)?.takeIf { it.isNotBlank() },
                album = tag?.getFirst(FieldKey.ALBUM)?.takeIf { it.isNotBlank() },
                albumArtist = tag?.getFirst(FieldKey.ALBUM_ARTIST)?.takeIf { it.isNotBlank() },
                genre = tag?.getFirst(FieldKey.GENRE)?.takeIf { it.isNotBlank() },
                year = tag?.getFirst(FieldKey.YEAR)?.toIntOrNull(),
                trackNumber = tag?.getFirst(FieldKey.TRACK)?.toIntOrNull(),
                discNumber = tag?.getFirst(FieldKey.DISC_NO)?.toIntOrNull(),
                duration = (header.trackLength * 1000).toLong(), // Convertir a ms
                bitrate = header.bitRateAsNumber?.toInt(),
                sampleRate = header.sampleRateAsNumber,
                coverArtData = coverArtData
            )
        } catch (e: Exception) {
            println("Error reading metadata from ${file.name}: ${e.message}")
            null
        }
    }
    
    /**
     * Extrae solo la carátula de un archivo de audio.
     * 
     * @param file Archivo de audio
     * @return Array de bytes con la imagen, o null si no existe
     */
    fun extractCoverArt(file: File): ByteArray? {
        if (!file.exists() || !file.isFile) {
            return null
        }
        
        return try {
            val audioFile: AudioFile = AudioFileIO.read(file)
            val tag = audioFile.tag
            
            tag?.firstArtwork?.binaryData
        } catch (e: Exception) {
            println("Error extracting cover art from ${file.name}: ${e.message}")
            null
        }
    }
    
    /**
     * Verifica si un archivo tiene formato de audio soportado.
     * 
     * @param file Archivo a verificar
     * @return true si es un formato de audio soportado
     */
    fun isAudioFile(file: File): Boolean {
        if (!file.exists() || !file.isFile) {
            return false
        }
        
        val supportedExtensions = setOf(
            "mp3", "m4a", "m4p", "m4b", "m4r", "aac",
            "flac", "ogg", "oga", "opus",
            "wav", "wma", "ape", "wv"
        )
        
        val extension = file.extension.lowercase()
        return extension in supportedExtensions
    }
    
    /**
     * Genera un nombre de archivo apropiado para la carátula.
     * 
     * @param metadata Metadatos del audio
     * @return Nombre de archivo sugerido (ej: "Artist - Album.jpg")
     */
    fun generateCoverArtFileName(metadata: AudioMetadata): String {
        val artist = metadata.artist ?: "Unknown Artist"
        val album = metadata.album ?: "Unknown Album"
        
        // Sanitizar nombres de archivo
        val sanitizedArtist = artist.replace(Regex("[^a-zA-Z0-9\\s-]"), "")
        val sanitizedAlbum = album.replace(Regex("[^a-zA-Z0-9\\s-]"), "")
        
        return "$sanitizedArtist - $sanitizedAlbum.jpg"
    }
}
