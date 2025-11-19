package com.musicmusic.ui.theme

// Expect declaration - actual implementation en desktopMain
expect class ThemeManager {
    val isDarkMode: kotlinx.coroutines.flow.StateFlow<Boolean>
    fun toggleTheme()
    fun setDarkMode(enabled: Boolean)
}
