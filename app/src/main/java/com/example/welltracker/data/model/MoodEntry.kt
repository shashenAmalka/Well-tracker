package com.example.welltracker.data.model

import java.util.Date

data class MoodEntry(
    val id: String = "",
    val userId: String = "", // User who owns this mood entry
    val emoji: String = "",
    val note: String = "",
    val timestamp: Date = Date()
) {
    companion object {
        fun generateId(): String = System.currentTimeMillis().toString()
    }
}
