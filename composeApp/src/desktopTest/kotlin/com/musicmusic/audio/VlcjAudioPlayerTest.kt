package com.musicmusic.audio

import com.musicmusic.domain.model.PlaybackState
import com.musicmusic.domain.model.RepeatMode
import com.musicmusic.domain.model.Song
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests para VlcjAudioPlayer.
 * 
 * Nota: Estos tests verifican la lógica de estado y control del AudioPlayer,
 * pero algunos tests pueden fallar si VLC no está instalado en el sistema.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class VlcjAudioPlayerTest {
    
    private lateinit var audioPlayer: VlcjAudioPlayer
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        audioPlayer = VlcjAudioPlayer()
    }
    
    @After
    fun tearDown() {
        audioPlayer.release()
    }
    
    // ========== Helper Functions ==========
    
    private fun createTestSong(
        id: String = "test-song",
        title: String = "Test Song",
        artist: String = "Test Artist",
        album: String = "Test Album"
    ): Song {
        return Song(
            id = id,
            title = title,
            artist = artist,
            album = album,
            duration = 180000L, // 3 minutes
            filePath = "test.mp3",
            genre = "Test",
            year = 2024,
            trackNumber = 1,
            coverArtPath = null,
            isFavorite = false
        )
    }
    
    // ========== State Tests ==========
    
    @Test
    fun `initial state should be stopped with null song`() = runTest {
        assertEquals(PlaybackState.STOPPED, audioPlayer.playbackState.first())
        assertEquals(null, audioPlayer.currentSong.first())
        assertEquals(0L, audioPlayer.currentPosition.first())
        assertEquals(0L, audioPlayer.duration.first())
    }
    
    @Test
    fun `initial volume should be 0_5f`() = runTest {
        assertEquals(0.5f, audioPlayer.volume.first())
    }
    
    @Test
    fun `initial shuffle should be disabled`() = runTest {
        assertFalse(audioPlayer.isShuffleEnabled.first())
    }
    
    @Test
    fun `initial repeat mode should be OFF`() = runTest {
        assertEquals(RepeatMode.OFF, audioPlayer.repeatMode.first())
    }
    
    @Test
    fun `initial queue should be empty`() = runTest {
        assertTrue(audioPlayer.queue.first().isEmpty())
        assertEquals(0, audioPlayer.currentIndex.first())
    }
    
    // ========== Volume Tests ==========
    
    @Test
    fun `setVolume should update volume state`() = runTest {
        audioPlayer.setVolume(0.8f)
        advanceUntilIdle()
        
        assertEquals(0.8f, audioPlayer.volume.first())
    }
    
    @Test
    fun `setVolume should clamp to 0_0f - 1_0f range`() = runTest {
        audioPlayer.setVolume(1.5f)
        advanceUntilIdle()
        assertEquals(1.0f, audioPlayer.volume.first())
        
        audioPlayer.setVolume(-0.5f)
        advanceUntilIdle()
        assertEquals(0.0f, audioPlayer.volume.first())
    }
    
    @Test
    fun `increaseVolume should increase by 0_1f by default`() = runTest {
        audioPlayer.setVolume(0.5f)
        advanceUntilIdle()
        
        audioPlayer.increaseVolume()
        advanceUntilIdle()
        
        assertEquals(0.6f, audioPlayer.volume.first(), 0.01f)
    }
    
    @Test
    fun `decreaseVolume should decrease by 0_1f by default`() = runTest {
        audioPlayer.setVolume(0.5f)
        advanceUntilIdle()
        
        audioPlayer.decreaseVolume()
        advanceUntilIdle()
        
        assertEquals(0.4f, audioPlayer.volume.first(), 0.01f)
    }
    
    // ========== Queue Management Tests ==========
    
    @Test
    fun `playQueue should set queue and current song`() = runTest {
        val songs = listOf(
            createTestSong("1", "Song 1"),
            createTestSong("2", "Song 2"),
            createTestSong("3", "Song 3")
        )
        
        audioPlayer.playQueue(songs, 1)
        advanceUntilIdle()
        
        assertEquals(3, audioPlayer.queue.first().size)
        assertEquals(1, audioPlayer.currentIndex.first())
        assertEquals(songs[1].id, audioPlayer.currentSong.first()?.id)
    }
    
    @Test
    fun `getQueue should return current queue`() = runTest {
        val songs = listOf(
            createTestSong("1", "Song 1"),
            createTestSong("2", "Song 2")
        )
        
        audioPlayer.playQueue(songs, 0)
        advanceUntilIdle()
        
        val queue = audioPlayer.getQueue()
        assertEquals(2, queue.size)
        assertEquals(songs[0].id, queue[0].id)
    }
    
    @Test
    fun `addToQueue should add single song to queue`() = runTest {
        val initialSongs = listOf(createTestSong("1", "Song 1"))
        audioPlayer.playQueue(initialSongs, 0)
        advanceUntilIdle()
        
        val newSong = createTestSong("2", "Song 2")
        audioPlayer.addToQueue(newSong)
        advanceUntilIdle()
        
        assertEquals(2, audioPlayer.queue.first().size)
        assertEquals(newSong.id, audioPlayer.queue.first()[1].id)
    }
    
    @Test
    fun `addToQueue should add multiple songs to queue`() = runTest {
        val initialSongs = listOf(createTestSong("1", "Song 1"))
        audioPlayer.playQueue(initialSongs, 0)
        advanceUntilIdle()
        
        val newSongs = listOf(
            createTestSong("2", "Song 2"),
            createTestSong("3", "Song 3")
        )
        audioPlayer.addToQueue(newSongs)
        advanceUntilIdle()
        
        assertEquals(3, audioPlayer.queue.first().size)
    }
    
    @Test
    fun `removeFromQueue should remove song at index`() = runTest {
        val songs = listOf(
            createTestSong("1", "Song 1"),
            createTestSong("2", "Song 2"),
            createTestSong("3", "Song 3")
        )
        
        audioPlayer.playQueue(songs, 0)
        advanceUntilIdle()
        
        audioPlayer.removeFromQueue(1)
        advanceUntilIdle()
        
        assertEquals(2, audioPlayer.queue.first().size)
        assertEquals("1", audioPlayer.queue.first()[0].id)
        assertEquals("3", audioPlayer.queue.first()[1].id)
    }
    
    @Test
    fun `clearQueue should remove all songs`() = runTest {
        val songs = listOf(
            createTestSong("1", "Song 1"),
            createTestSong("2", "Song 2")
        )
        
        audioPlayer.playQueue(songs, 0)
        advanceUntilIdle()
        
        audioPlayer.clearQueue()
        advanceUntilIdle()
        
        assertTrue(audioPlayer.queue.first().isEmpty())
    }
    
    // ========== Shuffle and Repeat Tests ==========
    
    @Test
    fun `setShuffle should update shuffle state`() = runTest {
        audioPlayer.setShuffle(true)
        advanceUntilIdle()
        
        assertTrue(audioPlayer.isShuffleEnabled.first())
        
        audioPlayer.setShuffle(false)
        advanceUntilIdle()
        
        assertFalse(audioPlayer.isShuffleEnabled.first())
    }
    
    @Test
    fun `setRepeatMode should update repeat mode`() = runTest {
        audioPlayer.setRepeatMode(RepeatMode.ALL)
        advanceUntilIdle()
        assertEquals(RepeatMode.ALL, audioPlayer.repeatMode.first())
        
        audioPlayer.setRepeatMode(RepeatMode.ONE)
        advanceUntilIdle()
        assertEquals(RepeatMode.ONE, audioPlayer.repeatMode.first())
        
        audioPlayer.setRepeatMode(RepeatMode.OFF)
        advanceUntilIdle()
        assertEquals(RepeatMode.OFF, audioPlayer.repeatMode.first())
    }
    
    @Test
    fun `shuffle should preserve current song`() = runTest {
        val songs = listOf(
            createTestSong("1", "Song 1"),
            createTestSong("2", "Song 2"),
            createTestSong("3", "Song 3")
        )
        
        audioPlayer.playQueue(songs, 1)
        advanceUntilIdle()
        
        val currentSongBeforeShuffle = audioPlayer.currentSong.first()
        
        audioPlayer.setShuffle(true)
        advanceUntilIdle()
        
        // Current song should remain the same after shuffle
        assertEquals(currentSongBeforeShuffle?.id, audioPlayer.currentSong.first()?.id)
    }
    
    // ========== Playback Control Tests ==========
    
    @Test
    fun `playAtIndex should change current index`() = runTest {
        val songs = listOf(
            createTestSong("1", "Song 1"),
            createTestSong("2", "Song 2"),
            createTestSong("3", "Song 3")
        )
        
        audioPlayer.playQueue(songs, 0)
        advanceUntilIdle()
        
        audioPlayer.playAtIndex(2)
        advanceUntilIdle()
        
        assertEquals(2, audioPlayer.currentIndex.first())
        assertEquals(songs[2].id, audioPlayer.currentSong.first()?.id)
    }
    
    @Test
    fun `next should move to next song in queue`() = runTest {
        val songs = listOf(
            createTestSong("1", "Song 1"),
            createTestSong("2", "Song 2")
        )
        
        audioPlayer.playQueue(songs, 0)
        advanceUntilIdle()
        
        val hasNext = audioPlayer.next()
        advanceUntilIdle()
        
        assertTrue(hasNext)
        assertEquals(1, audioPlayer.currentIndex.first())
        assertEquals(songs[1].id, audioPlayer.currentSong.first()?.id)
    }
    
    @Test
    fun `previous should move to previous song in queue`() = runTest {
        val songs = listOf(
            createTestSong("1", "Song 1"),
            createTestSong("2", "Song 2")
        )
        
        audioPlayer.playQueue(songs, 1)
        advanceUntilIdle()
        
        val hasPrevious = audioPlayer.previous()
        advanceUntilIdle()
        
        assertTrue(hasPrevious)
        assertEquals(0, audioPlayer.currentIndex.first())
        assertEquals(songs[0].id, audioPlayer.currentSong.first()?.id)
    }
    
    @Test
    fun `next at end of queue should return false when repeat is OFF`() = runTest {
        val songs = listOf(
            createTestSong("1", "Song 1"),
            createTestSong("2", "Song 2")
        )
        
        audioPlayer.playQueue(songs, 1) // Start at last song
        audioPlayer.setRepeatMode(RepeatMode.OFF)
        advanceUntilIdle()
        
        val hasNext = audioPlayer.next()
        
        assertFalse(hasNext)
    }
    
    @Test
    fun `previous at start of queue should return false`() = runTest {
        val songs = listOf(
            createTestSong("1", "Song 1"),
            createTestSong("2", "Song 2")
        )
        
        audioPlayer.playQueue(songs, 0) // Start at first song
        advanceUntilIdle()
        
        val hasPrevious = audioPlayer.previous()
        
        assertFalse(hasPrevious)
    }
}
