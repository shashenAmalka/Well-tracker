package com.example.welltracker.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.welltracker.MainActivity
import com.example.welltracker.R
import com.example.welltracker.data.SharedPreferencesManager

/**
 * HydrationAlarmReceiver - Receives alarm broadcasts and displays hydration notifications
 * Triggered by AlarmManager at the exact scheduled time
 */
class HydrationAlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "HydrationAlarmReceiver"
        private const val CHANNEL_ID = "hydration_reminders"
        private const val CHANNEL_NAME = "Hydration Reminders"
        private const val NOTIFICATION_ID = 1001
        
        const val ACTION_HYDRATION_REMINDER = "com.example.welltracker.HYDRATION_REMINDER"
        const val ACTION_SNOOZE = "com.example.welltracker.SNOOZE_REMINDER"
        const val ACTION_MARK_DRANK = "com.example.welltracker.MARK_DRANK"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received alarm broadcast: ${intent.action}")
        
        when (intent.action) {
            ACTION_HYDRATION_REMINDER -> {
                // Create notification channel if needed
                createNotificationChannel(context)
                
                // Check if reminders are still enabled
                val prefs = SharedPreferencesManager(context)
                if (prefs.isReminderEnabled()) {
                    sendHydrationNotification(context)
                } else {
                    Log.d(TAG, "Reminders disabled, skipping notification")
                }
            }
            ACTION_SNOOZE -> {
                handleSnooze(context)
            }
            ACTION_MARK_DRANK -> {
                handleMarkDrank(context)
            }
        }
    }

    /**
     * Create notification channel for Android O and above
     */
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications to remind you to drink water at scheduled times"
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Send hydration reminder notification with action buttons
     */
    private fun sendHydrationNotification(context: Context) {
        Log.d(TAG, "Sending hydration notification")
        
        // Intent to open the app
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_hydration_tab", true)
        }
        
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Snooze action intent
        val snoozeIntent = Intent(context, HydrationAlarmReceiver::class.java).apply {
            action = ACTION_SNOOZE
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Mark as drank action intent
        val drankIntent = Intent(context, HydrationAlarmReceiver::class.java).apply {
            action = ACTION_MARK_DRANK
        }
        val drankPendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            drankIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build the notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water) // Use water icon
            .setContentTitle("💧 Time to Drink Water!")
            .setContentText("Stay hydrated! Time for your scheduled water intake.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(openAppPendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .addAction(
                android.R.drawable.ic_menu_recent_history,
                "Snooze 15min",
                snoozePendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_add,
                "Mark as Drank",
                drankPendingIntent
            )
            .build()
        
        // Show the notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
        
        Log.d(TAG, "Notification displayed successfully")
    }

    /**
     * Handle snooze action - reschedule notification for 15 minutes later
     */
    private fun handleSnooze(context: Context) {
        Log.d(TAG, "Snooze action triggered")
        
        // Cancel current notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
        
        // Schedule one-time alarm for 15 minutes later
        val alarmHelper = HydrationAlarmHelper(context)
        alarmHelper.scheduleSnoozeAlarm()
        
        // Show toast feedback (if app is in foreground)
        // Note: Can't show toast from receiver reliably, so we'll just log
        Log.d(TAG, "Snoozed for 15 minutes")
    }

    /**
     * Handle mark as drank action - increment water count
     */
    private fun handleMarkDrank(context: Context) {
        Log.d(TAG, "Mark as drank action triggered")
        
        // Cancel current notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
        
        // Increment water count
        val prefs = SharedPreferencesManager(context)
        val currentCount = prefs.getWaterCount()
        prefs.setWaterCount(currentCount + 1)
        
        Log.d(TAG, "Water count incremented to ${currentCount + 1}")
    }
}
