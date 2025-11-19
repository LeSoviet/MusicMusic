package com.musicmusic.domain.error

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Manejador centralizado de errores de la aplicación.
 * 
 * Gestiona todos los errores de la aplicación de forma centralizada,
 * proporcionando:
 * - Flujo de errores observable para la UI
 * - Logging automático de errores
 * - Clasificación por severidad
 * - Mensajes de error user-friendly
 * 
 * Uso:
 * ```kotlin
 * try {
 *     // ... operación riesgosa
 * } catch (e: Exception) {
 *     errorHandler.handleError(AppError.DatabaseError("insert", e.message))
 * }
 * ```
 */
class ErrorHandler {
    private val _errors = MutableSharedFlow<AppError>(
        replay = 0,
        extraBufferCapacity = 10
    )
    
    /**
     * Flow de errores que la UI puede observar.
     */
    val errors: SharedFlow<AppError> = _errors.asSharedFlow()
    
    /**
     * Maneja un error de la aplicación.
     * 
     * Este método:
     * 1. Emite el error al flow para que la UI lo muestre
     * 2. Registra el error en los logs
     * 3. Clasifica el error por severidad
     * 
     * @param error El error a manejar
     */
    suspend fun handleError(error: AppError) {
        // Emitir el error para que la UI lo capture
        _errors.emit(error)
        
        // Log del error según su severidad
        when (error.getSeverity()) {
            ErrorSeverity.HIGH -> logError(error)
            ErrorSeverity.MEDIUM -> logWarning(error)
            ErrorSeverity.LOW -> logInfo(error)
        }
    }
    
    /**
     * Maneja un error directamente desde una excepción.
     * 
     * Convierte automáticamente la excepción en un AppError apropiado.
     * 
     * @param throwable La excepción capturada
     * @param context Contexto adicional del error
     */
    suspend fun handleException(throwable: Throwable, context: String? = null) {
        val error = when (throwable) {
            is java.io.FileNotFoundException -> 
                AppError.FileNotFound(throwable.message ?: "Unknown file", context)
            is java.sql.SQLException ->
                AppError.DatabaseError(context ?: "operation", throwable.message ?: "Unknown error")
            is java.net.UnknownHostException, is java.net.ConnectException ->
                AppError.NetworkError(context ?: "unknown", throwable.message ?: "Connection failed")
            is SecurityException ->
                AppError.PermissionError(context ?: "unknown", throwable.message ?: "Access denied")
            else ->
                AppError.UnknownError(throwable.message ?: "Unknown error", throwable)
        }
        
        handleError(error)
    }
    
    /**
     * Registra un error de alta severidad.
     */
    private fun logError(error: AppError) {
        println("❌ ERROR [${error::class.simpleName}]: ${error.getUserMessage()}")
        
        // Si hay una excepción, imprimir stack trace
        if (error is AppError.UnknownError && error.throwable != null) {
            error.throwable.printStackTrace()
        }
    }
    
    /**
     * Registra un warning de severidad media.
     */
    private fun logWarning(error: AppError) {
        println("⚠️ WARNING [${error::class.simpleName}]: ${error.getUserMessage()}")
    }
    
    /**
     * Registra información de baja severidad.
     */
    private fun logInfo(error: AppError) {
        println("ℹ️ INFO [${error::class.simpleName}]: ${error.getUserMessage()}")
    }
    
    /**
     * Limpia el buffer de errores.
     * 
     * Útil para testing o para limpiar errores antiguos.
     */
    fun clearErrors() {
        // Los errores ya emitidos no se pueden limpiar de SharedFlow
        // pero se puede implementar una lista de errores recientes si es necesario
    }
}
