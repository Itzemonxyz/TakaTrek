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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
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
            
            val isAmoledDark by viewModel.isAmoledDark.collectAsStateWithLifecycle()
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("AMOLED Dark Theme", style = MaterialTheme.typography.titleMedium)
                    Text(if (isAmoledDark) "Enabled" else "Disabled", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = isAmoledDark, onCheckedChange = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.toggleAmoledDark() 
                })
            }
            Spacer(modifier = Modifier.height(16.dp))

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
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            
            val monthlyLimit by viewModel.monthlyLimit.collectAsStateWithLifecycle()
            var monthlyLimitInput by remember(monthlyLimit) { mutableStateOf(monthlyLimit) }
            
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Monthly Target Limit", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = monthlyLimitInput,
                        onValueChange = { monthlyLimitInput = it },
                        label = { Text("Amount limit") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.updateMonthlyLimit(monthlyLimitInput)
                    }) {
                        Text("Save")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            
            var inviteCodeInput by remember { mutableStateOf("") }
            var showInviteMessage by remember { mutableStateOf("") }
            
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Join Shared Goal", style = MaterialTheme.typography.titleMedium)
                if (showInviteMessage.isNotEmpty()) {
                    Text(showInviteMessage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = inviteCodeInput,
                        onValueChange = { inviteCodeInput = it },
                        label = { Text("Enter Invite Code") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (inviteCodeInput.isNotBlank()) {
                            viewModel.joinSharedGoal(inviteCodeInput.trim()) { success ->
                                showInviteMessage = if (success) "Successfully joined!" else "Failed to join or code invalid."
                                if (success) inviteCodeInput = ""
                            }
                        }
                    }) {
                        Text("Join")
                    }
                }
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

            var showAboutDialog by remember { mutableStateOf(false) }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showAboutDialog = true
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            ) {
                Text("Help & About")
            }

            if (showAboutDialog) {
                AlertDialog(
                    onDismissRequest = { showAboutDialog = false },
                    title = { Text("TakaTrek") },
                    text = {
                        Column {
                            Text("Version 1.0.0", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Features:", style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("• Personal & Shared Savings Goals\n• Automatic Syncing & Cloud Backup\n• Real-time Analytics & Charts\n• Customizable Reminders\n• AMOLED Dark Theme Support")
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showAboutDialog = false }) {
                            Text("Close")
                        }
                    }
                )
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
