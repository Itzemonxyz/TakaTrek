package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.MainActivity

class ReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val title = inputData.getString("title") ?: "Reminder!"
        val message = inputData.getString("message") ?: "Time to add funds to your savings goal."
        val frequency = inputData.getString("frequency") ?: "Monthly"
        // Ensure successful display
        showNotification(title, message)
        
        if (frequency != "Monthly_Global") {
            // Schedule next for one-time requests manually
            val nextTime = System.currentTimeMillis() + when(frequency) {
                "Weekly" -> 7L * 24 * 60 * 60 * 1000
                else -> 30L * 24 * 60 * 60 * 1000
            }
            ReminderUtils.scheduleReminder(applicationContext, nextTime, title, message, frequency)
        }
        
        return Result.success()
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION.SDK_INT) { // simplified check since minSDk usually >= 26 but safe to keep checking if needed, or just let it compile
            val channel = NotificationChannel(
                "savings_reminder_channel",
                "Savings Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "savings_reminder_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
