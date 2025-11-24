package com.musicmusic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.unit.dp
import com.musicmusic.domain.error.ErrorHandler
import com.musicmusic.ui.animation.AppAnimations
import com.musicmusic.ui.components.ErrorSnackbar
import com.musicmusic.ui.components.PlayerBar
import com.musicmusic.ui.keyboard.KeyboardShortcuts
import com.musicmusic.ui.screens.library.LibraryScreen
import com.musicmusic.ui.screens.player.NowPlayingScreen
import com.musicmusic.ui.screens.player.PlayerViewModel
import com.musicmusic.ui.screens.queue.QueueScreen
import com.musicmusic.ui.screens.radio.RadioScreen
import com.musicmusic.ui.screens.settings.SettingsScreen
import com.musicmusic.ui.theme.MusicMusicTheme
import com.musicmusic.ui.theme.ThemeManager
import com.musicmusic.data.preferences.UserPreferences
import org.koin.compose.koinInject
import java.io.File
import javax.swing.JFileChooser
import kotlinx.coroutines.launch

/**
 * Aplicación principal de MusicMusic.
 *
 * Gestiona:
 * - Navegación entre pantallas
 * - Player bar persistente
 * - Tema de la aplicación
 * - Atajos de teclado globales
 */
@Composable
fun App(isDraggingOver: Boolean = false) {
    val themeManager = koinInject<ThemeManager>()
    val playerViewModel = koinInject<PlayerViewModel>()
    val userPreferences = koinInject<UserPreferences>()
    val errorHandler = koinInject<ErrorHandler>()
    val isDarkMode by themeManager.isDarkMode.collectAsState()
    val currentMusicFolder by userPreferences.musicFolderPath.collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    
    // Observar errores del ErrorHandler
    val latestError = errorHandler.errors.collectAsState(initial = null).value
    var displayedError by remember { mutableStateOf(latestError) }
    
    // Actualizar el error mostrado cuando cambie
    LaunchedEffect(latestError) {
        if (latestError != null) {
            displayedError = latestError
        }
    }

    MusicMusicTheme(darkTheme = isDarkMode) {
        var currentScreen by remember { mutableStateOf(Screen.LIBRARY_SONGS) }
        var isLibraryExpanded by remember { mutableStateOf(true) }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .onPreviewKeyEvent { event ->
                    KeyboardShortcuts.handleKeyEvent(
                        event = event,
                        onPlayPause = { playerViewModel.togglePlayPause() },
                        onPrevious = { playerViewModel.previous() },
                        onNext = { playerViewModel.next() },
                        onVolumeUp = { playerViewModel.setVolume((playerViewModel.volume.value + 0.1f).coerceAtMost(1f)) },
                        onVolumeDown = { playerViewModel.setVolume((playerViewModel.volume.value - 0.1f).coerceAtLeast(0f)) },
                        onSeekBackward = { playerViewModel.seekBy(-5000) },
                        onSeekForward = { playerViewModel.seekBy(5000) },
                        onSearch = { /* TODO: Implementar búsqueda */ },
                        onGoToLibrary = {
                            isLibraryExpanded = !isLibraryExpanded
                            if (!isLibraryExpanded) {
                                currentScreen = Screen.LIBRARY_SONGS
                            }
                        },
                        onGoToRadios = {
                            isLibraryExpanded = false
                            currentScreen = Screen.RADIOS
                        },
                        onGoToQueue = {
                            isLibraryExpanded = false
                            currentScreen = Screen.QUEUE
                        },
                        onSettings = {
                            isLibraryExpanded = false
                            currentScreen = Screen.SETTINGS
                        },
                        onToggleTheme = { themeManager.toggleTheme() }
                    )
                },
            color = MaterialTheme.colorScheme.background
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Sidebar de navegación
                NavigationRail(
                    modifier = Modifier.fillMaxHeight(),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Library expandible
                    NavigationRailItem(
                        icon = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.LibraryMusic,
                                    contentDescription = ""
                                )
                                Icon(
                                    if (isLibraryExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                                    contentDescription = "",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        },
                        label = { Text("Library") },
                        selected = currentScreen in listOf(Screen.LIBRARY_SONGS, Screen.LIBRARY_ALBUMS, Screen.LIBRARY_ARTISTS),
                        onClick = {
                            isLibraryExpanded = !isLibraryExpanded
                            if (!isLibraryExpanded) {
                                currentScreen = Screen.LIBRARY_SONGS
                            }
                        },
                        alwaysShowLabel = true
                    )

                    // Sub-items de Library (solo visibles cuando está expandido)
                    if (isLibraryExpanded) {
                        NavigationRailItem(
                            icon = { Icon(Icons.Default.MusicNote, contentDescription = "") },
                            label = { Text("  Songs") },
                            selected = currentScreen == Screen.LIBRARY_SONGS,
                            onClick = { currentScreen = Screen.LIBRARY_SONGS },
                            alwaysShowLabel = true
                        )

                        NavigationRailItem(
                            icon = { Icon(Icons.Default.Album, contentDescription = "") },
                            label = { Text("  Albums") },
                            selected = currentScreen == Screen.LIBRARY_ALBUMS,
                            onClick = { currentScreen = Screen.LIBRARY_ALBUMS },
                            alwaysShowLabel = true
                        )

                        NavigationRailItem(
                            icon = { Icon(Icons.Default.Person, contentDescription = "") },
                            label = { Text("  Artists") },
                            selected = currentScreen == Screen.LIBRARY_ARTISTS,
                            onClick = { currentScreen = Screen.LIBRARY_ARTISTS },
                            alwaysShowLabel = true
                        )
                    }

                    NavigationRailItem(
                        icon = { Icon(Icons.Default.Radio, contentDescription = "") },
                        label = { Text("Radios") },
                        selected = currentScreen == Screen.RADIOS,
                        onClick = {
                            isLibraryExpanded = false
                            currentScreen = Screen.RADIOS
                        },
                        alwaysShowLabel = true
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    NavigationRailItem(
                        icon = { Icon(Icons.Default.QueueMusic, contentDescription = "") },
                        label = { Text("Queue") },
                        selected = currentScreen == Screen.QUEUE,
                        onClick = {
                            isLibraryExpanded = false
                            currentScreen = Screen.QUEUE
                        },
                        alwaysShowLabel = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Toggle de tema
                    IconButton(
                        onClick = { themeManager.toggleTheme() },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Configuración
                    IconButton(
                        onClick = {
                            isLibraryExpanded = false
                            currentScreen = Screen.SETTINGS
                        },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Contenido principal
                Column(modifier = Modifier.fillMaxSize()) {
                    // Pantalla principal con animaciones
                    Box(modifier = Modifier.weight(1f)) {
                        AppAnimations.AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = AppAnimations.slideHorizontalTransition()
                        ) { screen ->
                            when (screen) {
                                Screen.LIBRARY_SONGS,
                                Screen.LIBRARY_ALBUMS,
                                Screen.LIBRARY_ARTISTS -> {
                                    val libraryViewModel = koinInject<com.musicmusic.ui.screens.library.LibraryViewModel>()

                                    // Sincronizar el tab seleccionado con la pantalla actual
                                    LaunchedEffect(screen) {
                                        val tab = when (screen) {
                                            Screen.LIBRARY_SONGS -> com.musicmusic.ui.screens.library.LibraryTab.SONGS
                                            Screen.LIBRARY_ALBUMS -> com.musicmusic.ui.screens.library.LibraryTab.ALBUMS
                                            Screen.LIBRARY_ARTISTS -> com.musicmusic.ui.screens.library.LibraryTab.ARTISTS
                                            else -> com.musicmusic.ui.screens.library.LibraryTab.SONGS
                                        }
                                        libraryViewModel.selectTab(tab)
                                    }

                                    LibraryScreen(
                                        onScanDirectory = {
                                            // Abrir selector de carpeta
                                            val chooser = JFileChooser()
                                            chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                                            chooser.dialogTitle = "Select Music Folder"

                                            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                                                val selectedPath = chooser.selectedFile.absolutePath
                                                libraryViewModel.scanDirectory(selectedPath)
                                            }
                                        },
                                        onAddFiles = {
                                            // Abrir selector de archivos múltiples
                                            val chooser = JFileChooser()
                                            chooser.isMultiSelectionEnabled = true
                                            chooser.fileSelectionMode = JFileChooser.FILES_ONLY
                                            chooser.dialogTitle = "Add Music Files"

                                            // Filtro de extensiones de audio
                                            chooser.fileFilter = object : javax.swing.filechooser.FileFilter() {
                                                override fun accept(f: File): Boolean {
                                                    if (f.isDirectory) return true
                                                    val extension = f.extension.lowercase()
                                                    return extension in listOf("mp3", "flac", "wav", "ogg", "m4a", "aac", "wma")
                                                }

                                                override fun getDescription() = "Audio Files (*.mp3, *.flac, *.wav, *.ogg, *.m4a, *.aac, *.wma)"
                                            }

                                            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                                                val selectedFiles = chooser.selectedFiles.toList()
                                                libraryViewModel.addFiles(selectedFiles)
                                            }
                                        },
                                        libraryViewModel = libraryViewModel
                                    )
                                }
                                Screen.RADIOS -> {
                                    RadioScreen(
                                        viewModel = koinInject()
                                    )
                                }
                                Screen.QUEUE -> {
                                    QueueScreen()
                                }
                                Screen.SETTINGS -> {
                                    val libraryViewModel = koinInject<com.musicmusic.ui.screens.library.LibraryViewModel>()
                                    val isScanning by libraryViewModel.isScanning.collectAsState()

                                    SettingsScreen(
                                        onBack = {
                                            isLibraryExpanded = true
                                            currentScreen = Screen.LIBRARY_SONGS
                                        },
                                        onChangeMusicFolder = {
                                            val chooser = JFileChooser()
                                            chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                                            chooser.dialogTitle = "Select Music Folder"

                                            // Si ya hay una carpeta seleccionada, partir desde ahí
                                            currentMusicFolder?.let { folder ->
                                                chooser.currentDirectory = File(folder)
                                            }

                                            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                                                val selectedPath = chooser.selectedFile.absolutePath
                                                scope.launch {
                                                    userPreferences.setMusicFolderPath(selectedPath)
                                                    // Automáticamente escanear la nueva carpeta
                                                    libraryViewModel.scanDirectory(selectedPath)
                                                }
                                            }
                                        },
                                        onUpdateLibrary = {
                                            currentMusicFolder?.let { folder ->
                                                libraryViewModel.scanDirectory(folder)
                                            }
                                        },
                                        currentMusicFolder = currentMusicFolder,
                                        isScanning = isScanning
                                    )
                                }
                            }
                        }
                    }

                    // Player Bar persistente (siempre en bottom)
                    Column {
                        // Snackbar de errores sobre el PlayerBar
                        ErrorSnackbar(
                            error = displayedError,
                            onDismiss = { displayedError = null }
                        )

                        PlayerBar()
                    }
                }
            }

            // Overlay visual para drag & drop
            if (isDraggingOver) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        .border(
                            width = 4.dp,
                            color = MaterialTheme.colorScheme.primary
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            modifier = Modifier.size(96.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Drop files or folders here",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Music files and folders will be added to your library",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Pantallas de navegación
 */
enum class Screen {
    LIBRARY_SONGS,
    LIBRARY_ALBUMS,
    LIBRARY_ARTISTS,
    RADIOS,
    QUEUE,
    SETTINGS
}
