package com.example.welltracker.data

import android.content.Context
import android.content.SharedPreferences
import com.example.welltracker.data.model.Habit
import com.example.welltracker.data.model.MoodEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class PrefsStore(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val PREFS_NAME = "wellness_buddy_prefs"
        private const val KEY_HABITS = "habits"
        private const val KEY_MOODS = "moods"
        private const val KEY_HYDRATION_GOAL = "hydration_goal"
        private const val KEY_HYDRATION_CURRENT = "hydration_current"
        private const val KEY_REMINDER_ENABLED = "reminder_enabled"
        private const val KEY_REMINDER_INTERVAL = "reminder_interval"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_WATER_COUNT = "daily_water_count"
        private const val KEY_WATER_DATE = "water_date"
    }
    
    /**
     * Get user-specific key by prefixing with userId
     * This ensures complete data isolation between users
     */
    private fun getUserKey(key: String, userId: String): String {
        return "${userId}_$key"
    }
    
    // Habits
    fun saveHabits(habits: List<Habit>, userId: String) {
        val json = gson.toJson(habits)
        prefs.edit().putString(getUserKey(KEY_HABITS, userId), json).apply()
    }
    
    fun getHabits(userId: String): List<Habit> {
        val json = prefs.getString(getUserKey(KEY_HABITS, userId), null)
        if (json == null) {
            return emptyList() // No sample data for new users
        }
        val type = object : TypeToken<List<Habit>>() {}.type
        val allHabits: List<Habit> = gson.fromJson(json, type) ?: emptyList()
        // Filter to ensure we only return habits for this user
        return allHabits.filter { it.userId == userId }
    }
    
    fun addHabit(habit: Habit, userId: String) {
        val habits = getHabits(userId).toMutableList()
        habits.add(habit.copy(userId = userId)) // Ensure userId is set
        saveHabits(habits, userId)
    }
    
    fun updateHabit(habit: Habit, userId: String) {
        val habits = getHabits(userId).toMutableList()
        val index = habits.indexOfFirst { it.id == habit.id && it.userId == userId }
        if (index != -1) {
            habits[index] = habit.copy(userId = userId)
            saveHabits(habits, userId)
        }
    }
    
    fun deleteHabit(habitId: String, userId: String) {
        val habits = getHabits(userId).toMutableList()
        habits.removeAll { it.id == habitId && it.userId == userId }
        saveHabits(habits, userId)
    }
    
    fun toggleHabitCompletion(habitId: String, userId: String) {
        val habits = getHabits(userId).toMutableList()
        val index = habits.indexOfFirst { it.id == habitId && it.userId == userId }
        if (index != -1) {
            val habit = habits[index]
            habits[index] = habit.copy(
                isCompleted = !habit.isCompleted,
                completedAt = if (!habit.isCompleted) Date() else null
            )
            saveHabits(habits, userId)
        }
    }
    
    // Moods
    fun saveMoods(moods: List<MoodEntry>, userId: String) {
        val json = gson.toJson(moods)
        prefs.edit().putString(getUserKey(KEY_MOODS, userId), json).apply()
    }
    
    fun getMoods(userId: String): List<MoodEntry> {
        val json = prefs.getString(getUserKey(KEY_MOODS, userId), null)
        if (json == null) {
            return emptyList() // No sample data for new users
        }
        val type = object : TypeToken<List<MoodEntry>>() {}.type
        val allMoods: List<MoodEntry> = gson.fromJson(json, type) ?: emptyList()
        // Filter to ensure we only return moods for this user
        return allMoods.filter { it.userId == userId }
    }
    
    fun addMood(mood: MoodEntry, userId: String) {
        val moods = getMoods(userId).toMutableList()
        moods.add(mood.copy(userId = userId)) // Ensure userId is set
        saveMoods(moods, userId)
    }
    
    fun deleteMood(moodId: String, userId: String) {
        val moods = getMoods(userId).toMutableList()
        moods.removeAll { it.id == moodId && it.userId == userId }
        saveMoods(moods, userId)
    }
    
    // Hydration
    fun setHydrationGoal(goal: Int, userId: String) {
        prefs.edit().putInt(getUserKey(KEY_HYDRATION_GOAL, userId), goal).apply()
    }
    
    fun getHydrationGoal(userId: String): Int {
        return prefs.getInt(getUserKey(KEY_HYDRATION_GOAL, userId), 8)
    }
    
    fun setHydrationCurrent(current: Int, userId: String) {
        prefs.edit().putInt(getUserKey(KEY_HYDRATION_CURRENT, userId), current).apply()
    }
    
    fun getHydrationCurrent(userId: String): Int {
        return prefs.getInt(getUserKey(KEY_HYDRATION_CURRENT, userId), 0)
    }
    
    fun incrementHydration(userId: String) {
        val current = getHydrationCurrent(userId)
        setHydrationCurrent(current + 1, userId)
    }
    
    fun decrementHydration(userId: String) {
        val current = getHydrationCurrent(userId)
        if (current > 0) {
            setHydrationCurrent(current - 1, userId)
        }
    }
    
    fun resetDailyHydration(userId: String) {
        setHydrationCurrent(0, userId)
    }
    
    // Settings - User-specific
    fun setReminderEnabled(enabled: Boolean, userId: String) {
        prefs.edit().putBoolean(getUserKey(KEY_REMINDER_ENABLED, userId), enabled).apply()
    }
    
    fun isReminderEnabled(userId: String): Boolean {
        return prefs.getBoolean(getUserKey(KEY_REMINDER_ENABLED, userId), true)
    }
    
    fun setReminderInterval(minutes: Int, userId: String) {
        prefs.edit().putInt(getUserKey(KEY_REMINDER_INTERVAL, userId), minutes).apply()
    }
    
    fun getReminderInterval(userId: String): Int {
        return prefs.getInt(getUserKey(KEY_REMINDER_INTERVAL, userId), 60)
    }
    
    // Settings - Global (not user-specific)
    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }
    
    fun isDarkMode(): Boolean {
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }
    
    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }
    
    fun isNotificationsEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }
    
    // Water Tracking
    fun setWaterCount(count: Int, userId: String) {
        prefs.edit().putInt(getUserKey(KEY_WATER_COUNT, userId), count).apply()
        prefs.edit().putString(getUserKey(KEY_WATER_DATE, userId), getTodayDateString()).apply()
    }
    
    fun getWaterCount(userId: String): Int {
        val savedDate = prefs.getString(getUserKey(KEY_WATER_DATE, userId), "")
        val today = getTodayDateString()
        
        return if (savedDate == today) {
            prefs.getInt(getUserKey(KEY_WATER_COUNT, userId), 0)
        } else {
            // Reset water count for new day
            setWaterCount(0, userId)
            0
        }
    }
    
    private fun getTodayDateString(): String {
        val calendar = java.util.Calendar.getInstance()
        return "${calendar.get(java.util.Calendar.YEAR)}-${calendar.get(java.util.Calendar.MONTH)}-${calendar.get(java.util.Calendar.DAY_OF_MONTH)}"
    }
    
    /**
     * Clear all data for a specific user (called on logout)
     * This ensures complete data isolation and privacy
     */
    fun clearUserData(userId: String) {
        val editor = prefs.edit()
        
        // Remove all user-specific keys
        editor.remove(getUserKey(KEY_HABITS, userId))
        editor.remove(getUserKey(KEY_MOODS, userId))
        editor.remove(getUserKey(KEY_HYDRATION_GOAL, userId))
        editor.remove(getUserKey(KEY_HYDRATION_CURRENT, userId))
        editor.remove(getUserKey(KEY_REMINDER_ENABLED, userId))
        editor.remove(getUserKey(KEY_REMINDER_INTERVAL, userId))
        editor.remove(getUserKey(KEY_WATER_COUNT, userId))
        editor.remove(getUserKey(KEY_WATER_DATE, userId))
        
        editor.apply()
    }
}
