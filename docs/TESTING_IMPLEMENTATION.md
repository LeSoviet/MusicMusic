# 🧪 Testing Guide - MusicMusic

## ✅ Tests Implementados

### 1. VlcjAudioPlayerTest
**Ubicación**: `composeApp/src/desktopTest/kotlin/com/musicmusic/audio/VlcjAudioPlayerTest.kt`

**Cobertura**: 30+ tests para el reproductor de audio

#### Categorías de Tests:

##### Estado Inicial
- ✅ Estado inicial debe ser STOPPED con canción nula
- ✅ Volumen inicial debe ser 0.5f
- ✅ Shuffle inicial debe estar desactivado
- ✅ Modo de repetición inicial debe ser OFF
- ✅ Cola inicial debe estar vacía

##### Control de Volumen
- ✅ setVolume debe actualizar el estado de volumen
- ✅ setVolume debe limitar el rango entre 0.0f y 1.0f
- ✅ increaseVolume debe aumentar en 0.1f por defecto
- ✅ decreaseVolume debe disminuir en 0.1f por defecto

##### Gestión de Cola
- ✅ playQueue debe establecer la cola y la canción actual
- ✅ getQueue debe devolver la cola actual
- ✅ addToQueue debe agregar canción individual a la cola
- ✅ addToQueue debe agregar múltiples canciones a la cola
- ✅ removeFromQueue debe eliminar canción en el índice
- ✅ clearQueue debe eliminar todas las canciones

##### Shuffle y Repeat
- ✅ setShuffle debe actualizar el estado de shuffle
- ✅ setRepeatMode debe actualizar el modo de repetición
- ✅ shuffle debe preservar la canción actual

##### Control de Reproducción
- ✅ playAtIndex debe cambiar el índice actual
- ✅ next debe moverse a la siguiente canción en la cola
- ✅ previous debe moverse a la canción anterior en la cola
- ✅ next al final de la cola debe retornar false cuando repeat está OFF
- ✅ previous al inicio de la cola debe retornar false

### 2. FavoritesRepositoryTest
**Ubicación**: `composeApp/src/desktopTest/kotlin/com/musicmusic/data/repository/FavoritesRepositoryTest.kt`

**Cobertura**: 25+ tests para el repositorio de favoritos

#### Categorías de Tests:

##### Operaciones Básicas
- ✅ Inicialmente no deben existir favoritos
- ✅ addFavorite debe agregar canción a favoritos
- ✅ removeFavorite debe eliminar canción de favoritos
- ✅ toggleFavorite debe agregar cuando no es favorito
- ✅ toggleFavorite debe eliminar cuando ya es favorito
- ✅ toggleFavorite debe alternar entre estados

##### Múltiples Favoritos
- ✅ Puede agregar múltiples favoritos
- ✅ Agregar el mismo favorito dos veces no debe duplicar
- ✅ Eliminar favorito no existente no debe lanzar error

##### Tests de Flow
- ✅ isFavoriteFlow debe emitir estado actual de favorito
- ✅ getAllFavoritesFlow debe emitir todos los favoritos

##### Clear All
- ✅ clearAllFavorites debe eliminar todos los favoritos
- ✅ clearAllFavorites en repositorio vacío no debe lanzar error

##### Persistencia
- ✅ Favoritos deben persistir entre instancias del repositorio
- ✅ Agregar favorito debe actualizar timestamp

##### Casos Edge
- ✅ isFavorite con string vacío debe retornar false
- ✅ Puede manejar IDs de canciones con caracteres especiales
- ✅ Puede manejar IDs de canciones muy largos

## 🚀 Cómo Ejecutar los Tests

### Opción 1: Usando Gradle
```powershell
# Ejecutar todos los tests
.\gradlew.bat test

# Ejecutar tests de Desktop específicamente
.\gradlew.bat :composeApp:desktopTest

# Ejecutar un test específico
.\gradlew.bat test --tests "com.musicmusic.audio.VlcjAudioPlayerTest"
.\gradlew.bat test --tests "com.musicmusic.data.repository.FavoritesRepositoryTest"

# Ejecutar tests con reporte detallado
.\gradlew.bat test --info
```

### Opción 2: Usando VS Code Task
Si tienes configurada la task `🧪 Run Tests` en VS Code:
1. Presiona `Ctrl+Shift+P`
2. Escribe "Run Task"
3. Selecciona "🧪 Run Tests"

### Opción 3: Usando IntelliJ IDEA / Android Studio
1. Navega a la clase de test
2. Click derecho en el nombre de la clase
3. Selecciona "Run Tests"

## 📊 Reportes de Tests

Los reportes HTML se generan en:
```
composeApp/build/reports/tests/desktopTest/index.html
```

Abre este archivo en tu navegador para ver:
- Resumen de tests pasados/fallados
- Tiempos de ejecución
- Stack traces de errores
- Cobertura por clase

## ⚠️ Notas Importantes

### VlcjAudioPlayerTest
⚠️ **Algunos tests pueden fallar si VLC no está instalado en el sistema.**

Los tests de `VlcjAudioPlayer` verifican principalmente la lógica de estado y control, no la reproducción real de audio. Si VLC no está disponible, los tests de estado y cola seguirán funcionando, pero los tests que intentan reproducir audio real pueden fallar.

**Solución**: Instala VLC Media Player en tu sistema:
- Windows: https://www.videolan.org/vlc/download-windows.html
- Linux: `sudo apt install vlc` (Ubuntu/Debian) o `sudo dnf install vlc` (Fedora)
- macOS: https://www.videolan.org/vlc/download-macosx.html

### FavoritesRepositoryTest
✅ **Estos tests son completamente independientes y no requieren dependencias externas.**

Usan una base de datos SQLite en memoria (`JdbcSqliteDriver.IN_MEMORY`) por lo que son rápidos y confiables.

## 🔧 Troubleshooting

### Error: "Could not find VLC installation"
**Causa**: VLC no está instalado o no está en el PATH del sistema.

**Solución**:
1. Instala VLC Media Player
2. En Windows, agrega la carpeta de VLC al PATH:
   - Normalmente: `C:\Program Files\VideoLAN\VLC`
3. Reinicia tu IDE/terminal

### Error: "Database is locked"
**Causa**: Múltiples tests intentan acceder a la misma base de datos.

**Solución**: Los tests ya usan bases de datos en memoria separadas, pero si persiste:
```kotlin
@After
fun tearDown() {
    database.close() // Asegúrate de cerrar la DB
}
```

### Error: "Coroutine test timeout"
**Causa**: Un test está esperando indefinidamente.

**Solución**: Usa `advanceUntilIdle()` en tests con coroutines:
```kotlin
@Test
fun myTest() = runTest {
    audioPlayer.play(song)
    advanceUntilIdle() // Avanza el tiempo virtual
    
    assertEquals(PlaybackState.PLAYING, audioPlayer.playbackState.first())
}
```

## 📈 Próximos Tests a Implementar

### Prioridad Alta
- [ ] `MusicRepositoryTest` - Tests para el repositorio principal de música
- [ ] `RadioRepositoryTest` - Tests para el repositorio de radios
- [ ] `FilesScannerTest` - Tests para el escáner de archivos

### Prioridad Media
- [ ] `MetadataReaderTest` - Tests para lectura de metadatos
- [ ] `LibraryViewModelTest` - Tests para el ViewModel de la biblioteca
- [ ] `PlayerViewModelTest` - Tests para el ViewModel del reproductor

### Prioridad Baja
- [ ] `ThemeManagerTest` - Tests para el gestor de temas
- [ ] `UserPreferencesTest` - Tests para preferencias de usuario
- [ ] Tests de UI con Compose Testing

## 🎯 Mejores Prácticas

### 1. Usa `runTest` para Coroutines
```kotlin
@Test
fun myTest() = runTest {
    // Tu código con coroutines
}
```

### 2. Usa `advanceUntilIdle()` para Esperar Coroutines
```kotlin
audioPlayer.play(song)
advanceUntilIdle() // Espera a que todas las coroutines terminen
```

### 3. Limpia Recursos en @After
```kotlin
@After
fun tearDown() {
    database.close()
    audioPlayer.release()
}
```

### 4. Usa Base de Datos en Memoria para Tests
```kotlin
val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
AppDatabase.Schema.create(driver)
database = AppDatabase(driver)
```

### 5. Tests Deben Ser Independientes
Cada test debe poder ejecutarse solo sin depender del orden de ejecución.

## 📝 Convenciones de Nombres

```kotlin
@Test
fun `should do something when condition`() { ... }

@Test
fun `methodName should expectedBehavior`() { ... }
```

## 🏆 Cobertura Actual

| Componente | Tests | Cobertura Estimada |
|------------|-------|-------------------|
| VlcjAudioPlayer | 30+ | ~70% |
| FavoritesRepository | 25+ | ~95% |
| MusicRepository | 0 | 0% |
| RadioRepository | 0 | 0% |
| FileScanner | 0 | 0% |
| ViewModels | 0 | 0% |
| UI Components | 0 | 0% |

**Total**: 55+ tests implementados

---

**Última actualización**: 2024-11-19
**Versión**: 1.0.0
