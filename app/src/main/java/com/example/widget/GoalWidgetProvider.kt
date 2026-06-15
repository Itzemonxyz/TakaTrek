package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class GoalWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_goal)
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widgetLayout, pendingIntent)

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context)
            val goals = db.savingsGoalDao().getAllActiveGoals().firstOrNull()
            
            if (!goals.isNullOrEmpty()) {
                val primaryGoal = goals.minByOrNull { it.targetAmount - it.currentAmount } ?: goals.first()
                val progress = if (primaryGoal.targetAmount > 0) ((primaryGoal.currentAmount / primaryGoal.targetAmount) * 100).toInt() else 0
                val progressText = "৳${primaryGoal.currentAmount.toLong()} / ৳${primaryGoal.targetAmount.toLong()}"

                views.setTextViewText(R.id.widgetTitle, primaryGoal.title)
                views.setTextViewText(R.id.widgetProgressText, progressText)
                views.setProgressBar(R.id.widgetProgressBar, 100, progress, false)
            } else {
                views.setTextViewText(R.id.widgetTitle, "No Active Goals")
                views.setTextViewText(R.id.widgetProgressText, "Open app to add one")
                views.setProgressBar(R.id.widgetProgressBar, 100, 0, false)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
