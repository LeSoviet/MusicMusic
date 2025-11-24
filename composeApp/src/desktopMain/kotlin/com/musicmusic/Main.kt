package com.musicmusic

import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.unit.dp
import com.musicmusic.data.repository.RadioRepository
import com.musicmusic.di.desktopModule
import com.musicmusic.ui.components.setupDragAndDrop
import com.musicmusic.ui.screens.library.LibraryViewModel
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

    Window(
        onCloseRequest = ::exitApplication,
        title = "MusicMusic",
        state = rememberWindowState(width = 1366.dp, height = 768.dp)
    ) {
        window.minimumSize = Dimension(1366, 768)

        var isDraggingOver by remember { mutableStateOf(false) }

        // Configurar drag & drop a nivel de ventana
        LaunchedEffect(Unit) {
            val libraryViewModel = koinApp.koin.get<LibraryViewModel>()

            setupDragAndDrop(
                component = window,
                onFilesDropped = { files ->
                    // Procesar archivos y carpetas arrastradas
                    handleDroppedFiles(files, libraryViewModel)
                },
                onDragStateChanged = { isDragging ->
                    isDraggingOver = isDragging
                }
            )
        }

        App(isDraggingOver = isDraggingOver)
    }
}

/**
 * Maneja archivos y carpetas arrastradas a la aplicación.
 * Distingue entre archivos individuales y directorios para procesarlos adecuadamente.
 */
private fun handleDroppedFiles(files: List<File>, libraryViewModel: LibraryViewModel) {
    val audioFiles = mutableListOf<File>()
    val directories = mutableListOf<File>()

    // Separar archivos de carpetas
    files.forEach { file ->
        when {
            file.isDirectory -> directories.add(file)
            file.isFile -> audioFiles.add(file)
        }
    }

    // Procesar carpetas (escanear recursivamente)
    directories.forEach { directory ->
        println("📁 Scanning dropped folder: ${directory.absolutePath}")
        libraryViewModel.scanDirectory(directory.absolutePath)
    }

    // Procesar archivos individuales
    if (audioFiles.isNotEmpty()) {
        println("🎵 Adding ${audioFiles.size} dropped files")
        libraryViewModel.addFiles(audioFiles)
    }
}
