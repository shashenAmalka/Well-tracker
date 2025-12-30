package com.example.welltracker.util

import android.content.Context
import androidx.work.*
import com.example.welltracker.workers.HydrationWorker
import java.util.concurrent.TimeUnit

object WorkManagerUtil {
    
    private const val HYDRATION_WORK_NAME = "hydration_reminder_work"
    
    fun startHydrationReminders(context: Context, intervalMinutes: Int) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        val hydrationWork = PeriodicWorkRequestBuilder<HydrationWorker>(
            intervalMinutes.toLong(),
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            HYDRATION_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            hydrationWork
        )
    }
    
    fun stopHydrationReminders(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(HYDRATION_WORK_NAME)
    }
}
