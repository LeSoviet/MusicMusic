package com.musicmusic.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Gestiona el tema de la aplicación (Light/Dark)
 * Persiste la preferencia del usuario
 */
class ThemeManager {
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()
    
    /**
     * Cambia entre modo oscuro y claro
     */
    fun toggleTheme() {
        _isDarkMode.value = !_isDarkMode.value
        savePreference(_isDarkMode.value)
    }
    
    /**
     * Establece el tema explícitamente
     */
    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        savePreference(enabled)
    }
    
    /**
     * Carga la preferencia guardada
     */
    fun loadPreference() {
        // TODO: Implementar lectura desde archivo de configuración
        // Por ahora, usa el valor por defecto (false = light mode)
    }
    
    /**
     * Guarda la preferencia del usuario
     */
    private fun savePreference(isDark: Boolean) {
        // TODO: Implementar guardado en archivo de configuración
    }
}
