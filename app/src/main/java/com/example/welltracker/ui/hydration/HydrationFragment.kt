package com.example.welltracker.ui.hydration

import android.animation.ObjectAnimator
import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.welltracker.MainActivity
import com.example.welltracker.R
import com.example.welltracker.data.SharedPreferencesManager
import com.example.welltracker.databinding.FragmentHydrationBinding
import com.example.welltracker.receiver.HydrationAlarmHelper
import com.example.welltracker.ui.home.HomeFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * HydrationFragment - Comprehensive water intake tracking screen
 * Features:
 * - Circular progress visualization
 * - Customizable daily goal
 * - Interval-based reminder system (5-60 minutes) with exact AlarmManager scheduling
 * - Automatic daily reset
 * - Data persistence
 */
class HydrationFragment : Fragment() {

    private var _binding: FragmentHydrationBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    private lateinit var alarmHelper: HydrationAlarmHelper
    private var currentWaterCount = 0
    private var dailyGoal = 10
    private var reminderInterval = 20 // Default 20 minutes
    private val TAG = "HydrationFragment"

    companion object {
        private const val MIN_GOAL = 1
        private const val MAX_GOAL = 20
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHydrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            // Initialize SharedPreferencesManager
            sharedPreferencesManager = SharedPreferencesManager(requireContext())
            alarmHelper = HydrationAlarmHelper(requireContext())

            // IMPORTANT: Check if a user is logged in. If not, disable the fragment.
            if (!sharedPreferencesManager.isUserLoggedIn()) {
                // Disable UI elements and show a message
                binding.mainLayout.visibility = View.GONE
                binding.loginPrompt.visibility = View.VISIBLE
                binding.loginPrompt.text = "Please log in or register to track your hydration."
                
                // Cancel any lingering reminders
                cancelReminders()
                return // Stop further execution
            } else {
                // Ensure the main layout is visible and the prompt is hidden
                binding.mainLayout.visibility = View.VISIBLE
                binding.loginPrompt.visibility = View.GONE
            }
            
            // Check if we need to reset daily count
            checkAndResetDaily()
            
            // Load saved data
            loadSavedData()
            
            // Update UI
            updateWaterDisplay()
            updateGoalDisplay()
            updateReminderUI()
            
            // Setup click listeners
            setupClickListeners()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated", e)
            Toast.makeText(context, "Error initializing hydration tracker", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Check if it's a new day and reset water count if needed
     */
    private fun checkAndResetDaily() {
        try {
            val today = getCurrentDate()
            val lastResetDate = sharedPreferencesManager.getLastResetDate()
            
            if (lastResetDate != today) {
                // It's a new day, reset the water count
                sharedPreferencesManager.setWaterCount(0)
                sharedPreferencesManager.setLastResetDate(today)
                currentWaterCount = 0
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in checkAndResetDaily", e)
            // Initialize with defaults in case of error
            currentWaterCount = 0
        }
    }

    /**
     * Get current date in YYYY-MM-DD format
     */
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    /**
     * Load all saved data from SharedPreferences
     */
    private fun loadSavedData() {
        try {
            // Load basic data with default values as fallback
            currentWaterCount = sharedPreferencesManager.getWaterCount()
            dailyGoal = sharedPreferencesManager.getDailyWaterGoal()
            reminderInterval = sharedPreferencesManager.getReminderInterval()
            
            // Update slider with current interval
            binding.sliderInterval.value = reminderInterval.toFloat()
            
            // Update switch with error handling
            binding.switchReminders.isChecked = sharedPreferencesManager.isReminderEnabled()
        } catch (e: Exception) {
            Log.e(TAG, "Error in loadSavedData", e)
            // Set defaults in case of error
            currentWaterCount = 0
            dailyGoal = 10
            reminderInterval = 20
        }
    }

    /**
     * Setup all click listeners for interactive elements
     */
    private fun setupClickListeners() {
        // Home button - navigate back to home fragment
        binding.btnHome.setOnClickListener {
            try {
                val mainActivity = activity as? MainActivity
                if (mainActivity != null) {
                    // Use the bottom navigation to go back to home
                    mainActivity.findViewById<BottomNavigationView>(R.id.bottom_navigation)?.selectedItemId = R.id.nav_home
                } else {
                    // Fallback: use fragment transaction
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment())
                        .commit()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error navigating to home", e)
            }
        }
        
        // Plus button - increment water count
        binding.btnPlus.setOnClickListener {
            if (currentWaterCount < dailyGoal) {
                currentWaterCount++
                sharedPreferencesManager.setWaterCount(currentWaterCount)
                updateWaterDisplay()
                
                // Show encouragement when goal is reached
                if (currentWaterCount == dailyGoal) {
                    Toast.makeText(
                        requireContext(),
                        "🎉 Great job! You've reached your daily goal!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "You've exceeded your daily goal! 💧",
                    Toast.LENGTH_SHORT
                ).show()
                currentWaterCount++
                sharedPreferencesManager.setWaterCount(currentWaterCount)
                updateWaterDisplay()
            }
        }
        
        // Minus button - decrement water count
        binding.btnMinus.setOnClickListener {
            if (currentWaterCount > 0) {
                currentWaterCount--
                sharedPreferencesManager.setWaterCount(currentWaterCount)
                updateWaterDisplay()
            }
        }
        
        // Decrease goal button
        binding.btnDecreaseGoal.setOnClickListener {
            if (dailyGoal > MIN_GOAL) {
                dailyGoal--
                sharedPreferencesManager.setDailyWaterGoal(dailyGoal)
                updateGoalDisplay()
                updateWaterDisplay()
            }
        }
        
        // Increase goal button
        binding.btnIncreaseGoal.setOnClickListener {
            if (dailyGoal < MAX_GOAL) {
                dailyGoal++
                sharedPreferencesManager.setDailyWaterGoal(dailyGoal)
                updateGoalDisplay()
                updateWaterDisplay()
            }
        }
        
        // Reminder switch
        binding.switchReminders.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferencesManager.setReminderEnabled(isChecked)
            
            if (isChecked) {
                // Check for exact alarm permission on Android 12+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (!alarmHelper.canScheduleExactAlarms()) {
                        // Request permission
                        showExactAlarmPermissionDialog()
                        binding.switchReminders.isChecked = false
                        return@setOnCheckedChangeListener
                    }
                }
                
                scheduleReminders()
                Toast.makeText(
                    requireContext(),
                    "Reminders enabled! 🔔 Every $reminderInterval minutes",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                cancelReminders()
                Toast.makeText(
                    requireContext(),
                    "Reminders disabled",
                    Toast.LENGTH_SHORT
                ).show()
            }
            
            updateReminderUI()
        }
        
        // Interval slider - set reminder frequency
        binding.sliderInterval.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                reminderInterval = value.toInt()
                sharedPreferencesManager.setReminderInterval(reminderInterval)
                updateIntervalDisplay()
                
                // Reschedule if reminders are enabled
                if (binding.switchReminders.isChecked) {
                    scheduleReminders()
                    Toast.makeText(
                        requireContext(),
                        "Reminder interval updated to $reminderInterval minutes",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    
    /**
     * Show dialog to request exact alarm permission (Android 12+)
     */
    private fun showExactAlarmPermissionDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Permission Required")
                .setMessage("WellTracker needs permission to schedule exact alarms for precise reminder notifications. Please enable this in Settings.")
                .setPositiveButton("Open Settings") { _, _ ->
                    try {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.parse("package:${requireContext().packageName}")
                        }
                        startActivity(intent)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error opening alarm settings", e)
                        Toast.makeText(requireContext(), "Could not open settings", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
    
    /**
     * Format time in 12-hour format with AM/PM
     */
    /**
     * Format time remaining until next reminder
     */
    private fun formatTimeRemaining(targetTimeMillis: Long): String {
        if (targetTimeMillis == 0L) return "Not scheduled"
        
        val now = System.currentTimeMillis()
        val remaining = targetTimeMillis - now
        
        if (remaining <= 0) return "Soon"
        
        val hours = remaining / (1000 * 60 * 60)
        val minutes = (remaining % (1000 * 60 * 60)) / (1000 * 60)
        
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "Soon"
        }
    }
    
    /**
     * Update water count display and circular progress
     */
    private fun updateWaterDisplay() {
        try {
            // Update text displays
            binding.tvCurrentCount.text = currentWaterCount.toString()
            
            // Calculate progress percentage
            val progress = if (dailyGoal > 0) {
                ((currentWaterCount.toFloat() / dailyGoal.toFloat()) * 100).toInt().coerceIn(0, 100)
            } else {
                0
            }
            
            // Animate progress bar
            animateProgress(progress)
        } catch (e: Exception) {
            Log.e(TAG, "Error in updateWaterDisplay", e)
        }
    }

    /**
     * Animate the circular progress bar smoothly
     */
    private fun animateProgress(targetProgress: Int) {
        try {
            val currentProgress = binding.circularProgress.progress
            
            ObjectAnimator.ofInt(binding.circularProgress, "progress", currentProgress, targetProgress).apply {
                duration = 500
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in animateProgress", e)
            // Set progress without animation as fallback
            try {
                binding.circularProgress.progress = targetProgress
            } catch (e2: Exception) {
                Log.e(TAG, "Fallback progress update failed", e2)
            }
        }
    }

    /**
     * Update daily goal display
     */
    private fun updateGoalDisplay() {
        binding.tvDailyGoal.text = dailyGoal.toString()
    }

    /**
     * Update reminder UI elements
     */
    private fun updateReminderUI() {
        // Update interval display
        updateIntervalDisplay()
        
        if (binding.switchReminders.isChecked) {
            binding.tvNextReminder.visibility = View.VISIBLE
            
            // Get next scheduled reminder time
            val nextReminderTime = sharedPreferencesManager.getNextReminderTime()
            
            if (nextReminderTime > 0) {
                val timeRemaining = formatTimeRemaining(nextReminderTime)
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = nextReminderTime
                }
                val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                val timeString = dateFormat.format(calendar.time)
                
                binding.tvNextReminder.text = "Next reminder: $timeString (in $timeRemaining)"
            } else {
                binding.tvNextReminder.text = "Next reminder in $reminderInterval minutes"
            }
        } else {
            binding.tvNextReminder.visibility = View.GONE
        }
    }
    
    /**
     * Update interval display text
     */
    private fun updateIntervalDisplay() {
        binding.tvIntervalValue.text = "$reminderInterval min"
    }

    /**
     * Schedule interval-based reminders using AlarmManager
     */
    private fun scheduleReminders() {
        if (!isAdded) {
            Log.w(TAG, "Fragment not added, skipping scheduleReminders")
            return
        }
        
        runCatching {
            alarmHelper.scheduleIntervalReminder(reminderInterval)
            updateReminderUI()
            Log.d(TAG, "Interval reminder scheduled for every $reminderInterval minutes")
        }.onFailure {
            Log.e(TAG, "Failed to schedule reminders", it)
        }
    }

    /**
     * Cancel all scheduled reminders
     */
    private fun cancelReminders() {
        if (!isAdded) return
        runCatching {
            alarmHelper.cancelAlarms()
            Log.d(TAG, "Cancelled hydration reminders")
        }.onFailure { Log.e(TAG, "Failed to cancel reminders", it) }
    }

    override fun onResume() {
        super.onResume()
        // Re-sync UI in case preferences changed while fragment not visible
        runCatching {
            reminderInterval = sharedPreferencesManager.getReminderInterval()
            binding.sliderInterval.value = reminderInterval.toFloat()
            updateIntervalDisplay()
            updateReminderUI()
        }
    }

    /**
     * Safe cleanup of binding
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}