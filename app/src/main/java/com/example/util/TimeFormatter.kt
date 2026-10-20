package com.example.util

import java.util.Locale

object TimeFormatter {

    fun formatDuration(ms: Long, includeMillis: Boolean = false): String {
        val totalSeconds = (ms / 1000).coerceAtLeast(0)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val millisTenths = (ms % 1000) / 100

        return if (includeMillis) {
            String.format(Locale.US, "%02d:%02d.%01d", minutes, seconds, millisTenths)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 KB"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        return if (mb >= 1.0) {
            String.format(Locale.US, "%.1f MB", mb)
        } else {
            String.format(Locale.US, "%.0f KB", kb)
        }
    }
}
