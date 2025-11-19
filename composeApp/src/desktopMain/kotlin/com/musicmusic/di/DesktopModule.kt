package com.musicmusic.di

import com.musicmusic.audio.VlcjAudioPlayer
import com.musicmusic.data.database.DatabaseDriverFactory
import com.musicmusic.data.preferences.UserPreferences
import com.musicmusic.data.preferences.createUserPreferencesDataStore
import com.musicmusic.data.repository.MusicRepository
import com.musicmusic.data.repository.RadioRepository
import com.musicmusic.database.AppDatabase
import com.musicmusic.domain.audio.AudioPlayer
import com.musicmusic.files.FileScanner
import com.musicmusic.files.MetadataReader
import com.musicmusic.ui.screens.library.LibraryViewModel
import com.musicmusic.ui.screens.player.PlayerViewModel
import com.musicmusic.ui.theme.ThemeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Módulo Koin para Desktop (audio, archivos, etc.)
 */
val desktopModule = module {
    
    // Scope para operaciones de background (I/O, database, preferences)
    single<CoroutineScope> {
        CoroutineScope(Dispatchers.IO + SupervisorJob())
    }
    
    // User Preferences
    single {
        createUserPreferencesDataStore()
    }
    
    single {
        UserPreferences(dataStore = get())
    }
    
    // Theme Manager
    single {
        ThemeManager(
            userPreferences = get(),
            scope = get()
        )
    }
    
    // Base de datos
    single<AppDatabase> {
        DatabaseDriverFactory.createDatabase()
    }
    
    // Audio Player
    single<AudioPlayer> {
        VlcjAudioPlayer(scope = get())
    }
    
    // File Scanner y Metadata Reader
    singleOf(::MetadataReader)
    single {
        FileScanner(metadataReader = get())
    }
    
    // Repositories
    single {
        com.musicmusic.data.repository.FavoritesRepository(database = get())
    }

    single {
        MusicRepository(
            fileScanner = get(),
            metadataReader = get(),
            favoritesRepository = get()
        )
    }

    single {
        RadioRepository(database = get())
    }
    
    // ViewModels
    single {
        PlayerViewModel(
            audioPlayer = get(),
            userPreferences = get(),
            viewModelScope = get(),
            musicRepository = get()
        )
    }
    
    single<LibraryViewModel> {
        LibraryViewModel(
            musicRepository = get(),
            playerViewModel = get(),
            viewModelScope = get()
        )
    }
    
    single {
        com.musicmusic.ui.screens.radio.RadioViewModel(
            radioRepository = get(),
            playerViewModel = get(),
            viewModelScope = get()
        )
    }
}
