package com.musicmusic.data.repository

import com.musicmusic.database.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Repositorio para gestionar canciones favoritas.
 *
 * Almacena IDs de canciones marcadas como favoritas en SQLDelight.
 */
class FavoritesRepository(
    private val database: AppDatabase
) {
    private val queries = database.favoriteQueries

    // Cache en memoria de favoritos para acceso rápido
    private val _favoritesCache = MutableStateFlow<Set<String>>(emptySet())

    init {
        loadFavorites()
    }

    /**
     * Carga todos los favoritos desde la base de datos al cache
     */
    private fun loadFavorites() {
        try {
            val favorites = queries.selectAll().executeAsList()
            _favoritesCache.value = favorites.map { it.songId }.toSet()
        } catch (e: Exception) {
            println("Error loading favorites: ${e.message}")
        }
    }

    /**
     * Verifica si una canción es favorita
     */
    fun isFavorite(songId: String): Boolean {
        return _favoritesCache.value.contains(songId)
    }

    /**
     * Flow que emite true/false cuando cambia el estado de favorito
     */
    fun isFavoriteFlow(songId: String): Flow<Boolean> {
        return _favoritesCache.map { it.contains(songId) }
    }

    /**
     * Agrega una canción a favoritos
     */
    fun addFavorite(songId: String) {
        try {
            val currentTime = System.currentTimeMillis() / 1000
            queries.addFavorite(songId, currentTime)
            _favoritesCache.value = _favoritesCache.value + songId
        } catch (e: Exception) {
            println("Error adding favorite: ${e.message}")
        }
    }

    /**
     * Elimina una canción de favoritos
     */
    fun removeFavorite(songId: String) {
        try {
            queries.removeFavorite(songId)
            _favoritesCache.value = _favoritesCache.value - songId
        } catch (e: Exception) {
            println("Error removing favorite: ${e.message}")
        }
    }

    /**
     * Alterna el estado de favorito de una canción
     */
    fun toggleFavorite(songId: String) {
        if (isFavorite(songId)) {
            removeFavorite(songId)
        } else {
            addFavorite(songId)
        }
    }

    /**
     * Obtiene todos los IDs de canciones favoritas
     */
    fun getAllFavorites(): Set<String> {
        return _favoritesCache.value
    }

    /**
     * Flow que emite el conjunto de IDs favoritos
     */
    fun getAllFavoritesFlow(): Flow<Set<String>> {
        return _favoritesCache
    }

    /**
     * Limpia todos los favoritos
     */
    fun clearAllFavorites() {
        try {
            queries.deleteAll()
            _favoritesCache.value = emptySet()
        } catch (e: Exception) {
            println("Error clearing favorites: ${e.message}")
        }
    }
}
