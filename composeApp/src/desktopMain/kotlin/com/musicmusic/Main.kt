package com.musicmusic

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.unit.dp
import com.musicmusic.data.repository.RadioRepository
import com.musicmusic.di.desktopModule
import com.musicmusic.ui.components.CustomTitleBar
import com.musicmusic.ui.components.setupDragAndDrop
import com.musicmusic.ui.screens.library.LibraryViewModel
import com.musicmusic.ui.theme.MusicMusicTheme
import com.musicmusic.ui.theme.ThemeManager
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin
import java.awt.Dimension
import java.io.File

fun main() = application {
    // Inicializar Koin
    val koinApp = startKoin {
        modules(desktopModule)
    }

    // Inicializar base de datos y cargar radios
    val radioRepository = koinApp.koin.get<RadioRepository>()
    runBlocking {
        radioRepository.loadRadios()
    }

    val windowState = rememberWindowState(width = 1280.dp, height = 720.dp)

    Window(
        onCloseRequest = ::exitApplication,
        title = "MusicMusic",
        state = windowState,
        undecorated = true  // Disable native title bar
    ) {
        window.minimumSize = Dimension(960, 600)

        val themeManager = koinApp.koin.get<ThemeManager>()
        val isDarkMode by themeManager.isDarkMode.collectAsState()
        var isDraggingOver by remember { mutableStateOf(false) }

        // Configure drag & drop at window level
        LaunchedEffect(Unit) {
            val libraryViewModel = koinApp.koin.get<LibraryViewModel>()

            setupDragAndDrop(
                component = window,
                onFilesDropped = { files ->
                    // Process dropped files and folders
                    handleDroppedFiles(files, libraryViewModel)
                },
                onDragStateChanged = { isDragging ->
                    isDraggingOver = isDragging
                }
            )
        }

        MusicMusicTheme(darkTheme = isDarkMode) {
            Column {
                // Custom title bar
                CustomTitleBar(
                    title = "MusicMusic",
                    windowState = windowState,
                    onClose = ::exitApplication
                )

                // Main app content
                App(isDraggingOver = isDraggingOver)
            }
        }
    }
}

/**
 * Handles files and folders dropped into the application.
 * Distinguishes between individual files and directories to process them appropriately.
 */
private fun handleDroppedFiles(files: List<File>, libraryViewModel: LibraryViewModel) {
    val audioFiles = mutableListOf<File>()
    val directories = mutableListOf<File>()

    // Separate files from folders
    files.forEach { file ->
        when {
            file.isDirectory -> directories.add(file)
            file.isFile -> audioFiles.add(file)
        }
    }

    // Process folders (scan recursively)
    directories.forEach { directory ->
        println("📁 Scanning dropped folder: ${directory.absolutePath}")
        libraryViewModel.scanDirectory(directory.absolutePath)
    }

    // Process individual files
    if (audioFiles.isNotEmpty()) {
        println("🎵 Adding ${audioFiles.size} dropped files")
        libraryViewModel.addFiles(audioFiles)
    }
}
