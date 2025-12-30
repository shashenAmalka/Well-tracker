package com.example.welltracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.welltracker.data.PrefsStore
import com.example.welltracker.data.SharedPreferencesManager
import com.example.welltracker.data.model.Habit
import java.util.Date

class HabitsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val prefsStore = PrefsStore(application)
    private val sharedPrefsManager = SharedPreferencesManager(application)
    
    private val _habits = MutableLiveData<List<Habit>>()
    val habits: LiveData<List<Habit>> = _habits
    
    private val _todayProgress = MutableLiveData<Pair<Int, Int>>()
    val todayProgress: LiveData<Pair<Int, Int>> = _todayProgress
    
    init {
        loadHabits()
    }
    
    private fun getCurrentUserId(): String? {
        return sharedPrefsManager.getCurrentUserEmail()
    }
    
    fun loadHabits() {
        val userId = getCurrentUserId() ?: return
        val habitsList = prefsStore.getHabits(userId)
        _habits.value = habitsList
        updateTodayProgress(habitsList)
    }
    
    fun addHabit(name: String, goal: String) {
        val userId = getCurrentUserId() ?: return
        val habit = Habit(
            id = Habit.generateId(),
            userId = userId,
            name = name,
            goal = goal,
            isCompleted = false,
            createdAt = Date()
        )
        prefsStore.addHabit(habit, userId)
        loadHabits()
    }
    
    fun updateHabit(habit: Habit) {
        val userId = getCurrentUserId() ?: return
        prefsStore.updateHabit(habit, userId)
        loadHabits()
    }
    
    fun deleteHabit(habitId: String) {
        val userId = getCurrentUserId() ?: return
        prefsStore.deleteHabit(habitId, userId)
        loadHabits()
    }
    
    fun toggleHabitCompletion(habitId: String) {
        val userId = getCurrentUserId() ?: return
        prefsStore.toggleHabitCompletion(habitId, userId)
        loadHabits()
    }
    
    private fun updateTodayProgress(habits: List<Habit>) {
        val completed = habits.count { it.isCompleted }
        val total = habits.size
        _todayProgress.value = Pair(completed, total)
    }
    
    fun getCompletionPercentage(): Int {
        val progress = _todayProgress.value ?: Pair(0, 0)
        return if (progress.second > 0) {
            (progress.first * 100) / progress.second
        } else {
            0
        }
    }
}
