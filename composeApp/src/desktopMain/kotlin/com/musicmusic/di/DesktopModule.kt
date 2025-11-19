package com.musicmusic.di

import com.musicmusic.audio.VlcjAudioPlayer
import com.musicmusic.data.database.DatabaseDriverFactory
import com.musicmusic.data.preferences.UserPreferences
import com.musicmusic.data.preferences.createUserPreferencesDataStore
import com.musicmusic.data.repository.MusicRepository
import com.musicmusic.data.repository.RadioRepository
import com.musicmusic.database.AppDatabase
import com.musicmusic.domain.audio.AudioPlayer
import com.musicmusic.domain.error.ErrorHandler
import com.musicmusic.files.FileScanner
import com.musicmusic.files.MetadataReader
import com.musicmusic.ui.screens.library.LibraryViewModel
import com.musicmusic.ui.screens.player.PlayerViewModel
import com.musicmusic.ui.theme.ThemeManager
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Módulo Koin para Desktop (audio, archivos, etc.)
 */
val desktopModule = module {
    
    // User Preferences
    single {
        createUserPreferencesDataStore()
    }
    
    single {
        UserPreferences(dataStore = get())
    }
    
    // Theme Manager
    single {
        ThemeManager(userPreferences = get())
    }
    
    // Error Handler
    single {
        ErrorHandler()
    }
    
    // Base de datos
    single<AppDatabase> {
        DatabaseDriverFactory.createDatabase()
    }
    
    // Audio Player
    single<AudioPlayer> {
        VlcjAudioPlayer(errorHandler = get())
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
            favoritesRepository = get(),
            errorHandler = get()
        )
    }

    single {
        RadioRepository(
            database = get(),
            errorHandler = get()
        )
    }
    
    // ViewModels (cada uno con su propio CoroutineScope)
    single {
        PlayerViewModel(
            audioPlayer = get(),
            userPreferences = get(),
            musicRepository = get()
        )
    }
    
    single<LibraryViewModel> {
        LibraryViewModel(
            musicRepository = get(),
            playerViewModel = get()
        )
    }
    
    single {
        com.musicmusic.ui.screens.radio.RadioViewModel(
            radioRepository = get(),
            playerViewModel = get()
        )
    }
}
