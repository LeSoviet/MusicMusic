package com.musicmusic.files

import com.musicmusic.domain.model.AudioMetadata
import com.musicmusic.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.*
import java.security.MessageDigest
import java.util.*

/**
 * Progreso del escaneo de archivos.
 */
data class ScanProgress(
    val currentFile: String,
    val filesProcessed: Int,
    val totalFiles: Int,
    val percentage: Float
)

/**
 * Escáner de sistema de archivos para encontrar y procesar archivos de audio.
 * 
 * Funcionalidades:
 * - Escaneo recursivo de directorios
 * - Extracción de metadatos
 * - Guardado de carátulas
 * - Caché de checksums para evitar re-escaneos
 */
class FileScanner(
    private val metadataReader: MetadataReader = MetadataReader()
) {
    
    companion object {
        private const val COVER_ART_DIR = ".musicmusic/covers"
    }
    
    /**
     * Escanea un directorio en busca de archivos de audio.
     * 
     * @param directoryPath Ruta al directorio a escanear
     * @return Flow que emite el progreso del escaneo
     */
    fun scanDirectory(directoryPath: String): Flow<ScanProgress> = flow {
        withContext(Dispatchers.IO) {
            val directory = File(directoryPath)
            
            if (!directory.exists() || !directory.isDirectory) {
                throw IllegalArgumentException("Invalid directory: $directoryPath")
            }
            
            // Obtener todos los archivos de audio recursivamente
            val audioFiles = findAudioFiles(directory)
            val totalFiles = audioFiles.size
            
            audioFiles.forEachIndexed { index, file ->
                emit(
                    ScanProgress(
                        currentFile = file.name,
                        filesProcessed = index + 1,
                        totalFiles = totalFiles,
                        percentage = (index + 1).toFloat() / totalFiles
                    )
                )
            }
        }
    }
    
    /**
     * Encuentra todos los archivos de audio en un directorio recursivamente.
     * 
     * @param directory Directorio raíz
     * @return Lista de archivos de audio encontrados
     */
    fun findAudioFiles(directory: File): List<File> {
        val audioFiles = mutableListOf<File>()
        
        directory.walkTopDown()
            .filter { it.isFile }
            .filter { metadataReader.isAudioFile(it) }
            .forEach { audioFiles.add(it) }
        
        return audioFiles
    }
    
    /**
     * Procesa un archivo de audio y crea un objeto Song.
     * 
     * @param file Archivo de audio
     * @param extractCoverArt Si se debe extraer y guardar la carátula
     * @return Song creado, o null si hay error
     */
    suspend fun processSongFile(
        file: File,
        extractCoverArt: Boolean = true
    ): Song? = withContext(Dispatchers.IO) {
        val metadata = metadataReader.readMetadata(file) ?: return@withContext null
        
        // Generar ID único basado en la ruta del archivo
        val songId = generateSongId(file)
        
        // Guardar carátula si existe y se solicita
        val coverArtPath = if (extractCoverArt && metadata.coverArtData != null) {
            saveCoverArt(metadata)
        } else null
        
        // Crear objeto Song
        Song(
            id = songId,
            title = metadata.title ?: file.nameWithoutExtension,
            artist = metadata.artist ?: "Unknown Artist",
            album = metadata.album ?: "Unknown Album",
            albumArtist = metadata.albumArtist,
            genre = metadata.genre,
            year = metadata.year,
            duration = metadata.duration,
            trackNumber = metadata.trackNumber,
            discNumber = metadata.discNumber,
            filePath = file.absolutePath,
            coverArtPath = coverArtPath,
            bitrate = metadata.bitrate,
            sampleRate = metadata.sampleRate,
            fileSize = file.length(),
            dateAdded = System.currentTimeMillis()
        )
    }
    
    /**
     * Procesa múltiples archivos de audio.
     * 
     * @param files Lista de archivos a procesar
     * @param onProgress Callback de progreso
     * @return Lista de canciones procesadas exitosamente
     */
    suspend fun processSongFiles(
        files: List<File>,
        onProgress: ((Int, Int) -> Unit)? = null
    ): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        
        files.forEachIndexed { index, file ->
            onProgress?.invoke(index + 1, files.size)
            
            processSongFile(file)?.let { song ->
                songs.add(song)
            }
        }
        
        songs
    }
    
    /**
     * Genera un ID único para una canción basado en su archivo.
     * Usa SHA-256 hash de la ruta absoluta.
     * 
     * @param file Archivo de audio
     * @return ID único
     */
    private fun generateSongId(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(file.absolutePath.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Guarda la carátula en el sistema de archivos.
     * 
     * @param metadata Metadatos que contienen la carátula
     * @return Ruta al archivo guardado, o null si hay error
     */
    private fun saveCoverArt(metadata: AudioMetadata): String? {
        val coverArtData = metadata.coverArtData ?: return null
        
        // Crear directorio de carátulas si no existe
        val coverDir = File(System.getProperty("user.home"), COVER_ART_DIR)
        if (!coverDir.exists()) {
            coverDir.mkdirs()
        }
        
        // Generar nombre de archivo único
        val fileName = metadataReader.generateCoverArtFileName(metadata)
        val coverFile = File(coverDir, fileName)
        
        return try {
            // Solo guardar si no existe ya
            if (!coverFile.exists()) {
                coverFile.writeBytes(coverArtData)
            }
            coverFile.absolutePath
        } catch (e: Exception) {
            println("Error saving cover art: ${e.message}")
            null
        }
    }
    
    /**
     * Observa cambios en un directorio (para actualización automática).
     * 
     * @param directoryPath Ruta al directorio a observar
     * @param onChange Callback cuando hay cambios
     */
    fun watchDirectory(
        directoryPath: String,
        onChange: (WatchEvent.Kind<*>, Path) -> Unit
    ) {
        val watchService = FileSystems.getDefault().newWatchService()
        val path = Paths.get(directoryPath)
        
        path.register(
            watchService,
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_DELETE,
            StandardWatchEventKinds.ENTRY_MODIFY
        )
        
        while (true) {
            val key = watchService.take()
            
            for (event in key.pollEvents()) {
                @Suppress("UNCHECKED_CAST")
                val ev = event as WatchEvent<Path>
                onChange(ev.kind(), ev.context())
            }
            
            if (!key.reset()) {
                break
            }
        }
    }
    
    /**
     * Calcula estadísticas de una biblioteca de música.
     * 
     * @param songs Lista de canciones
     * @return Mapa con estadísticas
     */
    fun calculateLibraryStats(songs: List<Song>): Map<String, Any> {
        val totalDuration = songs.sumOf { it.duration }
        val totalSize = songs.sumOf { it.fileSize }
        val artists = songs.map { it.artist }.distinct().size
        val albums = songs.map { it.album }.distinct().size
        
        return mapOf(
            "totalSongs" to songs.size,
            "totalDuration" to totalDuration,
            "totalSize" to totalSize,
            "totalArtists" to artists,
            "totalAlbums" to albums,
            "averageDuration" to if (songs.isNotEmpty()) totalDuration / songs.size else 0L
        )
    }
}
