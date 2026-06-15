package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.SavingsGoal
import java.io.File
import java.io.FileWriter

object ExportUtils {
    fun exportToCsv(context: Context, goals: List<SavingsGoal>) {
        try {
            val fileName = "savings_history.csv"
            val file = File(context.cacheDir, fileName)
            val writer = FileWriter(file)
            
            writer.append("ID,Title,TargetAmount,CurrentAmount,TargetDate,Frequency,Category\n")
            for (goal in goals) {
                writer.append("\${goal.id},\${goal.title},\${goal.targetAmount},\${goal.currentAmount},\${goal.targetDateMillis},\${goal.depositFrequency},\${goal.category}\n")
            }
            writer.flush()
            writer.close()
            
            val uri = FileProvider.getUriForFile(context, "\${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "Savings History")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Export Savings History"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
