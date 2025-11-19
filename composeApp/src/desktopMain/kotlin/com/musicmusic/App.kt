package com.musicmusic

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.unit.dp
import com.musicmusic.ui.animation.AppAnimations
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
import org.koin.compose.koinInject
import java.io.File
import javax.swing.JFileChooser

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
    val isDarkMode by themeManager.isDarkMode.collectAsState()
    
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
                                        chooser.dialogTitle = "Seleccionar Carpeta de Música"
                                        
                                        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                                            val selectedPath = chooser.selectedFile.absolutePath
                                            // Iniciar el escaneo
                                            libraryViewModel.scanDirectory(selectedPath)
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
                                    onBack = { currentScreen = Screen.LIBRARY }
                                )
                            }
                            Screen.QUEUE -> {
                                QueueScreen(
                                    onDismiss = { currentScreen = Screen.LIBRARY }
                                )
                            }
                            Screen.SETTINGS -> {
                                SettingsScreen(
                                    onBack = { currentScreen = Screen.LIBRARY }
                                )
                            }
                        }
                    }
                    
                    // Player Bar persistente (siempre en bottom)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(androidx.compose.ui.Alignment.BottomCenter)
                    ) {
                        PlayerBar(
                            onClick = { currentScreen = Screen.NOW_PLAYING }
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
    LIBRARY,
    RADIOS,
    NOW_PLAYING,
    QUEUE,
    SETTINGS
}
