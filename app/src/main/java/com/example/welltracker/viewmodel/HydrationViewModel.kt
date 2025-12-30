package com.example.welltracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.welltracker.data.PrefsStore
import com.example.welltracker.data.SharedPreferencesManager

class HydrationViewModel(application: Application) : AndroidViewModel(application) {
    
    private val prefsStore = PrefsStore(application)
    private val sharedPrefsManager = SharedPreferencesManager(application)
    
    private val _hydrationCurrent = MutableLiveData<Int>()
    val hydrationCurrent: LiveData<Int> = _hydrationCurrent
    
    private val _hydrationGoal = MutableLiveData<Int>()
    val hydrationGoal: LiveData<Int> = _hydrationGoal
    
    private val _reminderEnabled = MutableLiveData<Boolean>()
    val reminderEnabled: LiveData<Boolean> = _reminderEnabled
    
    private val _reminderInterval = MutableLiveData<Int>()
    val reminderInterval: LiveData<Int> = _reminderInterval
    
    init {
        loadHydrationData()
    }
    
    private fun getCurrentUserId(): String? {
        return sharedPrefsManager.getCurrentUserEmail()
    }
    
    fun loadHydrationData() {
        val userId = getCurrentUserId() ?: return
        _hydrationCurrent.value = prefsStore.getHydrationCurrent(userId)
        _hydrationGoal.value = prefsStore.getHydrationGoal(userId)
        _reminderEnabled.value = prefsStore.isReminderEnabled(userId)
        _reminderInterval.value = prefsStore.getReminderInterval(userId)
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
    
    fun setHydrationGoal(goal: Int) {
        val userId = getCurrentUserId() ?: return
        prefsStore.setHydrationGoal(goal, userId)
        _hydrationGoal.value = goal
    }
    
    fun setReminderEnabled(enabled: Boolean) {
        val userId = getCurrentUserId() ?: return
        prefsStore.setReminderEnabled(enabled, userId)
        _reminderEnabled.value = enabled
    }
    
    fun setReminderInterval(interval: Int) {
        val userId = getCurrentUserId() ?: return
        prefsStore.setReminderInterval(interval, userId)
        _reminderInterval.value = interval
    }
}
