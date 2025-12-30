package com.example.welltracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.welltracker.data.PrefsStore
import com.example.welltracker.data.SharedPreferencesManager

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val prefsStore = PrefsStore(application)
    private val sharedPrefsManager = SharedPreferencesManager(application)
    
    private val _hydrationGoal = MutableLiveData<Int>()
    val hydrationGoal: LiveData<Int> = _hydrationGoal
    
    private val _hydrationCurrent = MutableLiveData<Int>()
    val hydrationCurrent: LiveData<Int> = _hydrationCurrent
    
    private val _reminderEnabled = MutableLiveData<Boolean>()
    val reminderEnabled: LiveData<Boolean> = _reminderEnabled
    
    private val _reminderInterval = MutableLiveData<Int>()
    val reminderInterval: LiveData<Int> = _reminderInterval
    
    private val _darkMode = MutableLiveData<Boolean>()
    val darkMode: LiveData<Boolean> = _darkMode
    
    private val _notificationsEnabled = MutableLiveData<Boolean>()
    val notificationsEnabled: LiveData<Boolean> = _notificationsEnabled
    
    init {
        loadSettings()
    }
    
    private fun getCurrentUserId(): String? {
        return sharedPrefsManager.getCurrentUserEmail()
    }
    
    fun loadSettings() {
        val userId = getCurrentUserId() ?: return
        _hydrationGoal.value = prefsStore.getHydrationGoal(userId)
        _hydrationCurrent.value = prefsStore.getHydrationCurrent(userId)
        _reminderEnabled.value = prefsStore.isReminderEnabled(userId)
        _reminderInterval.value = prefsStore.getReminderInterval(userId)
        _darkMode.value = prefsStore.isDarkMode()
        _notificationsEnabled.value = prefsStore.isNotificationsEnabled()
    }
    
    fun setHydrationGoal(goal: Int) {
        val userId = getCurrentUserId() ?: return
        prefsStore.setHydrationGoal(goal, userId)
        _hydrationGoal.value = goal
    }
    
    fun incrementHydration() {
        val userId = getCurrentUserId() ?: return
        prefsStore.incrementHydration(userId)
        _hydrationCurrent.value = prefsStore.getHydrationCurrent(userId)
    }
    
    fun decrementHydration() {
        val userId = getCurrentUserId() ?: return
        prefsStore.decrementHydration(userId)
        _hydrationCurrent.value = prefsStore.getHydrationCurrent(userId)
    }
    
    fun setReminderEnabled(enabled: Boolean) {
        val userId = getCurrentUserId() ?: return
        prefsStore.setReminderEnabled(enabled, userId)
        _reminderEnabled.value = enabled
    }
    
    fun setReminderInterval(minutes: Int) {
        val userId = getCurrentUserId() ?: return
        prefsStore.setReminderInterval(minutes, userId)
        _reminderInterval.value = minutes
    }
    
    fun setDarkMode(enabled: Boolean) {
        prefsStore.setDarkMode(enabled)
        _darkMode.value = enabled
    }
    
    fun setNotificationsEnabled(enabled: Boolean) {
        prefsStore.setNotificationsEnabled(enabled)
        _notificationsEnabled.value = enabled
    }
    
    fun resetDailyHydration() {
        val userId = getCurrentUserId() ?: return
        prefsStore.resetDailyHydration(userId)
        _hydrationCurrent.value = 0
    }
    
    fun getHydrationProgress(): Int {
        val current = _hydrationCurrent.value ?: 0
        val goal = _hydrationGoal.value ?: 8
        return if (goal > 0) {
            (current * 100) / goal
        } else {
            0
        }
    }
}
