package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.util.CurrencyFormatter
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    goalId: String,
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val goal by viewModel.getGoal(goalId).collectAsStateWithLifecycle()
    val isUsd by viewModel.isUsd.collectAsStateWithLifecycle()
    var extraFundAmount by remember { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(goal?.title ?: "Analytics") },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.deleteGoal(goalId)
                        onNavigateBack() 
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Goal", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        if (goal == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val validGoal = goal!!
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                
                // Graphical Progress
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    val progress = if (validGoal.targetAmount > 0) (validGoal.currentAmount / validGoal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
                    val animatedProgress by animateFloatAsState(
                        targetValue = progress,
                        animationSpec = tween(durationMillis = 1000),
                        label = "progress"
                    )
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val trackColor = MaterialTheme.colorScheme.surfaceVariant
                    
                    Canvas(modifier = Modifier.size(200.dp)) {
                        drawArc(
                            color = trackColor,
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter = false,
                            style = Stroke(width = 40f, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = primaryColor,
                            startAngle = 135f,
                            sweepAngle = 270f * animatedProgress,
                            useCenter = false,
                            style = Stroke(width = 40f, cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.primary)
                        Text("COMPLETED", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Summary", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Saved: ${CurrencyFormatter.formatCurrency(validGoal.currentAmount, isUsd)}")
                        Text("Target: ${CurrencyFormatter.formatCurrency(validGoal.targetAmount, isUsd)}")
                        Text("Remaining: ${CurrencyFormatter.formatCurrency((validGoal.targetAmount - validGoal.currentAmount).coerceAtLeast(0.0), isUsd)}")
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Add Funds", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = extraFundAmount,
                                onValueChange = { extraFundAmount = it },
                                label = { Text("Amount") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    extraFundAmount.toDoubleOrNull()?.let {
                                        viewModel.addFunds(validGoal.id, it)
                                        extraFundAmount = ""
                                    }
                                }
                            ) {
                                Text("Add")
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = Color.White,
                    ),
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                            Surface(
                                shape = androidx.compose.foundation.shape.CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("?", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("What-If Calculator", style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                        }
                        
                        val remainingAmount = (validGoal.targetAmount - validGoal.currentAmount).coerceAtLeast(0.0)
                        val monthsLeft = ((validGoal.targetDateMillis - System.currentTimeMillis()) / (1000L * 60 * 60 * 24 * 30)).coerceAtLeast(1L).toInt()
                        val currentMonthlyDeposit = remainingAmount / monthsLeft
                        
                        var whatIfExtraInput by remember { mutableStateOf("") }
                        val extraAmount = whatIfExtraInput.toDoubleOrNull() ?: 0.0
                        
                        val (newMonths, monthsSaved) = calculateWhatIf(remainingAmount, currentMonthlyDeposit, extraAmount)
                        
                        val currencySymbol = if (isUsd) "$" else "৳"
                        OutlinedTextField(
                            value = whatIfExtraInput,
                            onValueChange = { whatIfExtraInput = it },
                            label = { Text("Extra Monthly Savings ($currencySymbol)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            singleLine = true
                        )
                        
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(modifier = Modifier.padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                                val message = if (monthsSaved > 0) "Finish $monthsSaved Months Earlier!" else if (remainingAmount == 0.0) "Goal Achieved!" else "Enter an amount to see impact"
                                Text(message, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Black))
                            }
                        }
                        
                        Text("Based on current required avg of ${CurrencyFormatter.formatCurrency(currentMonthlyDeposit, isUsd)}/mo", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.6f), modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                }
            }
        }
    }
}

fun calculateWhatIf(remaining: Double, currentMonthly: Double, extra: Double): Pair<Int, Int> {
    if (remaining <= 0 || currentMonthly <= 0) return Pair(0, 0)
    val newMonthly = currentMonthly + extra
    val originalMonths = Math.ceil(remaining / currentMonthly).toInt()
    val newMonths = Math.ceil(remaining / newMonthly).toInt()
    return Pair(newMonths, (originalMonths - newMonths).coerceAtLeast(0))
}
