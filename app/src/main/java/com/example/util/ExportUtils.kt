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

    fun exportToPdf(context: Context, goals: List<SavingsGoal>, isUsd: Boolean) {
        try {
            val document = android.graphics.pdf.PdfDocument()
            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = document.startPage(pageInfo)
            val canvas: android.graphics.Canvas = page.canvas

            val titlePaint = android.graphics.Paint().apply {
                color = android.graphics.Color.rgb(63, 81, 181) // Indigo 500
                textSize = 28f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            }
            val headerPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 16f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            }
            val itemHeadPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.DKGRAY
                textSize = 14f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            }
            val bodyPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.DKGRAY
                textSize = 12f
            }

            var y = 60f
            val startX = 50f

            canvas.drawText("TakaTrek Savings Report", startX, y, titlePaint)
            y += 30f

            val dateLabel = "Generated on: ${java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}"
            canvas.drawText(dateLabel, startX, y, bodyPaint)
            y += 40f

            val totalTarget = goals.sumOf { it.targetAmount }
            val totalSaved = goals.sumOf { it.currentAmount }
            val overallProgress = if (totalTarget > 0) (totalSaved / totalTarget) * 100 else 0.0

            canvas.drawText("Overview", startX, y, headerPaint)
            y += 25f
            canvas.drawText("Total Goals: ${goals.size}", startX, y, bodyPaint)
            y += 20f
            canvas.drawText("Total Target: ${CurrencyFormatter.formatCurrency(totalTarget, isUsd)}", startX, y, bodyPaint)
            y += 20f
            canvas.drawText("Total Saved: ${CurrencyFormatter.formatCurrency(totalSaved, isUsd)}", startX, y, bodyPaint)
            y += 20f
            canvas.drawText("Overall Progress: ${String.format(java.util.Locale.getDefault(), "%.1f", overallProgress)}%", startX, y, bodyPaint)
            y += 40f

            canvas.drawText("Goal Statistics", startX, y, headerPaint)
            y += 30f

            var currentPage = page
            var currentCanvas = canvas

            for (goal in goals) {
                if (y > 750f) {
                    document.finishPage(currentPage)
                    val newPageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 2).create()
                    currentPage = document.startPage(newPageInfo)
                    currentCanvas = currentPage.canvas
                    y = 60f
                }
                
                val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount) * 100 else 0.0
                
                currentCanvas.drawText("• ${goal.title} (${goal.category})", startX, y, itemHeadPaint)
                y += 20f
                
                val targetText = CurrencyFormatter.formatCurrency(goal.targetAmount, isUsd)
                val currentText = CurrencyFormatter.formatCurrency(goal.currentAmount, isUsd)
                val formatProgress = String.format(java.util.Locale.getDefault(), "%.1f", progress) + "%"
                
                currentCanvas.drawText("Saved: $currentText / $targetText ($formatProgress collected)", startX + 15f, y, bodyPaint)
                y += 15f
                
                val dateStr = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(goal.targetDateMillis))
                currentCanvas.drawText("Target Date: $dateStr | Frequency: ${goal.depositFrequency}", startX + 15f, y, bodyPaint)
                y += 30f
            }

            document.finishPage(currentPage)

            val fileName = "savings_report.pdf"
            val file = java.io.File(context.cacheDir, fileName)
            document.writeTo(java.io.FileOutputStream(file))
            document.close()

            val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(android.content.Intent.EXTRA_SUBJECT, "TakaTrek Savings Report")
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(android.content.Intent.createChooser(intent, "Share PDF Report"))

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
