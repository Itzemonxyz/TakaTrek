package com.example.util

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object ReminderUtils {
    fun scheduleReminder(context: Context, timeInMillis: Long, title: String, message: String, frequency: String) {
        val delayMillis = timeInMillis - System.currentTimeMillis()
        if (delayMillis <= 0) return

        val inputData = Data.Builder()
            .putString("title", title)
            .putString("message", message)
            .putString("frequency", frequency)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }

    fun scheduleMonthlyReminder(context: Context, hour: Int, minute: Int) {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, hour)
        calendar.set(java.util.Calendar.MINUTE, minute)
        calendar.set(java.util.Calendar.SECOND, 0)
        
        var nextTime = calendar.timeInMillis
        if (nextTime <= System.currentTimeMillis()) {
            calendar.add(java.util.Calendar.MONTH, 1)
            nextTime = calendar.timeInMillis
        }
        
        val delayMillis = nextTime - System.currentTimeMillis()
        
        val inputData = Data.Builder()
            .putString("title", "Monthly Savings Reminder")
            .putString("message", "It's time to review and add funds to your savings goals!")
            .putString("frequency", "Monthly_Global")
            .build()
            
        val workRequest = androidx.work.PeriodicWorkRequestBuilder<ReminderWorker>(30, TimeUnit.DAYS)
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .build()
            
        WorkManager.getInstance(context).enqueueUniquePeriodicWork("GlobalMonthlyReminder", androidx.work.ExistingPeriodicWorkPolicy.UPDATE, workRequest)
    }
}
