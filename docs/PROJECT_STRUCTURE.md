# 🏗️ Estructura del Proyecto MusicMusic

## Árbol de Directorios Detallado

```
MusicMusic/
│
├── 📁 composeApp/                           # Módulo principal de la aplicación
│   │
│   ├── 📁 src/
│   │   │
│   │   ├── 📁 commonMain/                   # Código compartido entre todas las plataformas
│   │   │   ├── 📁 kotlin/
│   │   │   │   ├── 📁 com.musicmusic/
│   │   │   │   │   │
│   │   │   │   │   ├── 📁 ui/               # Capa de presentación
│   │   │   │   │   │   ├── 📁 theme/        # Sistema de theming
│   │   │   │   │   │   │   ├── Color.kt     # Paleta de colores
│   │   │   │   │   │   │   ├── Typography.kt # Tipografía
│   │   │   │   │   │   │   ├── Shape.kt     # Formas y bordes
│   │   │   │   │   │   │   └── Theme.kt     # Composición del tema
│   │   │   │   │   │   │
│   │   │   │   │   │   ├── 📁 screens/      # Pantallas principales
│   │   │   │   │   │   │   ├── 📁 home/
│   │   │   │   │   │   │   │   ├── HomeScreen.kt
│   │   │   │   │   │   │   │   └── HomeViewModel.kt
│   │   │   │   │   │   │   │
│   │   │   │   │   │   │   ├── 📁 player/
│   │   │   │   │   │   │   │   ├── NowPlayingScreen.kt
│   │   │   │   │   │   │   │   ├── PlayerViewModel.kt
│   │   │   │   │   │   │   │   └── PlayerBar.kt
│   │   │   │   │   │   │   │
│   │   │   │   │   │   │   ├── 📁 radio/
│   │   │   │   │   │   │   │   ├── RadioScreen.kt
│   │   │   │   │   │   │   │   └── RadioViewModel.kt
│   │   │   │   │   │   │   │
│   │   │   │   │   │   │   ├── 📁 library/
│   │   │   │   │   │   │   │   ├── LibraryScreen.kt
│   │   │   │   │   │   │   │   ├── AlbumDetailScreen.kt
│   │   │   │   │   │   │   │   ├── ArtistDetailScreen.kt
│   │   │   │   │   │   │   │   └── LibraryViewModel.kt
│   │   │   │   │   │   │   │
│   │   │   │   │   │   │   ├── 📁 playlists/
│   │   │   │   │   │   │   │   ├── PlaylistsScreen.kt
│   │   │   │   │   │   │   │   ├── PlaylistDetailScreen.kt
│   │   │   │   │   │   │   │   └── PlaylistViewModel.kt
│   │   │   │   │   │   │   │
│   │   │   │   │   │   │   ├── 📁 search/
│   │   │   │   │   │   │   │   ├── SearchScreen.kt
│   │   │   │   │   │   │   │   └── SearchViewModel.kt
│   │   │   │   │   │   │   │
│   │   │   │   │   │   │   └── 📁 settings/
│   │   │   │   │   │   │       ├── SettingsScreen.kt
│   │   │   │   │   │   │       └── SettingsViewModel.kt
│   │   │   │   │   │   │
│   │   │   │   │   │   ├── 📁 components/   # Componentes reutilizables
│   │   │   │   │   │   │   ├── SongItem.kt
│   │   │   │   │   │   │   ├── AlbumCard.kt
│   │   │   │   │   │   │   ├── ArtistCard.kt
│   │   │   │   │   │   │   ├── RadioCard.kt
│   │   │   │   │   │   │   ├── PlaylistCard.kt
│   │   │   │   │   │   │   ├── PlayerControls.kt
│   │   │   │   │   │   │   ├── SeekBar.kt
│   │   │   │   │   │   │   ├── VolumeControl.kt
│   │   │   │   │   │   │   ├── AlbumCover.kt
│   │   │   │   │   │   │   ├── NavigationSidebar.kt
│   │   │   │   │   │   │   ├── SearchBar.kt
│   │   │   │   │   │   │   └── LoadingIndicator.kt
│   │   │   │   │   │   │
│   │   │   │   │   │   ├── 📁 navigation/   # Sistema de navegación
│   │   │   │   │   │   │   ├── NavGraph.kt
│   │   │   │   │   │   │   ├── Screen.kt    # Definición de rutas
│   │   │   │   │   │   │   └── Navigator.kt
│   │   │   │   │   │   │
│   │   │   │   │   │   └── App.kt           # Punto de entrada UI
│   │   │   │   │   │
│   │   │   │   │   ├── 📁 domain/           # Lógica de negocio
│   │   │   │   │   │   ├── 📁 model/        # Modelos de dominio
│   │   │   │   │   │   │   ├── Song.kt
│   │   │   │   │   │   │   ├── Album.kt
│   │   │   │   │   │   │   ├── Artist.kt
│   │   │   │   │   │   │   ├── Radio.kt
│   │   │   │   │   │   │   ├── Playlist.kt
│   │   │   │   │   │   │   ├── PlaybackState.kt
│   │   │   │   │   │   │   └── AudioMetadata.kt
│   │   │   │   │   │   │
│   │   │   │   │   │   ├── 📁 repository/   # Interfaces de repositorios
│   │   │   │   │   │   │   ├── SongRepository.kt
│   │   │   │   │   │   │   ├── AlbumRepository.kt
│   │   │   │   │   │   │   ├── ArtistRepository.kt
│   │   │   │   │   │   │   ├── RadioRepository.kt
│   │   │   │   │   │   │   ├── PlaylistRepository.kt
│   │   │   │   │   │   │   └── SettingsRepository.kt
│   │   │   │   │   │   │
│   │   │   │   │   │   └── 📁 usecase/      # Casos de uso
│   │   │   │   │   │       ├── GetSongsUseCase.kt
│   │   │   │   │   │       ├── PlaySongUseCase.kt
│   │   │   │   │   │       ├── ScanLibraryUseCase.kt
│   │   │   │   │   │       ├── CreatePlaylistUseCase.kt
│   │   │   │   │   │       ├── SearchUseCase.kt
│   │   │   │   │   │       └── StreamRadioUseCase.kt
│   │   │   │   │   │
│   │   │   │   │   ├── 📁 data/             # Capa de datos
│   │   │   │   │   │   ├── 📁 repository/   # Implementaciones
│   │   │   │   │   │   │   ├── SongRepositoryImpl.kt
│   │   │   │   │   │   │   ├── AlbumRepositoryImpl.kt
│   │   │   │   │   │   │   ├── ArtistRepositoryImpl.kt
│   │   │   │   │   │   │   ├── RadioRepositoryImpl.kt
│   │   │   │   │   │   │   ├── PlaylistRepositoryImpl.kt
│   │   │   │   │   │   │   └── SettingsRepositoryImpl.kt
│   │   │   │   │   │   │
│   │   │   │   │   │   ├── 📁 source/       # Data sources
│   │   │   │   │   │   │   ├── 📁 local/
│   │   │   │   │   │   │   │   ├── DatabaseDataSource.kt
│   │   │   │   │   │   │   │   ├── FileSystemDataSource.kt
│   │   │   │   │   │   │   │   └── PreferencesDataSource.kt
│   │   │   │   │   │   │   │
│   │   │   │   │   │   │   └── 📁 remote/
│   │   │   │   │   │   │       └── RadioStreamDataSource.kt
│   │   │   │   │   │   │
│   │   │   │   │   │   └── 📁 mapper/      # Mappers DTO <-> Domain
│   │   │   │   │   │       ├── SongMapper.kt
│   │   │   │   │   │       ├── AlbumMapper.kt
│   │   │   │   │   │       └── RadioMapper.kt
│   │   │   │   │   │
│   │   │   │   │   ├── 📁 di/              # Dependency Injection
│   │   │   │   │   │   ├── AppModule.kt    # Módulo general
│   │   │   │   │   │   ├── DataModule.kt   # Repositorios y sources
│   │   │   │   │   │   ├── DomainModule.kt # Use cases
│   │   │   │   │   │   └── UiModule.kt     # ViewModels
│   │   │   │   │   │
│   │   │   │   │   └── 📁 util/            # Utilidades compartidas
│   │   │   │   │       ├── Constants.kt
│   │   │   │   │       ├── Extensions.kt
│   │   │   │   │       ├── Result.kt       # Wrapper de resultados
│   │   │   │   │       └── Logger.kt
│   │   │   │   │
│   │   │   └── 📁 resources/                # Recursos compartidos
│   │   │       ├── 📁 drawable/             # Íconos y assets
│   │   │       ├── 📁 fonts/                # Fuentes personalizadas
│   │   │       └── radios.json              # Lista de 400 radios
│   │   │
│   │   ├── 📁 desktopMain/                  # Código específico desktop
│   │   │   └── 📁 kotlin/
│   │   │       ├── 📁 com.musicmusic/
│   │   │       │   │
│   │   │       │   ├── Main.kt              # Punto de entrada app
│   │   │       │   │
│   │   │       │   ├── 📁 audio/            # Implementación audio
│   │   │       │   │   ├── AudioPlayer.kt   # Wrapper VLCJ
│   │   │       │   │   ├── AudioPlayerImpl.kt
│   │   │       │   │   ├── MediaPlayerListener.kt
│   │   │       │   │   └── AudioFormat.kt
│   │   │       │   │
│   │   │       │   ├── 📁 files/            # Sistema de archivos
│   │   │       │   │   ├── FileScanner.kt   # Escaneo de carpetas
│   │   │       │   │   ├── MetadataReader.kt # Lectura de tags
│   │   │       │   │   └── CoverArtExtractor.kt
│   │   │       │   │
│   │   │       │   ├── 📁 platform/         # APIs de plataforma
│   │   │       │   │   ├── FileSystem.kt    # Acceso a archivos
│   │   │       │   │   ├── MediaKeys.kt     # Teclas multimedia
│   │   │       │   │   └── SystemTray.kt    # Bandeja del sistema
│   │   │       │   │
│   │   │       │   └── 📁 di/
│   │   │       │       └── DesktopModule.kt # DI específico desktop
│   │   │       │
│   │   │       └── 📁 resources/
│   │   │           └── app_icon.png         # Ícono de la aplicación
│   │   │
│   │   ├── 📁 windowsMain/                  # Específico Windows (si necesario)
│   │   │   └── 📁 kotlin/
│   │   │       └── 📁 com.musicmusic/
│   │   │           └── platform/
│   │   │               └── WindowsMediaKeys.kt
│   │   │
│   │   └── 📁 linuxMain/                    # Específico Linux (si necesario)
│   │       └── 📁 kotlin/
│   │           └── 📁 com.musicmusic/
│   │               └── platform/
│   │                   └── LinuxMediaKeys.kt
│   │
│   └── build.gradle.kts                     # Configuración del módulo
│
├── 📁 shared/                               # Módulo compartido (opcional)
│   ├── 📁 src/
│   │   └── 📁 commonMain/
│   │       └── 📁 kotlin/
│   │           └── 📁 com.musicmusic.shared/
│   │               ├── 📁 models/           # DTOs y modelos puros
│   │               ├── 📁 utils/            # Utilidades puras
│   │               └── 📁 constants/        # Constantes globales
│   │
│   └── build.gradle.kts
│
├── 📁 sqldelight/                           # Esquemas de base de datos
│   └── 📁 databases/
│       ├── Songs.sq                         # Tabla de canciones
│       ├── Albums.sq                        # Tabla de álbumes
│       ├── Artists.sq                       # Tabla de artistas
│       ├── Radios.sq                        # Tabla de radios
│       ├── Playlists.sq                     # Tabla de playlists
│       ├── PlaylistSongs.sq                 # Relación playlist-songs
│       └── Settings.sq                      # Configuración
│
├── 📁 gradle/                               # Wrapper de Gradle
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
│
├── 📁 buildSrc/                             # Scripts de build personalizados
│   └── src/
│       └── main/
│           └── kotlin/
│               ├── Dependencies.kt          # Versiones centralizadas
│               └── BuildConfig.kt
│
├── 📁 .github/                              # GitHub workflows (futuro)
│   └── workflows/
│       ├── build.yml
│       └── release.yml
│
├── 📁 docs/                                 # Documentación adicional
│   ├── ARCHITECTURE.md
│   ├── CONTRIBUTING.md
│   └── USER_GUIDE.md
│
├── .gitignore
├── build.gradle.kts                         # Build principal
├── settings.gradle.kts                      # Configuración del proyecto
├── gradle.properties                        # Propiedades de Gradle
├── gradlew                                  # Gradle wrapper (Unix)
├── gradlew.bat                              # Gradle wrapper (Windows)
├── README.md                                # Documentación principal
├── ROADMAP.md                               # Este roadmap
├── PROJECT_STRUCTURE.md                     # Este archivo
└── LICENSE                                  # Licencia del proyecto
```

---

## 📦 Descripción de Módulos

### `composeApp/`
**Módulo principal de la aplicación.**  
Contiene toda la UI, lógica de negocio y código específico de plataforma.

#### `commonMain/`
Código que se comparte entre todas las plataformas. Aquí está el 90% de la aplicación:
- **UI**: Compose Desktop UI
- **Domain**: Lógica de negocio pura
- **Data**: Repositorios e implementaciones
- **DI**: Configuración de Koin

#### `desktopMain/`
Código específico para plataformas desktop (Windows/Linux):
- **Audio**: Integración con VLCJ
- **Files**: Lectura de sistema de archivos y metadatos
- **Platform**: APIs nativas (media keys, system tray)

#### `windowsMain/` y `linuxMain/`
Implementaciones específicas de plataforma cuando sea necesario (ej: media keys del SO).

---

### `shared/` (Opcional)
Módulo de utilidades puras sin dependencias de UI. Útil si en el futuro quieres:
- Crear una versión móvil
- Compartir lógica con otros proyectos
- Mantener el core independiente

---

### `sqldelight/`
Esquemas SQL de la base de datos local. SQLDelight genera código Kotlin type-safe a partir de estos archivos.

**Tablas principales**:
- `Songs`: Canciones individuales
- `Albums`: Álbumes
- `Artists`: Artistas
- `Radios`: Estaciones de radio
- `Playlists`: Playlists del usuario
- `PlaylistSongs`: Relación muchos-a-muchos
- `Settings`: Preferencias del usuario

---

## 🎯 Arquitectura en Capas

```
┌─────────────────────────────────────────┐
│            UI Layer (Compose)           │
│  Screens │ Components │ ViewModels      │
└─────────────┬───────────────────────────┘
              │
┌─────────────▼───────────────────────────┐
│         Domain Layer (Pure)             │
│  Models │ UseCases │ Repository Interfaces │
└─────────────┬───────────────────────────┘
              │
┌─────────────▼───────────────────────────┐
│          Data Layer                     │
│  Repository Impl │ DataSources │ Mappers │
└─────────────┬───────────────────────────┘
              │
    ┌─────────┴─────────┐
    │                   │
┌───▼────┐      ┌──────▼──────┐
│Database│      │File System  │
│(SQLite)│      │Audio Player │
└────────┘      └─────────────┘
```

### Flujo de Datos
1. **UI** dispara eventos de usuario
2. **ViewModel** llama a **UseCases**
3. **UseCases** orquestan lógica usando **Repositories**
4. **Repositories** acceden a **DataSources**
5. **DataSources** interactúan con DB/FileSystem/Audio
6. Datos fluyen de vuelta con **StateFlow/SharedFlow**

---

## 🔑 Componentes Clave

### AudioPlayer (desktopMain)
```kotlin
interface AudioPlayer {
    val playbackState: StateFlow<PlaybackState>
    val currentPosition: StateFlow<Long>
    val volume: StateFlow<Float>
    
    suspend fun play(uri: String)
    suspend fun pause()
    suspend fun resume()
    suspend fun stop()
    suspend fun seekTo(position: Long)
    suspend fun setVolume(volume: Float)
}
```

### SongRepository (domain)
```kotlin
interface SongRepository {
    fun getAllSongs(): Flow<List<Song>>
    fun getSongById(id: String): Flow<Song?>
    suspend fun insertSongs(songs: List<Song>)
    fun searchSongs(query: String): Flow<List<Song>>
    suspend fun deleteSong(id: String)
}
```

### FileScanner (desktopMain)
```kotlin
interface FileScanner {
    fun scanDirectory(path: String): Flow<ScanProgress>
    suspend fun extractMetadata(file: File): AudioMetadata
    suspend fun extractCoverArt(file: File): ByteArray?
}
```

---

## 🎨 Convenciones de Código

### Naming
- **Screens**: `*Screen.kt` (ej: `HomeScreen.kt`)
- **ViewModels**: `*ViewModel.kt` (ej: `HomeViewModel.kt`)
- **Repositories**: `*Repository.kt` / `*RepositoryImpl.kt`
- **UseCases**: `*UseCase.kt` (ej: `GetSongsUseCase.kt`)
- **Components**: Sustantivos descriptivos (ej: `AlbumCard.kt`)

### Paquetes
- Use nombres en minúsculas sin guiones bajos
- Agrupe por feature, no por tipo de clase
- Ejemplo: `com.musicmusic.ui.screens.home` ✅
- No: `com.musicmusic.ui.viewmodels` ❌

### Comentarios
```kotlin
/**
 * Escanea un directorio en busca de archivos de audio.
 *
 * @param path Ruta absoluta al directorio
 * @return Flow que emite el progreso del escaneo
 */
fun scanDirectory(path: String): Flow<ScanProgress>
```

---

## 🧪 Testing

### Estructura de Tests
```
composeApp/
├── src/
│   ├── commonTest/          # Tests compartidos
│   │   └── kotlin/
│   │       ├── domain/      # Tests de UseCases
│   │       ├── data/        # Tests de Repositories
│   │       └── ui/          # Tests de ViewModels
│   │
│   └── desktopTest/         # Tests específicos desktop
│       └── kotlin/
│           ├── audio/       # Tests de AudioPlayer
│           └── files/       # Tests de FileScanner
```

### Herramientas
- **kotlin.test**: Framework de testing
- **Turbine**: Testing de Flows
- **MockK**: Mocking
- **Compose UI Test**: Testing de UI

---

## 📝 Notas Adicionales

### Base de Datos
SQLDelight generará:
- `Database.sq` → `Database.kt`
- Queries type-safe
- Soporte para coroutines

### Recursos
- Carátulas: Cache en `~/.musicmusic/cache/covers/`
- Base de datos: `~/.musicmusic/musicmusic.db`
- Config: `~/.musicmusic/config.json`
- Logs: `~/.musicmusic/logs/`

### Performance
- Use `LazyColumn` para listas
- `remember` para evitar recomposiciones
- Background coroutines para I/O
- Virtual scrolling para 10k+ items

---

**Última actualización**: Noviembre 2025  
**Versión**: 1.0
