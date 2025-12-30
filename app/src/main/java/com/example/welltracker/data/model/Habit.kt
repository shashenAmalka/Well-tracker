package com.example.welltracker.data.model

import java.util.Date

data class Habit(
    val id: String = "",
    val userId: String = "", // User who owns this habit
    val name: String = "",
    val goal: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Date = Date(),
    val completedAt: Date? = null
) {
    companion object {
        fun generateId(): String = System.currentTimeMillis().toString()
    }
}
