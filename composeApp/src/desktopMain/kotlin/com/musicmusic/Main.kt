package com.musicmusic

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.unit.dp
import com.musicmusic.data.repository.RadioRepository
import com.musicmusic.di.desktopModule
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin

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
        state = rememberWindowState(width = 1200.dp, height = 800.dp)
    ) {
        App()
    }
}
