package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.AppDatabase
import com.example.data.SavingsGoalRepository
import com.example.data.TransactionEntity
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

class AutoDepositWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            val db = AppDatabase.getDatabase(applicationContext)
            val repository = SavingsGoalRepository(db.savingsGoalDao(), db.transactionDao())

            val goals = repository.allActiveGoals.firstOrNull() ?: emptyList()
            val todayDayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

            for (goal in goals) {
                if (goal.isAutoDepositEnabled && goal.autoDepositDayOfMonth == todayDayOfMonth) {
                    val remainingAmount = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)
                    if (remainingAmount > 0) {
                        val monthsLeft = ((goal.targetDateMillis - System.currentTimeMillis()) / (1000L * 60 * 60 * 24 * 30))
                            .coerceAtLeast(1L).toInt()
                        val monthlyDeposit = remainingAmount / monthsLeft

                        repository.addFunds(goal.id, monthlyDeposit, "Auto Deposit")
                    }
                }
            }
            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        }
    }
}
