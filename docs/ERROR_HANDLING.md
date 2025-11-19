# Error Handling System

## Overview

MusicMusic implementa un sistema centralizado de manejo de errores que proporciona:

- **Clasificación de errores por tipo y severidad**
- **Observable error stream para la UI**
- **Logging automático con formato consistente**
- **Mensajes user-friendly**
- **Integración con todo el stack de la app**

## Architecture

```
┌─────────────────┐
│   Repositories  │──┐
│   AudioPlayer   │  │
│   ViewModels    │  ├──> ErrorHandler ──> UI (ErrorSnackbar)
│   FileScanner   │  │           │
│   etc.          │──┘           │
└─────────────────┘           Logs
```

### Components

#### 1. `AppError` (Sealed Class)

Define todos los tipos de errores de la aplicación:

```kotlin
sealed class AppError {
    data class FileNotFound(val path: String, val reason: String?)
    data class ScanError(val directory: String, val message: String)
    data class DatabaseError(val operation: String, val message: String)
    data class PlaybackError(val songPath: String, val message: String)
    data class NetworkError(val url: String, val message: String)
    data class MetadataError(val file: String, val message: String)
    data class PermissionError(val path: String, val message: String)
    data class PreferencesError(val key: String, val message: String)
    data class UnknownError(val message: String, val throwable: Throwable?)
}
```

Cada tipo de error incluye:
- Contexto específico del error (path, url, etc.)
- Mensaje descriptivo
- Severidad (LOW, MEDIUM, HIGH)
- Mensaje user-friendly

#### 2. `ErrorHandler` (Singleton)

Manejador centralizado de errores:

```kotlin
class ErrorHandler {
    val errors: SharedFlow<AppError>  // Observable por la UI
    
    suspend fun handleError(error: AppError)
    suspend fun handleException(throwable: Throwable, context: String?)
}
```

**Features:**
- `SharedFlow` con buffer de 10 errores
- Emisión no bloqueante
- Logging automático según severidad
- Conversión de excepciones Java a `AppError`

#### 3. `ErrorSnackbar` (Composable)

Componente UI para mostrar errores:

```kotlin
@Composable
fun ErrorSnackbar(
    error: AppError?,
    onDismiss: () -> Unit
)
```

**Features:**
- Auto-dismiss después de 5 segundos (errores LOW)
- Colores según severidad:
  - HIGH: error color (rojo)
  - MEDIUM: warning color (naranja)
  - LOW: surface variant (gris)
- Animaciones de entrada/salida
- Botón de cerrar manual

## Usage

### 1. Handling Errors in Repositories

```kotlin
class MusicRepository(
    private val errorHandler: ErrorHandler
) {
    suspend fun scanDirectory(path: String) {
        try {
            val dir = File(path)
            
            if (!dir.exists()) {
                errorHandler.handleError(
                    AppError.FileNotFound(path, "Directory does not exist")
                )
                return
            }
            
            // ... operación riesgosa
        } catch (e: Exception) {
            errorHandler.handleException(e, path)
        }
    }
}
```

### 2. Handling Errors in Audio Player

```kotlin
class VlcjAudioPlayer(
    private val errorHandler: ErrorHandler?
) {
    private fun setupMediaPlayerEvents() {
        mediaPlayer.events().addMediaPlayerEventListener(object : MediaPlayerEventAdapter() {
            override fun error(mediaPlayer: MediaPlayer) {
                scope.launch {
                    errorHandler?.handleError(
                        AppError.PlaybackError(
                            currentSong?.filePath ?: "unknown",
                            "Media player error"
                        )
                    )
                }
            }
        })
    }
}
```

### 3. Observing Errors in UI

```kotlin
@Composable
fun App() {
    val errorHandler = koinInject<ErrorHandler>()
    val latestError = errorHandler.errors.collectAsState(initial = null).value
    var displayedError by remember { mutableStateOf<AppError?>(null) }
    
    LaunchedEffect(latestError) {
        if (latestError != null) {
            displayedError = latestError
        }
    }
    
    // ... UI layout
    
    ErrorSnackbar(
        error = displayedError,
        onDismiss = { displayedError = null }
    )
}
```

## Error Severity Levels

### LOW
- **Impacto:** Error menor, no afecta funcionalidad crítica
- **Ejemplos:** 
  - Archivo de audio sin metadata
  - Preferencia no encontrada (usa default)
  - Cover art no encontrado
- **UI:** Se muestra 5 segundos, auto-dismiss
- **Log:** `ℹ️ INFO`

### MEDIUM
- **Impacto:** Afecta funcionalidad pero la app sigue usable
- **Ejemplos:**
  - Error al escanear directorio (algunos archivos fallan)
  - Error de base de datos en operación no crítica
  - Permisos de lectura denegados
- **UI:** Snackbar amarillo, requiere cierre manual
- **Log:** `⚠️ WARNING`

### HIGH
- **Impacto:** Error crítico que afecta funcionalidad principal
- **Ejemplos:**
  - Error de reproducción de audio
  - Error de red al hacer streaming
  - Error desconocido con excepción
- **UI:** Snackbar rojo, requiere cierre manual
- **Log:** `❌ ERROR` con stack trace si hay throwable

## Logging Format

```
❌ ERROR [PlaybackError]: Cannot play audio file: File not found
⚠️ WARNING [ScanError]: Error scanning music folder: Permission denied
ℹ️ INFO [MetadataError]: Error reading file metadata: Unsupported format
```

## Integration with Koin

```kotlin
val desktopModule = module {
    // ErrorHandler singleton
    single { ErrorHandler() }
    
    // Inject in repositories
    single {
        MusicRepository(
            errorHandler = get()
        )
    }
    
    // Inject in audio player
    single<AudioPlayer> {
        VlcjAudioPlayer(
            errorHandler = get()
        )
    }
}
```

## Best Practices

### 1. Always Provide Context
```kotlin
// ❌ Bad
errorHandler.handleError(AppError.FileNotFound("", ""))

// ✅ Good
errorHandler.handleError(
    AppError.FileNotFound(
        path = file.absolutePath,
        reason = "File does not exist or is not accessible"
    )
)
```

### 2. Use Specific Error Types
```kotlin
// ❌ Bad
errorHandler.handleError(AppError.UnknownError("Can't read file"))

// ✅ Good
errorHandler.handleError(AppError.PermissionError(path, "No read permission"))
```

### 3. Don't Swallow Exceptions
```kotlin
// ❌ Bad
try {
    riskyOperation()
} catch (e: Exception) {
    // Silent fail
}

// ✅ Good
try {
    riskyOperation()
} catch (e: Exception) {
    errorHandler.handleException(e, "context")
}
```

### 4. Handle Errors Early
```kotlin
suspend fun scanDirectory(path: String) {
    // Validar ANTES de hacer trabajo pesado
    if (!File(path).exists()) {
        errorHandler.handleError(AppError.FileNotFound(path))
        return
    }
    
    // ... operaciones pesadas
}
```

## Testing

### Testing Error Handler

```kotlin
@Test
fun `error handler emits errors to flow`() = runTest {
    val errorHandler = ErrorHandler()
    val errors = mutableListOf<AppError>()
    
    val job = launch {
        errorHandler.errors.collect { errors.add(it) }
    }
    
    val error = AppError.FileNotFound("/test/path", "Not found")
    errorHandler.handleError(error)
    
    advanceUntilIdle()
    assertEquals(1, errors.size)
    assertEquals(error, errors[0])
    
    job.cancel()
}
```

### Testing UI with Errors

```kotlin
@Test
fun `error snackbar shows and dismisses`() = runComposeUiTest {
    var displayedError: AppError? = AppError.FileNotFound("test.mp3")
    
    setContent {
        ErrorSnackbar(
            error = displayedError,
            onDismiss = { displayedError = null }
        )
    }
    
    // Verificar que se muestra
    onNodeWithText("File not found: test.mp3").assertExists()
    
    // Cerrar manualmente
    onNodeWithContentDescription("Close").performClick()
    
    // Verificar que se ocultó
    onNodeWithText("File not found: test.mp3").assertDoesNotExist()
}
```

## Migration from println

Antes del `ErrorHandler`, el código usaba `println`:

```kotlin
// ❌ Antes
try {
    riskyOperation()
} catch (e: Exception) {
    println("⚠️ Error: ${e.message}")
}
```

Ahora con `ErrorHandler`:

```kotlin
// ✅ Después
try {
    riskyOperation()
} catch (e: Exception) {
    errorHandler.handleException(e, "operation context")
}
```

## Performance Considerations

- **SharedFlow:** No buffer replay, solo nuevos errores
- **extraBufferCapacity:** 10 errores en buffer antes de backpressure
- **Coroutines:** Emisión en suspend functions, no bloquea
- **UI:** Solo 1 error mostrado a la vez (última emisión)

## Future Enhancements

- [ ] Error history para debugging
- [ ] Analytics integration (contar errores por tipo)
- [ ] Error recovery strategies (retry logic)
- [ ] User-configurable error verbosity
- [ ] Export error logs to file
- [ ] Localization de mensajes de error

---

**Created:** 2025
**Last Updated:** 2025
**Status:** ✅ Implemented
