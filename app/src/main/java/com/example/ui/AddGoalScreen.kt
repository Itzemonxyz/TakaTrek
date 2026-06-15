package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("Monthly") }
    var category by remember { mutableStateOf("Other") }
    val categories = listOf("Education", "Travel", "Emergency", "Home", "Gadget", "Other")
    val haptic = LocalHapticFeedback.current

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(initialHour = 10, initialMinute = 0)
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis() + 86400000L * 30
    )
    val selectedDateMillis = datePickerState.selectedDateMillis ?: (System.currentTimeMillis() + 86400000L * 30)
    
    val sdf = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
    val displayDate = sdf.format(selectedDateMillis)
    val displayTime = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showDatePicker = false 
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showDatePicker = false 
                }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showTimePicker = false 
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showTimePicker = false 
                }) {
                    Text("Cancel")
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Goal") },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(24.dp).verticalScroll(rememberScrollState())) {

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Goal Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Target Amount (৳)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = displayDate,
                onValueChange = { },
                label = { Text("Target Deadline") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showDatePicker = true 
                    }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                    }
                },
                modifier = Modifier.fillMaxWidth().clickable { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showDatePicker = true 
                },
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = displayTime,
                onValueChange = { },
                label = { Text("Daily Reminder Time") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showTimePicker = true 
                    }) {
                        Icon(Icons.Default.Schedule, contentDescription = "Select Time")
                    }
                },
                modifier = Modifier.fillMaxWidth().clickable { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showTimePicker = true 
                },
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text("Deposit Frequency", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                FilterChip(
                    selected = frequency == "Weekly",
                    onClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        frequency = "Weekly" 
                    },
                    label = { Text("Weekly") },
                    modifier = Modifier.weight(1f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                )
                FilterChip(
                    selected = frequency == "Monthly",
                    onClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        frequency = "Monthly" 
                    },
                    label = { Text("Monthly") },
                    modifier = Modifier.weight(1f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Category", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { 
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            category = cat 
                        },
                        label = { Text(cat) },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    val target = amount.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && target > 0) {
                        viewModel.addGoal(title, target, selectedDateMillis, frequency, category)
                        val calendar = java.util.Calendar.getInstance()
                        calendar.set(java.util.Calendar.HOUR_OF_DAY, timePickerState.hour)
                        calendar.set(java.util.Calendar.MINUTE, timePickerState.minute)
                        calendar.set(java.util.Calendar.SECOND, 0)
                        
                        var firstReminderTime = calendar.timeInMillis
                        if (firstReminderTime <= System.currentTimeMillis()) {
                            firstReminderTime += if (frequency == "Weekly") 7L * 24 * 60 * 60 * 1000 else 30L * 24 * 60 * 60 * 1000
                        }

                        com.example.util.ReminderUtils.scheduleReminder(
                            context,
                            firstReminderTime,
                            "Savings Reminder",
                            "Time to add funds to '$title'!",
                            frequency
                        )
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                enabled = title.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Save Goal", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
