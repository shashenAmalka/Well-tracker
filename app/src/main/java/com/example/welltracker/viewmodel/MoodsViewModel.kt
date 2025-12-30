package com.example.welltracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.welltracker.data.PrefsStore
import com.example.welltracker.data.SharedPreferencesManager
import com.example.welltracker.data.model.MoodEntry
import java.util.Date

class MoodsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val prefsStore = PrefsStore(application)
    private val sharedPrefsManager = SharedPreferencesManager(application)
    
    private val _moods = MutableLiveData<List<MoodEntry>>()
    val moods: LiveData<List<MoodEntry>> = _moods
    
    private val _todayMood = MutableLiveData<MoodEntry?>()
    val todayMood: LiveData<MoodEntry?> = _todayMood
    
    init {
        loadMoods()
    }
    
    private fun getCurrentUserId(): String? {
        return sharedPrefsManager.getCurrentUserEmail()
    }
    
    fun loadMoods() {
        val userId = getCurrentUserId() ?: return
        val moodsList = prefsStore.getMoods(userId).sortedByDescending { it.timestamp }
        _moods.value = moodsList
        updateTodayMood(moodsList)
    }
    
    fun addMood(emoji: String, note: String) {
        val userId = getCurrentUserId() ?: return
        val mood = MoodEntry(
            id = MoodEntry.generateId(),
            userId = userId,
            emoji = emoji,
            note = note,
            timestamp = Date()
        )
        prefsStore.addMood(mood, userId)
        loadMoods()
    }
    
    fun deleteMood(moodId: String) {
        val userId = getCurrentUserId() ?: return
        prefsStore.deleteMood(moodId, userId)
        loadMoods()
    }
    
    private fun updateTodayMood(moods: List<MoodEntry>) {
        val today = Date()
        val todayMoodEntry = moods.find { mood ->
            val moodDate = mood.timestamp
            val moodCalendar = java.util.Calendar.getInstance().apply { time = moodDate }
            val todayCalendar = java.util.Calendar.getInstance().apply { time = today }
            moodCalendar.get(java.util.Calendar.YEAR) == todayCalendar.get(java.util.Calendar.YEAR) &&
            moodCalendar.get(java.util.Calendar.MONTH) == todayCalendar.get(java.util.Calendar.MONTH) &&
            moodCalendar.get(java.util.Calendar.DAY_OF_MONTH) == todayCalendar.get(java.util.Calendar.DAY_OF_MONTH)
        }
        _todayMood.value = todayMoodEntry
    }
    
    fun getMoodTrend(): List<MoodEntry> {
        val allMoods = _moods.value ?: emptyList()
        val calendar = java.util.Calendar.getInstance()
        val trendMoods = mutableListOf<MoodEntry>()
        
        // Get moods from the last 7 days
        repeat(7) { daysBack ->
            calendar.time = Date()
            calendar.add(java.util.Calendar.DAY_OF_YEAR, -daysBack)
            val targetDate = calendar.time
            
            val dayMood = allMoods.find { mood ->
                val moodDate = mood.timestamp
                val moodCalendar = java.util.Calendar.getInstance().apply { time = moodDate }
                val targetCalendar = java.util.Calendar.getInstance().apply { time = targetDate }
                moodCalendar.get(java.util.Calendar.YEAR) == targetCalendar.get(java.util.Calendar.YEAR) &&
                moodCalendar.get(java.util.Calendar.MONTH) == targetCalendar.get(java.util.Calendar.MONTH) &&
                moodCalendar.get(java.util.Calendar.DAY_OF_MONTH) == targetCalendar.get(java.util.Calendar.DAY_OF_MONTH)
            }
            
            if (dayMood != null) {
                trendMoods.add(dayMood)
            }
        }
        
        return trendMoods.reversed()
    }
}
