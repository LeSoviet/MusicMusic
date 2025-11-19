package com.musicmusic.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

/**
 * Gestión de preferencias del usuario usando DataStore.
 * 
 * Almacena:
 * - Tema oscuro activado
 * - Última canción reproducida
 * - Volumen
 * - Estados del reproductor (shuffle, repeat)
 * - Ruta de carpeta de música
 */
class UserPreferences(private val dataStore: DataStore<Preferences>) {
    
    companion object {
        // Keys para preferencias
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private val LAST_SONG_ID_KEY = stringPreferencesKey("last_song_id")
        private val LAST_SONG_POSITION_KEY = longPreferencesKey("last_song_position")
        private val VOLUME_KEY = floatPreferencesKey("volume")
        private val SHUFFLE_ENABLED_KEY = booleanPreferencesKey("shuffle_enabled")
        private val REPEAT_MODE_KEY = intPreferencesKey("repeat_mode")
        private val MUSIC_FOLDER_PATH_KEY = stringPreferencesKey("music_folder_path")
        
        // Valores por defecto
        private const val DEFAULT_VOLUME = 0.7f
        private const val DEFAULT_DARK_MODE = true
        private const val DEFAULT_SHUFFLE = false
        private const val DEFAULT_REPEAT_MODE = 0 // RepeatMode.OFF
    }
    
    // Flows para observar cambios
    val darkModeEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY] ?: DEFAULT_DARK_MODE
    }
    
    val volume: Flow<Float> = dataStore.data.map { preferences ->
        preferences[VOLUME_KEY] ?: DEFAULT_VOLUME
    }
    
    val shuffleEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SHUFFLE_ENABLED_KEY] ?: DEFAULT_SHUFFLE
    }
    
    val repeatMode: Flow<Int> = dataStore.data.map { preferences ->
        preferences[REPEAT_MODE_KEY] ?: DEFAULT_REPEAT_MODE
    }
    
    val musicFolderPath: Flow<String?> = dataStore.data.map { preferences ->
        preferences[MUSIC_FOLDER_PATH_KEY]
    }
    
    val lastSongId: Flow<String?> = dataStore.data.map { preferences ->
        preferences[LAST_SONG_ID_KEY]
    }
    
    val lastSongPosition: Flow<Long> = dataStore.data.map { preferences ->
        preferences[LAST_SONG_POSITION_KEY] ?: 0L
    }
    
    // Métodos para actualizar preferencias
    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }
    
    suspend fun setVolume(volume: Float) {
        dataStore.edit { preferences ->
            preferences[VOLUME_KEY] = volume.coerceIn(0f, 1f)
        }
    }
    
    suspend fun setShuffleEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHUFFLE_ENABLED_KEY] = enabled
        }
    }
    
    suspend fun setRepeatMode(mode: Int) {
        dataStore.edit { preferences ->
            preferences[REPEAT_MODE_KEY] = mode
        }
    }
    
    suspend fun setMusicFolderPath(path: String?) {
        dataStore.edit { preferences ->
            if (path != null) {
                preferences[MUSIC_FOLDER_PATH_KEY] = path
            } else {
                preferences.remove(MUSIC_FOLDER_PATH_KEY)
            }
        }
    }
    
    suspend fun setLastPlayedSong(songId: String?, position: Long) {
        dataStore.edit { preferences ->
            if (songId != null) {
                preferences[LAST_SONG_ID_KEY] = songId
                preferences[LAST_SONG_POSITION_KEY] = position
            } else {
                preferences.remove(LAST_SONG_ID_KEY)
                preferences.remove(LAST_SONG_POSITION_KEY)
            }
        }
    }
    
    suspend fun clearLastPlayedSong() {
        dataStore.edit { preferences ->
            preferences.remove(LAST_SONG_ID_KEY)
            preferences.remove(LAST_SONG_POSITION_KEY)
        }
    }
    
    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

/**
 * Crea el DataStore para preferencias de usuario.
 * Se almacena en el directorio de configuración de la aplicación.
 */
fun createUserPreferencesDataStore(): DataStore<Preferences> {
    val userHome = System.getProperty("user.home")
    val appDir = File(userHome, ".musicmusic")
    appDir.mkdirs()
    
    return PreferenceDataStoreFactory.create(
        produceFile = { File(appDir, "user_preferences.preferences_pb") }
    )
}
