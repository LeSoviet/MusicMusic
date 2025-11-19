package com.musicmusic.domain.error

/**
 * Tipos de errores de la aplicación.
 * 
 * Representa todos los tipos de errores que pueden ocurrir en MusicMusic.
 */
sealed class AppError {
    /**
     * Error de archivo no encontrado o no accesible.
     */
    data class FileNotFound(val path: String, val reason: String? = null) : AppError()
    
    /**
     * Error al escanear el directorio de música.
     */
    data class ScanError(val directory: String, val message: String) : AppError()
    
    /**
     * Error de base de datos.
     */
    data class DatabaseError(val operation: String, val message: String) : AppError()
    
    /**
     * Error de reproducción de audio.
     */
    data class PlaybackError(val songPath: String, val message: String) : AppError()
    
    /**
     * Error de red (streaming de radios).
     */
    data class NetworkError(val url: String, val message: String) : AppError()
    
    /**
     * Error al leer metadatos de archivos de audio.
     */
    data class MetadataError(val file: String, val message: String) : AppError()
    
    /**
     * Error de permisos del sistema de archivos.
     */
    data class PermissionError(val path: String, val message: String) : AppError()
    
    /**
     * Error al cargar preferencias del usuario.
     */
    data class PreferencesError(val key: String, val message: String) : AppError()
    
    /**
     * Error desconocido o genérico.
     */
    data class UnknownError(val message: String, val throwable: Throwable? = null) : AppError()
    
    /**
     * Obtiene un mensaje legible para el usuario.
     */
    fun getUserMessage(): String = when (this) {
        is FileNotFound -> "File not found: $path${reason?.let { " ($it)" } ?: ""}"
        is ScanError -> "Error scanning music folder: $message"
        is DatabaseError -> "Database error during $operation: $message"
        is PlaybackError -> "Cannot play audio file: $message"
        is NetworkError -> "Network error accessing $url: $message"
        is MetadataError -> "Error reading file metadata: $message"
        is PermissionError -> "Permission denied for $path: $message"
        is PreferencesError -> "Error loading preferences: $message"
        is UnknownError -> "An unexpected error occurred: $message"
    }
    
    /**
     * Obtiene el nivel de severidad del error.
     */
    fun getSeverity(): ErrorSeverity = when (this) {
        is PlaybackError, is NetworkError -> ErrorSeverity.HIGH
        is DatabaseError, is ScanError, is PermissionError -> ErrorSeverity.MEDIUM
        is FileNotFound, is MetadataError, is PreferencesError -> ErrorSeverity.LOW
        is UnknownError -> ErrorSeverity.HIGH
    }
}

/**
 * Niveles de severidad de errores.
 */
enum class ErrorSeverity {
    LOW,     // Error menor que no afecta funcionalidad crítica
    MEDIUM,  // Error que afecta funcionalidad pero la app sigue usable
    HIGH     // Error crítico que puede afectar funcionalidad principal
}
