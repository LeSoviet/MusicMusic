package com.musicmusic.ui.theme

import com.musicmusic.data.preferences.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Gestiona el tema de la aplicación (Light/Dark) con persistencia.
 * Versión Desktop que usa UserPreferences.
 * 
 * Tiene su propio CoroutineScope para operaciones asíncronas.
 */
actual class ThemeManager(
    private val userPreferences: UserPreferences
) {
    // ThemeManager-specific coroutine scope
    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    private val _isDarkMode = MutableStateFlow(true)
    actual val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()
    
    init {
        // Cargar preferencia inicial
        scope.launch {
            userPreferences.darkModeEnabled.collect { darkMode ->
                _isDarkMode.value = darkMode
            }
        }
    }
    
    /**
     * Cambia entre modo oscuro y claro
     */
    actual fun toggleTheme() {
        scope.launch {
            val newValue = !_isDarkMode.value
            _isDarkMode.value = newValue
            userPreferences.setDarkMode(newValue)
        }
    }
    
    /**
     * Establece el tema explícitamente
     */
    actual fun setDarkMode(enabled: Boolean) {
        scope.launch {
            _isDarkMode.value = enabled
            userPreferences.setDarkMode(enabled)
        }
    }
}
