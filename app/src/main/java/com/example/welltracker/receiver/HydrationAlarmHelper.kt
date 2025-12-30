package com.example.welltracker.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.welltracker.data.SharedPreferencesManager
import java.util.Calendar

/**
 * HydrationAlarmHelper - Manages scheduling and canceling of hydration reminder alarms
 * Uses AlarmManager with interval-based timing for recurring notifications every X minutes
 */
class HydrationAlarmHelper(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val prefs = SharedPreferencesManager(context)

    companion object {
        private const val TAG = "HydrationAlarmHelper"
        private const val ALARM_REQUEST_CODE = 1001
        private const val SNOOZE_REQUEST_CODE = 1002
    }

    /**
     * Schedule interval-based recurring reminders every X minutes
     * @param intervalMinutes The interval in minutes between reminders (default from preferences)
     */
    fun scheduleIntervalReminder(intervalMinutes: Int = prefs.getReminderInterval()) {
        Log.d(TAG, "Scheduling interval reminder every $intervalMinutes minutes")
        
        // Calculate next alarm time
        val intervalMillis = intervalMinutes * 60 * 1000L
        val nextAlarmTime = System.currentTimeMillis() + intervalMillis
        
        // Create pending intent for the alarm
        val intent = Intent(context, HydrationAlarmReceiver::class.java).apply {
            action = HydrationAlarmReceiver.ACTION_HYDRATION_REMINDER
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Schedule the repeating alarm
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Use setRepeating for interval-based reminders
                // Note: setRepeating is not exact but works well for intervals
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    nextAlarmTime,
                    intervalMillis,
                    pendingIntent
                )
                
                Log.d(TAG, "Interval alarm scheduled with setRepeating for every $intervalMinutes minutes")
            } else {
                // For older Android versions
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    nextAlarmTime,
                    intervalMillis,
                    pendingIntent
                )
            }
            
            val calendar = Calendar.getInstance().apply {
                timeInMillis = nextAlarmTime
            }
            
            Log.d(TAG, "Next alarm at: ${calendar.time}")
            
            // Save the next alarm time for UI display
            prefs.setNextReminderTime(nextAlarmTime)
            prefs.setReminderInterval(intervalMinutes)
            
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied for alarms", e)
        }
    }

    /**
     * Schedule a one-time snooze alarm for 15 minutes from now
     */
    fun scheduleSnoozeAlarm() {
        val snoozeTime = System.currentTimeMillis() + (15 * 60 * 1000) // 15 minutes
        
        Log.d(TAG, "Scheduling snooze alarm for 15 minutes")
        
        val intent = Intent(context, HydrationAlarmReceiver::class.java).apply {
            action = HydrationAlarmReceiver.ACTION_HYDRATION_REMINDER
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            SNOOZE_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    snoozeTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    snoozeTime,
                    pendingIntent
                )
            }
            
            // Save snooze time
            prefs.setNextReminderTime(snoozeTime)
            
            Log.d(TAG, "Snooze alarm scheduled successfully")
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied for snooze alarm", e)
        }
    }
    
    /**
     * Reschedule the next interval reminder after an alarm fires
     * This ensures continuous reminders
     */
    fun rescheduleNextReminder() {
        val intervalMinutes = prefs.getReminderInterval()
        val intervalMillis = intervalMinutes * 60 * 1000L
        val nextAlarmTime = System.currentTimeMillis() + intervalMillis
        
        Log.d(TAG, "Rescheduling next reminder in $intervalMinutes minutes")
        
        val intent = Intent(context, HydrationAlarmReceiver::class.java).apply {
            action = HydrationAlarmReceiver.ACTION_HYDRATION_REMINDER
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextAlarmTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    nextAlarmTime,
                    pendingIntent
                )
            }
            
            // Save the next alarm time
            prefs.setNextReminderTime(nextAlarmTime)
            
            Log.d(TAG, "Next reminder rescheduled")
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied for rescheduling", e)
        }
    }

    /**
     * Cancel all scheduled hydration alarms
     */
    fun cancelAlarms() {
        Log.d(TAG, "Canceling all hydration alarms")
        
        // Cancel daily reminder
        val dailyIntent = Intent(context, HydrationAlarmReceiver::class.java).apply {
            action = HydrationAlarmReceiver.ACTION_HYDRATION_REMINDER
        }
        
        val dailyPendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            dailyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(dailyPendingIntent)
        
        // Cancel snooze alarm
        val snoozeIntent = Intent(context, HydrationAlarmReceiver::class.java).apply {
            action = HydrationAlarmReceiver.ACTION_HYDRATION_REMINDER
        }
        
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            SNOOZE_REQUEST_CODE,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(snoozePendingIntent)
        
        // Clear saved next reminder time
        prefs.setNextReminderTime(0)
        
        Log.d(TAG, "All alarms canceled")
    }

    /**
     * Check if exact alarm permission is granted (Android 12+)
     */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true // Permission not required for older versions
        }
    }
}
