package com.musicmusic.utils

/**
 * Utility functions for time formatting and manipulation.
 */
object TimeUtils {
    /**
     * Formats duration in milliseconds to MM:SS format.
     *
     * @param durationMs Duration in milliseconds
     * @return Formatted string in MM:SS format
     */
    fun formatDuration(durationMs: Long): String {
        val totalSeconds = durationMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}