package com.musicmusic

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun App() {
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
        var currentScreen by remember { mutableStateOf(Screen.LIBRARY) }
        
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
                        onGoToLibrary = { currentScreen = Screen.LIBRARY },
                        onGoToRadios = { currentScreen = Screen.RADIOS },
                        onGoToQueue = { currentScreen = Screen.QUEUE },
                        onSettings = { currentScreen = Screen.SETTINGS },
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
                    
                    NavigationRailItem(
                        icon = { Icon(Icons.Default.LibraryMusic, contentDescription = null) },
                        label = { Text("Library") },
                        selected = currentScreen == Screen.LIBRARY,
                        onClick = { currentScreen = Screen.LIBRARY }
                    )
                    
                    NavigationRailItem(
                        icon = { Icon(Icons.Default.Radio, contentDescription = null) },
                        label = { Text("Radios") },
                        selected = currentScreen == Screen.RADIOS,
                        onClick = { currentScreen = Screen.RADIOS }
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    NavigationRailItem(
                        icon = { Icon(Icons.Default.QueueMusic, contentDescription = null) },
                        label = { Text("Queue") },
                        selected = currentScreen == Screen.QUEUE,
                        onClick = { currentScreen = Screen.QUEUE }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Toggle de tema
                    IconButton(
                        onClick = { themeManager.toggleTheme() },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Configuración
                    IconButton(
                        onClick = { currentScreen = Screen.SETTINGS },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configuración",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Contenido principal
                Box(modifier = Modifier.fillMaxSize()) {
                    // Pantalla principal con animaciones
                    AppAnimations.AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = AppAnimations.slideHorizontalTransition()
                    ) { screen ->
                        when (screen) {
                            Screen.LIBRARY -> {
                                val libraryViewModel = koinInject<com.musicmusic.ui.screens.library.LibraryViewModel>()
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
                            Screen.NOW_PLAYING -> {
                                NowPlayingScreen(
                                    onBack = { currentScreen = Screen.LIBRARY },
                                    onShowQueue = { currentScreen = Screen.QUEUE }
                                )
                            }
                            Screen.QUEUE -> {
                                QueueScreen()
                            }
                            Screen.SETTINGS -> {
                                val libraryViewModel = koinInject<com.musicmusic.ui.screens.library.LibraryViewModel>()
                                val isScanning by libraryViewModel.isScanning.collectAsState()

                                SettingsScreen(
                                    onBack = { currentScreen = Screen.LIBRARY },
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
                    
                    // Player Bar persistente (siempre en bottom, excepto en NOW_PLAYING)
                    if (currentScreen != Screen.NOW_PLAYING) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(androidx.compose.ui.Alignment.BottomCenter)
                        ) {
                            Column {
                                // Snackbar de errores sobre el PlayerBar
                                ErrorSnackbar(
                                    error = displayedError,
                                    onDismiss = { displayedError = null }
                                )
                                
                                PlayerBar(
                                    onClick = { currentScreen = Screen.NOW_PLAYING }
                                )
                            }
                        }
                    } else {
                        // En NOW_PLAYING, mostrar errores en la parte inferior
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(androidx.compose.ui.Alignment.BottomCenter)
                        ) {
                            ErrorSnackbar(
                                error = displayedError,
                                onDismiss = { displayedError = null }
                            )
                        }
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
    LIBRARY,
    RADIOS,
    NOW_PLAYING,
    QUEUE,
    SETTINGS
}
