package com.example.welltracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.example.welltracker.data.PrefsStore
import com.example.welltracker.data.SharedPreferencesManager
import com.example.welltracker.data.model.Habit
import com.example.welltracker.data.model.MoodEntry

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val prefsStore = PrefsStore(application)
    private val sharedPrefsManager = SharedPreferencesManager(application)
    
    private val _habits = MutableLiveData<List<Habit>>()
    val habits: LiveData<List<Habit>> = _habits
    
    private val _moods = MutableLiveData<List<MoodEntry>>()
    val moods: LiveData<List<MoodEntry>> = _moods
    
    private val _hydrationProgress = MutableLiveData<Int>()
    val hydrationProgress: LiveData<Int> = _hydrationProgress
    
    private val _hydrationGoal = MutableLiveData<Int>()
    val hydrationGoal: LiveData<Int> = _hydrationGoal
    
    val todayProgress: LiveData<Pair<Int, Int>> = _habits.map { habitsList ->
        val completed = habitsList.count { it.isCompleted }
        val total = habitsList.size
        Pair(completed, total)
    }
    
    val completionPercentage: LiveData<Int> = todayProgress.map { progress ->
        if (progress.second > 0) {
            (progress.first * 100) / progress.second
        } else {
            0
        }
    }
    
    val todayMood: LiveData<MoodEntry?> = _moods.map { moodsList ->
        val today = java.util.Date()
        moodsList.find { mood ->
            val moodDate = mood.timestamp
            val moodCalendar = java.util.Calendar.getInstance().apply { time = moodDate }
            val todayCalendar = java.util.Calendar.getInstance().apply { time = today }
            moodCalendar.get(java.util.Calendar.YEAR) == todayCalendar.get(java.util.Calendar.YEAR) &&
            moodCalendar.get(java.util.Calendar.MONTH) == todayCalendar.get(java.util.Calendar.MONTH) &&
            moodCalendar.get(java.util.Calendar.DAY_OF_MONTH) == todayCalendar.get(java.util.Calendar.DAY_OF_MONTH)
        }
    }
    
    init {
        loadData()
    }
    
    private fun getCurrentUserId(): String? {
        return sharedPrefsManager.getCurrentUserEmail()
    }
    
    fun loadData() {
        val userId = getCurrentUserId() ?: return
        _habits.value = prefsStore.getHabits(userId)
        _moods.value = prefsStore.getMoods(userId).sortedByDescending { it.timestamp }
        _hydrationProgress.value = prefsStore.getHydrationCurrent(userId)
        _hydrationGoal.value = prefsStore.getHydrationGoal(userId)
    }
    
    fun refresh() {
        loadData()
    }
}
