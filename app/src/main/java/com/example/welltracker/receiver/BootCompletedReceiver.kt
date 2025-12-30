package com.example.welltracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.welltracker.data.SharedPreferencesManager

/**
 * BootCompletedReceiver - Restores hydration reminders after device reboot
 * Automatically reschedules alarms when the device finishes booting
 */
class BootCompletedReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootCompletedReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Device boot completed, restoring hydration reminders")
            
            // Check if reminders were enabled before reboot
            val prefs = SharedPreferencesManager(context)
            if (prefs.isReminderEnabled()) {
                // Restore the interval-based alarm
                val alarmHelper = HydrationAlarmHelper(context)
                val intervalMinutes = prefs.getReminderInterval()
                alarmHelper.scheduleIntervalReminder(intervalMinutes)
                
                Log.d(TAG, "Hydration reminders restored successfully (interval: $intervalMinutes min)")
            } else {
                Log.d(TAG, "Reminders were disabled, not restoring")
            }
        }
    }
}
