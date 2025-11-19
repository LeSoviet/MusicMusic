package com.musicmusic.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.musicmusic.database.AppDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests para FavoritesRepository.
 * 
 * Usa una base de datos SQLite en memoria para testing.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesRepositoryTest {
    
    private lateinit var database: AppDatabase
    private lateinit var driver: JdbcSqliteDriver
    private lateinit var repository: FavoritesRepository
    
    @Before
    fun setup() {
        // Crear base de datos en memoria para tests
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        AppDatabase.Schema.create(driver)
        database = AppDatabase(driver)
        
        repository = FavoritesRepository(database)
    }
    
    @After
    fun tearDown() {
        driver.close()
    }
    
    // ========== Basic Operations Tests ==========
    
    @Test
    fun `initially no favorites should exist`() {
        val favorites = repository.getAllFavorites()
        assertTrue(favorites.isEmpty())
    }
    
    @Test
    fun `addFavorite should add song to favorites`() {
        val songId = "test-song-1"
        
        assertFalse(repository.isFavorite(songId))
        
        repository.addFavorite(songId)
        
        assertTrue(repository.isFavorite(songId))
        assertTrue(repository.getAllFavorites().contains(songId))
    }
    
    @Test
    fun `removeFavorite should remove song from favorites`() {
        val songId = "test-song-1"
        
        repository.addFavorite(songId)
        assertTrue(repository.isFavorite(songId))
        
        repository.removeFavorite(songId)
        
        assertFalse(repository.isFavorite(songId))
        assertFalse(repository.getAllFavorites().contains(songId))
    }
    
    @Test
    fun `toggleFavorite should add when not favorite`() {
        val songId = "test-song-1"
        
        assertFalse(repository.isFavorite(songId))
        
        repository.toggleFavorite(songId)
        
        assertTrue(repository.isFavorite(songId))
    }
    
    @Test
    fun `toggleFavorite should remove when already favorite`() {
        val songId = "test-song-1"
        
        repository.addFavorite(songId)
        assertTrue(repository.isFavorite(songId))
        
        repository.toggleFavorite(songId)
        
        assertFalse(repository.isFavorite(songId))
    }
    
    @Test
    fun `toggleFavorite should alternate between states`() {
        val songId = "test-song-1"
        
        // Not favorite initially
        assertFalse(repository.isFavorite(songId))
        
        // Toggle 1: Add to favorites
        repository.toggleFavorite(songId)
        assertTrue(repository.isFavorite(songId))
        
        // Toggle 2: Remove from favorites
        repository.toggleFavorite(songId)
        assertFalse(repository.isFavorite(songId))
        
        // Toggle 3: Add again
        repository.toggleFavorite(songId)
        assertTrue(repository.isFavorite(songId))
    }
    
    // ========== Multiple Favorites Tests ==========
    
    @Test
    fun `can add multiple favorites`() {
        val songIds = listOf("song-1", "song-2", "song-3")
        
        songIds.forEach { songId ->
            repository.addFavorite(songId)
        }
        
        val favorites = repository.getAllFavorites()
        assertEquals(3, favorites.size)
        
        songIds.forEach { songId ->
            assertTrue(repository.isFavorite(songId))
            assertTrue(favorites.contains(songId))
        }
    }
    
    @Test
    fun `adding same favorite twice should not duplicate`() {
        val songId = "test-song-1"
        
        repository.addFavorite(songId)
        repository.addFavorite(songId)
        
        val favorites = repository.getAllFavorites()
        assertEquals(1, favorites.size)
        assertTrue(repository.isFavorite(songId))
    }
    
    @Test
    fun `removing non-existent favorite should not throw error`() {
        val songId = "non-existent-song"
        
        // Should not throw exception
        repository.removeFavorite(songId)
        
        assertFalse(repository.isFavorite(songId))
    }
    
    // ========== Flow Tests ==========
    
    @Test
    fun `isFavoriteFlow should emit current favorite state`() = runTest {
        val songId = "test-song-1"
        
        // Initially not favorite
        val initialState = repository.isFavoriteFlow(songId).first()
        assertFalse(initialState)
        
        // Add to favorites
        repository.addFavorite(songId)
        val afterAdd = repository.isFavoriteFlow(songId).first()
        assertTrue(afterAdd)
        
        // Remove from favorites
        repository.removeFavorite(songId)
        val afterRemove = repository.isFavoriteFlow(songId).first()
        assertFalse(afterRemove)
    }
    
    @Test
    fun `getAllFavoritesFlow should emit all favorites`() = runTest {
        val songIds = listOf("song-1", "song-2", "song-3")
        
        // Initially empty
        val initial = repository.getAllFavoritesFlow().first()
        assertTrue(initial.isEmpty())
        
        // Add favorites one by one
        repository.addFavorite(songIds[0])
        val afterFirst = repository.getAllFavoritesFlow().first()
        assertEquals(1, afterFirst.size)
        assertTrue(afterFirst.contains(songIds[0]))
        
        repository.addFavorite(songIds[1])
        val afterSecond = repository.getAllFavoritesFlow().first()
        assertEquals(2, afterSecond.size)
        
        repository.addFavorite(songIds[2])
        val afterThird = repository.getAllFavoritesFlow().first()
        assertEquals(3, afterThird.size)
        
        songIds.forEach { songId ->
            assertTrue(afterThird.contains(songId))
        }
    }
    
    // ========== Clear All Tests ==========
    
    @Test
    fun `clearAllFavorites should remove all favorites`() {
        val songIds = listOf("song-1", "song-2", "song-3")
        
        songIds.forEach { songId ->
            repository.addFavorite(songId)
        }
        
        assertEquals(3, repository.getAllFavorites().size)
        
        repository.clearAllFavorites()
        
        assertTrue(repository.getAllFavorites().isEmpty())
        songIds.forEach { songId ->
            assertFalse(repository.isFavorite(songId))
        }
    }
    
    @Test
    fun `clearAllFavorites on empty repository should not throw error`() {
        // Should not throw exception
        repository.clearAllFavorites()
        
        assertTrue(repository.getAllFavorites().isEmpty())
    }
    
    // ========== Persistence Tests ==========
    
    @Test
    fun `favorites should persist across repository instances`() {
        val songId = "persistent-song"
        
        // Add favorite in first instance
        repository.addFavorite(songId)
        assertTrue(repository.isFavorite(songId))
        
        // Create new repository instance with same database
        val newRepository = FavoritesRepository(database)
        
        // Favorite should still exist
        assertTrue(newRepository.isFavorite(songId))
        assertTrue(newRepository.getAllFavorites().contains(songId))
    }
    
    @Test
    fun `adding favorite should update timestamp`() {
        val songId = "test-song-1"
        
        val beforeTimestamp = System.currentTimeMillis() / 1000
        
        repository.addFavorite(songId)
        
        // Verify favorite was added (timestamp is internal, we just verify it was added)
        assertTrue(repository.isFavorite(songId))
        
        // Query database directly to verify timestamp was set
        val favorite = database.favoriteQueries.selectAll().executeAsList()
            .find { it.songId == songId }
        
        assertTrue(favorite != null)
        assertTrue(favorite!!.addedAt >= beforeTimestamp)
    }
    
    // ========== Edge Cases Tests ==========
    
    @Test
    fun `isFavorite with empty string should return false`() {
        assertFalse(repository.isFavorite(""))
    }
    
    @Test
    fun `can handle song IDs with special characters`() {
        val specialIds = listOf(
            "song-with-dash",
            "song_with_underscore",
            "song.with.dots",
            "song with spaces",
            "song/with/slashes"
        )
        
        specialIds.forEach { songId ->
            repository.addFavorite(songId)
            assertTrue(repository.isFavorite(songId))
        }
        
        val favorites = repository.getAllFavorites()
        assertEquals(specialIds.size, favorites.size)
    }
    
    @Test
    fun `can handle very long song IDs`() {
        val longId = "a".repeat(1000)
        
        repository.addFavorite(longId)
        assertTrue(repository.isFavorite(longId))
        
        repository.removeFavorite(longId)
        assertFalse(repository.isFavorite(longId))
    }
}
