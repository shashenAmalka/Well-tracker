package com.example.welltracker.data

import android.content.Context
import android.content.SharedPreferences

/**
 * SharedPreferencesManager - Comprehensive user authentication and session management
 * This class handles all user-related data storage using SharedPreferences exclusively
 */
class SharedPreferencesManager(context: Context) {
    
    private val userPrefs: SharedPreferences = context.getSharedPreferences(USER_PREFS_NAME, Context.MODE_PRIVATE)
    private val sessionPrefs: SharedPreferences = context.getSharedPreferences(SESSION_PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val USER_PREFS_NAME = "user_data"
        private const val SESSION_PREFS_NAME = "session_data"
        
        // User data keys
        private const val KEY_USERNAME_PREFIX = "username_"
        private const val KEY_EMAIL_PREFIX = "email_"
        private const val KEY_PASSWORD_PREFIX = "password_"
        
        // Session keys
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_CURRENT_USER = "current_user"
        private const val KEY_LOGIN_TIMESTAMP = "login_timestamp"
        
        // Hydration tracking keys
        private const val KEY_WATER_COUNT = "water_count"
        private const val KEY_DAILY_WATER_GOAL = "daily_water_goal"
        private const val KEY_REMINDER_ENABLED = "reminder_enabled"
        private const val KEY_REMINDER_INTERVAL = "reminder_interval"
        private const val KEY_LAST_RESET_DATE = "last_reset_date"
        private const val KEY_REMINDER_HOUR = "reminder_hour"
        private const val KEY_REMINDER_MINUTE = "reminder_minute"
        private const val KEY_NEXT_REMINDER_TIME = "next_reminder_time"
    }
    
    /**
     * Register a new user
     * @param username The user's username
     * @param email The user's email address
     * @param password The user's password
     * @return true if registration is successful, false if user already exists
     */
    fun registerUser(username: String, email: String, password: String): Boolean {
        // Check if user already exists (by email)
        if (userExists(email)) {
            return false
        }
        
        // Save user data using email as the unique identifier
        val emailKey = email.lowercase().trim()
        with(userPrefs.edit()) {
            putString("$KEY_USERNAME_PREFIX$emailKey", username)
            putString("$KEY_EMAIL_PREFIX$emailKey", email)
            putString("$KEY_PASSWORD_PREFIX$emailKey", password)
            apply()
        }
        
        return true
    }
    
    /**
     * Sign in a user
     * @param email The user's email address
     * @param password The user's password
     * @return true if sign in is successful, false otherwise
     */
    fun signInUser(email: String, password: String): Boolean {
        val emailKey = email.lowercase().trim()
        val storedPassword = userPrefs.getString("$KEY_PASSWORD_PREFIX$emailKey", null)
        
        if (storedPassword != null && storedPassword == password) {
            // Create session
            with(sessionPrefs.edit()) {
                putBoolean(KEY_IS_LOGGED_IN, true)
                putString(KEY_CURRENT_USER, emailKey)
                putLong(KEY_LOGIN_TIMESTAMP, System.currentTimeMillis())
                apply()
            }
            return true
        }
        
        return false
    }
    
    /**
     * Check if a user exists
     * @param email The user's email address
     * @return true if user exists, false otherwise
     */
    fun userExists(email: String): Boolean {
        val emailKey = email.lowercase().trim()
        return userPrefs.contains("$KEY_EMAIL_PREFIX$emailKey")
    }
    
    /**
     * Check if user is currently logged in
     * @return true if user is logged in, false otherwise
     */
    fun isUserLoggedIn(): Boolean {
        return sessionPrefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    /**
     * Get current logged in user's username
     * @return username if logged in, null otherwise
     */
    fun getCurrentUsername(): String? {
        if (!isUserLoggedIn()) return null
        
        val currentUserEmail = sessionPrefs.getString(KEY_CURRENT_USER, null) ?: return null
        return userPrefs.getString("$KEY_USERNAME_PREFIX$currentUserEmail", null)
    }
    
    /**
     * Get current logged in user's email
     * @return email if logged in, null otherwise
     */
    fun getCurrentUserEmail(): String? {
        if (!isUserLoggedIn()) return null
        
        val currentUserEmail = sessionPrefs.getString(KEY_CURRENT_USER, null) ?: return null
        return userPrefs.getString("$KEY_EMAIL_PREFIX$currentUserEmail", null)
    }
    
    /**
     * Get user data by email
     * @param email The user's email address
     * @return UserData object if user exists, null otherwise
     */
    fun getUserData(email: String): UserData? {
        val emailKey = email.lowercase().trim()
        if (!userExists(email)) return null
        
        val username = userPrefs.getString("$KEY_USERNAME_PREFIX$emailKey", null) ?: return null
        val userEmail = userPrefs.getString("$KEY_EMAIL_PREFIX$emailKey", null) ?: return null
        
        return UserData(username, userEmail)
    }
    
    /**
     * Get current logged in user's data
     * @return UserData object if logged in, null otherwise
     */
    fun getCurrentUserData(): UserData? {
        val username = getCurrentUsername() ?: return null
        val email = getCurrentUserEmail() ?: return null
        return UserData(username, email)
    }
    
    /**
     * Update current user's profile
     * @param newUsername The new username
     * @return true if update is successful, false otherwise
     */
    fun updateCurrentUserProfile(newUsername: String): Boolean {
        if (!isUserLoggedIn()) return false
        
        val currentUserEmail = sessionPrefs.getString(KEY_CURRENT_USER, null) ?: return false
        
        with(userPrefs.edit()) {
            putString("$KEY_USERNAME_PREFIX$currentUserEmail", newUsername)
            apply()
        }
        
        return true
    }
    
    /**
     * Change current user's password
     * @param oldPassword The current password
     * @param newPassword The new password
     * @return true if password change is successful, false otherwise
     */
    fun changePassword(oldPassword: String, newPassword: String): Boolean {
        if (!isUserLoggedIn()) return false
        
        val currentUserEmail = sessionPrefs.getString(KEY_CURRENT_USER, null) ?: return false
        val storedPassword = userPrefs.getString("$KEY_PASSWORD_PREFIX$currentUserEmail", null)
        
        if (storedPassword != null && storedPassword == oldPassword) {
            with(userPrefs.edit()) {
                putString("$KEY_PASSWORD_PREFIX$currentUserEmail", newPassword)
                apply()
            }
            return true
        }
        
        return false
    }
    
    /**
     * Log out the current user
     * Clears all session data
     */
    fun logoutUser() {
        with(sessionPrefs.edit()) {
            clear()
            apply()
        }
    }
    
    /**
     * Get login timestamp
     * @return timestamp of last login, or 0 if not logged in
     */
    fun getLoginTimestamp(): Long {
        return sessionPrefs.getLong(KEY_LOGIN_TIMESTAMP, 0L)
    }
    
    // ==================== HYDRATION TRACKING (USER-SPECIFIC) ====================
    
    private fun getCurrentUserKey(key: String): String? {
        val currentUserEmail = getCurrentUserEmail() ?: return null
        return "${key}_$currentUserEmail"
    }
    
    /**
     * Set the current water intake count for today for the logged-in user
     * @param count The number of glasses consumed
     */
    fun setWaterCount(count: Int) {
        val key = getCurrentUserKey(KEY_WATER_COUNT) ?: return
        with(userPrefs.edit()) {
            putInt(key, count)
            apply()
        }
    }
    
    /**
     * Get the current water intake count for today for the logged-in user
     * @return The number of glasses consumed (default: 0)
     */
    fun getWaterCount(): Int {
        val key = getCurrentUserKey(KEY_WATER_COUNT) ?: return 0
        return userPrefs.getInt(key, 0)
    }
    
    /**
     * Set the daily water goal (number of glasses) for the logged-in user
     * @param goal The target number of glasses per day
     */
    fun setDailyWaterGoal(goal: Int) {
        val key = getCurrentUserKey(KEY_DAILY_WATER_GOAL) ?: return
        with(userPrefs.edit()) {
            putInt(key, goal)
            apply()
        }
    }
    
    /**
     * Get the daily water goal for the logged-in user
     * @return The target number of glasses per day (default: 10)
     */
    fun getDailyWaterGoal(): Int {
        val key = getCurrentUserKey(KEY_DAILY_WATER_GOAL) ?: return 10
        return userPrefs.getInt(key, 10)
    }
    
    /**
     * Set whether hydration reminders are enabled for the logged-in user
     * @param enabled true to enable reminders, false to disable
     */
    fun setReminderEnabled(enabled: Boolean) {
        val key = getCurrentUserKey(KEY_REMINDER_ENABLED) ?: return
        with(userPrefs.edit()) {
            putBoolean(key, enabled)
            apply()
        }
    }
    
    /**
     * Check if hydration reminders are enabled for the logged-in user
     * @return true if reminders are enabled, false otherwise
     */
    fun isReminderEnabled(): Boolean {
        val key = getCurrentUserKey(KEY_REMINDER_ENABLED) ?: return false
        return userPrefs.getBoolean(key, false)
    }
    
    /**
     * Set the reminder interval in minutes for the logged-in user
     * @param minutes The interval between reminders (15-120 minutes)
     */
    fun setReminderInterval(minutes: Int) {
        val key = getCurrentUserKey(KEY_REMINDER_INTERVAL) ?: return
        with(userPrefs.edit()) {
            putInt(key, minutes)
            apply()
        }
    }
    
    /**
     * Get the reminder interval in minutes for the logged-in user
     * @return The interval between reminders (default: 20 minutes)
     */
    fun getReminderInterval(): Int {
        val key = getCurrentUserKey(KEY_REMINDER_INTERVAL) ?: return 20
        return userPrefs.getInt(key, 20)
    }
    
    /**
     * Set the last date when water count was reset for the logged-in user
     * @param date The date string in YYYY-MM-DD format
     */
    fun setLastResetDate(date: String) {
        val key = getCurrentUserKey(KEY_LAST_RESET_DATE) ?: return
        with(userPrefs.edit()) {
            putString(key, date)
            apply()
        }
    }
    
    /**
     * Get the last date when water count was reset for the logged-in user
     * @return The date string in YYYY-MM-DD format (default: empty string)
     */
    fun getLastResetDate(): String {
        val key = getCurrentUserKey(KEY_LAST_RESET_DATE) ?: return ""
        return userPrefs.getString(key, "") ?: ""
    }
    
    /**
     * Clear all user data (for testing or reset purposes)
     * WARNING: This will delete all user accounts
     */
    fun clearAllUserData() {
        with(userPrefs.edit()) {
            clear()
            apply()
        }
        logoutUser()
    }
    
    /**
     * Set the reminder hour (0-23) for the logged-in user
     * @param hour The hour of the day (0-23)
     */
    fun setReminderHour(hour: Int) {
        val key = getCurrentUserKey(KEY_REMINDER_HOUR) ?: return
        with(userPrefs.edit()) {
            putInt(key, hour)
            apply()
        }
    }
    
    /**
     * Get the reminder hour for the logged-in user
     * @return The hour of the day (default: 9 AM)
     */
    fun getReminderHour(): Int {
        val key = getCurrentUserKey(KEY_REMINDER_HOUR) ?: return 9
        return userPrefs.getInt(key, 9)
    }
    
    /**
     * Set the reminder minute (0-59) for the logged-in user
     * @param minute The minute of the hour (0-59)
     */
    fun setReminderMinute(minute: Int) {
        val key = getCurrentUserKey(KEY_REMINDER_MINUTE) ?: return
        with(userPrefs.edit()) {
            putInt(key, minute)
            apply()
        }
    }
    
    /**
     * Get the reminder minute for the logged-in user
     * @return The minute of the hour (default: 0)
     */
    fun getReminderMinute(): Int {
        val key = getCurrentUserKey(KEY_REMINDER_MINUTE) ?: return 0
        return userPrefs.getInt(key, 0)
    }
    
    /**
     * Set the next scheduled reminder time (timestamp) for the logged-in user
     * @param timeMillis The timestamp in milliseconds
     */
    fun setNextReminderTime(timeMillis: Long) {
        val key = getCurrentUserKey(KEY_NEXT_REMINDER_TIME) ?: return
        with(userPrefs.edit()) {
            putLong(key, timeMillis)
            apply()
        }
    }
    
    /**
     * Get the next scheduled reminder time for the logged-in user
     * @return The timestamp in milliseconds (default: 0)
     */
    fun getNextReminderTime(): Long {
        val key = getCurrentUserKey(KEY_NEXT_REMINDER_TIME) ?: return 0
        return userPrefs.getLong(key, 0)
    }
    
    /**
     * Data class to hold user information
     */
    data class UserData(
        val username: String,
        val email: String
    )
}