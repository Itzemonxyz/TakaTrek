package com.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.SavingsGoal
import com.example.util.CurrencyFormatter
import java.text.NumberFormat
import java.util.Locale
import com.example.R
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToAddGoal: () -> Unit,
    onNavigateToAnalytics: (String) -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val goals by viewModel.allGoals.collectAsStateWithLifecycle()
    val isUsd by viewModel.isUsd.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableStateOf(0) }
    val activeGoals = goals.filter { it.currentAmount < it.targetAmount }
    val completedGoals = goals.filter { it.currentAmount >= it.targetAmount }
    val displayGoals = if (selectedTabIndex == 0) activeGoals else completedGoals
    val haptic = LocalHapticFeedback.current
    val savingTip by viewModel.savingTip.collectAsStateWithLifecycle()

    androidx.compose.runtime.LaunchedEffect(goals.isNotEmpty()) {
        if (goals.isNotEmpty()) viewModel.generateTips()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_takatrek_logo_header),
                        contentDescription = "TakaTrek Logo",
                        modifier = Modifier.height(40.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        com.example.util.ExportUtils.exportToCsv(context, goals)
                    }) {
                        Icon(Icons.Default.Download, contentDescription = "Export CSV", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateToProfile()
                    }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNavigateToAddGoal()
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.add_goal))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Tab(selected = selectedTabIndex == 0, onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    selectedTabIndex = 0 
                }, text = { Text("Active") })
                Tab(selected = selectedTabIndex == 1, onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    selectedTabIndex = 1 
                }, text = { Text("Completed") })
            }
            if (displayGoals.isEmpty()) {
                Column(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(
                        Icons.Default.AccountBalanceWallet,
                        contentDescription = "Empty State",
                        modifier = Modifier.size(120.dp),
                        tint = MaterialTheme.colorScheme.primaryContainer
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    val msg = if (selectedTabIndex == 0) "No active goals yet.\nTap '+' to create one!" else "No completed goals yet.\nKeep saving!"
                    Text(
                        msg, 
                        style = MaterialTheme.typography.titleMedium, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 32.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = "AI Tip", tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Gemini Insights", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(savingTip, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.generateTips()
                                    },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Refresh Insights")
                                }
                            }
                        }
                    }
                    items(displayGoals) { goal ->
                        GoalCard(goal = goal, isUsd = isUsd, onClick = { 
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNavigateToAnalytics(goal.id) 
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun getCategoryIcon(category: String) = when (category) {
    "Education" -> Icons.Default.School
    "Travel" -> Icons.Default.Flight
    "Emergency" -> Icons.Default.Warning
    "Home" -> Icons.Default.Home
    "Gadget" -> Icons.Default.Smartphone
    else -> Icons.Default.Star
}

@Composable
fun GoalCard(goal: SavingsGoal, isUsd: Boolean, onClick: () -> Unit) {
    val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat() else 0f
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = androidx.compose.foundation.shape.CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(40.dp)) {
                        Icon(getCategoryIcon(goal.category), contentDescription = goal.category, modifier = Modifier.padding(8.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(goal.category.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Text(goal.title, style = MaterialTheme.typography.titleLarge)
                    }
                }
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("${(progress * 100).toInt()}%", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column {
                    Text(CurrencyFormatter.formatCurrency(goal.currentAmount, isUsd), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
                    Text("/ ${CurrencyFormatter.formatCurrency(goal.targetAmount, isUsd)}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.End) {
                   Text("DEADLINE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                   Text(sdf.format(Date(goal.targetDateMillis)), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(10.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer,
                strokeCap = StrokeCap.Round
            )
        }
    }
}
