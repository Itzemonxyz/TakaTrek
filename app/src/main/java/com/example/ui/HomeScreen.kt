package com.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.R
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.SavingsGoal
import com.example.util.CurrencyFormatter
import java.text.NumberFormat
import java.util.Locale
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
    onNavigateToProfile: () -> Unit,
    onNavigateToBadges: () -> Unit
) {
    val goals by viewModel.allGoals.collectAsStateWithLifecycle()
    val isUsd by viewModel.isUsd.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    
    val activeGoals = goals.filter { it.currentAmount < it.targetAmount }
    val completedGoals = goals.filter { it.currentAmount >= it.targetAmount }
    val displayGoals = if (selectedTabIndex == 0) activeGoals else completedGoals
    
    val filteredDisplayGoals = displayGoals.filter {
        it.title.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true)
    }
    
    val haptic = LocalHapticFeedback.current
    val savingTip by viewModel.savingTip.collectAsStateWithLifecycle()
    val monthlyLimitString by viewModel.monthlyLimit.collectAsStateWithLifecycle()

    androidx.compose.runtime.LaunchedEffect(goals.isNotEmpty()) {
        if (goals.isNotEmpty()) viewModel.generateTips()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Image(
                        painter = painterResource(id = R.drawable.ic_takatrek_logo_header),
                        contentDescription = "TakaTrek Logo",
                        modifier = Modifier.height(32.dp)
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
                        com.example.util.ExportUtils.exportToPdf(context, goals, isUsd)
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Export PDF Report", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        com.example.util.ExportUtils.exportToCsv(context, goals)
                    }) {
                        Icon(Icons.Default.Download, contentDescription = "Export CSV", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateToBadges()
                    }) {
                        Icon(androidx.compose.material.icons.Icons.Default.Star, contentDescription = "Badges", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateToProfile()
                    }) {
                        Icon(androidx.compose.material.icons.Icons.Default.AccountCircle, contentDescription = "Profile", tint = MaterialTheme.colorScheme.onPrimaryContainer)
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
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search by title or category...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            if (filteredDisplayGoals.isEmpty()) {
                Column(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(
                        Icons.Default.AccountBalanceWallet,
                        contentDescription = "Empty State",
                        modifier = Modifier.size(120.dp),
                        tint = MaterialTheme.colorScheme.primaryContainer
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    val msg = if (displayGoals.isEmpty()) {
                        if (selectedTabIndex == 0) "No active goals yet.\nTap '+' to create one!" else "No completed goals yet.\nKeep saving!"
                    } else {
                        "No matching goals found."
                    }
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
                    val monthlyLimit = monthlyLimitString.toDoubleOrNull()
                    if (monthlyLimit != null && monthlyLimit > 0) {
                        item {
                            val currentSaved = goals.sumOf { it.currentAmount }
                            val targetProgress = (currentSaved / monthlyLimit).toFloat().coerceIn(0f, 1f)
                            val animatedProgress by androidx.compose.animation.core.animateFloatAsState(
                                targetValue = targetProgress,
                                animationSpec = androidx.compose.animation.core.tween(durationMillis = 1000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                                label = "monthlyProgressAnim"
                            )
                            val isApproaching = targetProgress >= 0.8f
                            val isOverLimit = currentSaved > monthlyLimit

                            val containerCol = if (isOverLimit) MaterialTheme.colorScheme.errorContainer else if (isApproaching) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            val contentCol = if (isOverLimit) MaterialTheme.colorScheme.onErrorContainer else if (isApproaching) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = containerCol,
                                    contentColor = contentCol
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            if (isOverLimit) Icons.Default.Warning else if (isApproaching) Icons.Default.Info else Icons.Default.CheckCircle, 
                                            contentDescription = "Limit Icon", 
                                            tint = contentCol
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Monthly Target Status", style = MaterialTheme.typography.titleMedium, color = contentCol)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Total so far: ${CurrencyFormatter.formatCurrency(currentSaved, isUsd)} / Limit: ${CurrencyFormatter.formatCurrency(monthlyLimit, isUsd)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = contentCol
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = { animatedProgress },
                                        modifier = Modifier.fillMaxWidth().height(8.dp),
                                        color = if (isOverLimit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        trackColor = contentCol.copy(alpha = 0.2f),
                                    )
                                    if (isOverLimit) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("You have exceeded your monthly limit!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                    } else if (isApproaching) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("You are approaching your monthly limit.", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                    item {
                        SavingsHistoryChart()
                    }
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
                    items(filteredDisplayGoals) { goal ->
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
fun GoalCard(goal: SavingsGoal, isUsd: Boolean, onClick: () -> Unit) {
    val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat() else 0f
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
    val catEnum = com.example.data.GoalCategory.fromString(goal.category)

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
                    Surface(shape = androidx.compose.foundation.shape.CircleShape, color = catEnum.color.copy(alpha = 0.15f), modifier = Modifier.size(40.dp)) {
                        Icon(catEnum.icon, contentDescription = goal.category, modifier = Modifier.padding(8.dp), tint = catEnum.color)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(catEnum.displayName.uppercase(), style = MaterialTheme.typography.labelSmall, color = catEnum.color)
                        Text(goal.title, style = MaterialTheme.typography.titleLarge)
                    }
                }
                Surface(
                    color = catEnum.color.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("${(progress * 100).toInt()}%", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = catEnum.color)
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
            val animatedGoalProgress by androidx.compose.animation.core.animateFloatAsState(
                targetValue = progress,
                animationSpec = androidx.compose.animation.core.tween(durationMillis = 1000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                label = "goalProgressAnim"
            )
            LinearProgressIndicator(
                progress = { animatedGoalProgress },
                modifier = Modifier.fillMaxWidth().height(10.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun SavingsHistoryChart() {
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")
    val data = listOf(120f, 250f, 180f, 300f, 210f, 400f) // Mock data for exactly six months
    val maxData = data.maxOrNull() ?: 1f

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                "6-Month Contribution Trend", 
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val barGradient = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.primary
                    )
                )

                data.forEachIndexed { index, value ->
                    val proportion = value / (maxData * 1.1f) // Ensure 10% headroom above tallest bar
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight(proportion.coerceAtLeast(0.08f))
                                    .width(36.dp)
                                    .background(
                                        brush = barGradient, 
                                        shape = RoundedCornerShape(18.dp)
                                    )
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = months.getOrNull(index) ?: "",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
