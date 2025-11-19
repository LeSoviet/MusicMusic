package com.musicmusic.ui.keyboard

import androidx.compose.ui.input.key.*

/**
 * Gestor de atajos de teclado globales para MusicMusic
 * 
 * Atajos soportados:
 * - Space: Play/Pause
 * - Left Arrow: Anterior (o -5s)
 * - Right Arrow: Siguiente (o +5s)
 * - Up Arrow: Subir volumen
 * - Down Arrow: Bajar volumen
 * - Ctrl+F: Buscar
 * - Ctrl+L: Ir a Biblioteca
 * - Ctrl+R: Ir a Radios
 * - Ctrl+Q: Ir a Cola
 * - Ctrl+,: Configuración
 * - Ctrl+T: Toggle Tema
 */
object KeyboardShortcuts {
    
    /**
     * Procesa eventos de teclado y ejecuta las acciones correspondientes
     */
    fun handleKeyEvent(
        event: KeyEvent,
        onPlayPause: () -> Unit,
        onPrevious: () -> Unit,
        onNext: () -> Unit,
        onVolumeUp: () -> Unit,
        onVolumeDown: () -> Unit,
        onSeekBackward: () -> Unit,
        onSeekForward: () -> Unit,
        onSearch: () -> Unit,
        onGoToLibrary: () -> Unit,
        onGoToRadios: () -> Unit,
        onGoToQueue: () -> Unit,
        onSettings: () -> Unit,
        onToggleTheme: () -> Unit
    ): Boolean {
        if (event.type != KeyEventType.KeyDown) return false
        
        val isCtrlPressed = event.isCtrlPressed
        val isShiftPressed = event.isShiftPressed
        
        return when {
            // Reproducción
            event.key == Key.Spacebar && !isCtrlPressed -> {
                onPlayPause()
                true
            }
            
            // Navegación entre canciones
            event.key == Key.DirectionLeft && isCtrlPressed -> {
                onPrevious()
                true
            }
            event.key == Key.DirectionRight && isCtrlPressed -> {
                onNext()
                true
            }
            
            // Seek (sin Ctrl)
            event.key == Key.DirectionLeft && !isCtrlPressed -> {
                onSeekBackward()
                true
            }
            event.key == Key.DirectionRight && !isCtrlPressed -> {
                onSeekForward()
                true
            }
            
            // Volumen
            event.key == Key.DirectionUp -> {
                onVolumeUp()
                true
            }
            event.key == Key.DirectionDown -> {
                onVolumeDown()
                true
            }
            
            // Búsqueda
            event.key == Key.F && isCtrlPressed -> {
                onSearch()
                true
            }
            
            // Navegación entre pantallas
            event.key == Key.L && isCtrlPressed -> {
                onGoToLibrary()
                true
            }
            event.key == Key.R && isCtrlPressed -> {
                onGoToRadios()
                true
            }
            event.key == Key.Q && isCtrlPressed -> {
                onGoToQueue()
                true
            }
            
            // Configuración
            event.key == Key.Comma && isCtrlPressed -> {
                onSettings()
                true
            }
            
            // Toggle Tema
            event.key == Key.T && isCtrlPressed -> {
                onToggleTheme()
                true
            }
            
            else -> false
        }
    }
    
    /**
     * Obtiene la descripción legible de un atajo
     */
    fun getShortcutDescription(): List<Pair<String, String>> = listOf(
        "Space" to "Play/Pause",
        "←/→" to "Retroceder/Adelantar 5s",
        "Ctrl + ←/→" to "Anterior/Siguiente",
        "↑/↓" to "Subir/Bajar volumen",
        "Ctrl + F" to "Buscar",
        "Ctrl + L" to "Biblioteca",
        "Ctrl + R" to "Radios",
        "Ctrl + Q" to "Cola",
        "Ctrl + ," to "Configuración",
        "Ctrl + T" to "Cambiar tema"
    )
}
