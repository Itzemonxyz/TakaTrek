package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser
    val isGuest = user == null || user.isAnonymous
    val displayName = user?.displayName
    val email = user?.email
    val context = androidx.compose.ui.platform.LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isUsd by viewModel.isUsd.collectAsStateWithLifecycle()
    val allGoals by viewModel.allGoals.collectAsStateWithLifecycle()
    
    val nameToShow = when {
        isGuest -> "Guest User"
        !displayName.isNullOrBlank() -> displayName
        else -> "Unknown User"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
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
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val photoUrl = user?.photoUrl
            if (!isGuest && photoUrl != null) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = "Profile Icon",
                    modifier = Modifier.size(120.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = nameToShow,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )
            if (!isGuest && !email.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            if (isGuest) {
                Text(
                    text = "You are currently using a guest account. Log in or sign up to save your progress safely in the cloud across all your devices.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.logout()
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) {
                    Text("Log In / Sign Up")
                }
            } else {
                Spacer(modifier = Modifier.height(32.dp))
            }
            
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Currency", style = MaterialTheme.typography.titleMedium)
                    Text(if (isUsd) "USD ($)" else "BDT (৳)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = isUsd, onCheckedChange = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.toggleCurrency() 
                })
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            
            val reminderHour by viewModel.reminderHour.collectAsStateWithLifecycle()
            val reminderMinute by viewModel.reminderMinute.collectAsStateWithLifecycle()
            var showTimePicker by remember { mutableStateOf(false) }
            
            @OptIn(ExperimentalMaterial3Api::class)
            val timePickerState = rememberTimePickerState(initialHour = reminderHour, initialMinute = reminderMinute)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Notifications", style = MaterialTheme.typography.titleMedium)
                    val formattedTime = String.format("%02d:%02d", reminderHour, reminderMinute)
                    Text("Monthly reminder at $formattedTime", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showTimePicker = true 
                }) {
                    Text("Change")
                }
            }
            
            if (showTimePicker) {
                @OptIn(ExperimentalMaterial3Api::class)
                AlertDialog(
                    onDismissRequest = { showTimePicker = false },
                    confirmButton = {
                        TextButton(onClick = { 
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showTimePicker = false 
                            val h = timePickerState.hour
                            val m = timePickerState.minute
                            viewModel.updateReminderTime(h, m)
                            com.example.util.ReminderUtils.scheduleMonthlyReminder(context, h, m)
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

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    com.example.util.ExportUtils.exportToCsv(context, allGoals)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            ) {
                Text("Export Savings History (.CSV)")
            }
            
            if (!isGuest) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.logout() 
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Text("Sign Out")
                }
            }
        }
    }
}
