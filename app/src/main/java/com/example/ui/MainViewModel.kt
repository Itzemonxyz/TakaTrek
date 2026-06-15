package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.SavingsGoal
import com.example.data.SavingsGoalRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.firstOrNull
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.R

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPrefs = application.getSharedPreferences("TakaTrekPrefs", android.content.Context.MODE_PRIVATE)
    
    private val _isUsd = MutableStateFlow(sharedPrefs.getBoolean("isUsd", false))
    val isUsd = _isUsd.asStateFlow()
    
    private val _isAmoledDark = MutableStateFlow(sharedPrefs.getBoolean("isAmoledDark", false))
    val isAmoledDark = _isAmoledDark.asStateFlow()

    fun toggleAmoledDark() {
        val newVal = !_isAmoledDark.value
        sharedPrefs.edit().putBoolean("isAmoledDark", newVal).apply()
        _isAmoledDark.value = newVal
    }
    
    private val _reminderHour = MutableStateFlow(sharedPrefs.getInt("reminderHour", 10))
    val reminderHour = _reminderHour.asStateFlow()
    
    private val _reminderMinute = MutableStateFlow(sharedPrefs.getInt("reminderMinute", 0))
    val reminderMinute = _reminderMinute.asStateFlow()

    fun updateReminderTime(hour: Int, minute: Int) {
        sharedPrefs.edit().putInt("reminderHour", hour).putInt("reminderMinute", minute).apply()
        _reminderHour.value = hour
        _reminderMinute.value = minute
    }

    fun toggleCurrency() {
        val newVal = !_isUsd.value
        sharedPrefs.edit().putBoolean("isUsd", newVal).apply()
        _isUsd.value = newVal
    }

    private val db = AppDatabase.getDatabase(application)

    private val repository = SavingsGoalRepository(db.savingsGoalDao(), db.transactionDao())

    val allGoals: StateFlow<List<SavingsGoal>> = repository.allActiveGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getTransactionsForGoal(goalId: String): StateFlow<List<com.example.data.TransactionEntity>> {
        return repository.getTransactionsForGoal(goalId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    private val _isUserLoggedIn = MutableStateFlow(FirebaseAuth.getInstance().currentUser != null)
    val isUserLoggedIn = _isUserLoggedIn.asStateFlow()
    
    private val _isBiometricAuthenticated = MutableStateFlow(false)
    val isBiometricAuthenticated = _isBiometricAuthenticated.asStateFlow()
    
    private val _savingTip = MutableStateFlow<String>("Loading insights...")
    val savingTip = _savingTip.asStateFlow()
    
    private val _monthlyLimit = MutableStateFlow(sharedPrefs.getString("monthlyLimit", "") ?: "")
    val monthlyLimit = _monthlyLimit.asStateFlow()

    fun updateMonthlyLimit(amount: String) {
        sharedPrefs.edit().putString("monthlyLimit", amount).apply()
        _monthlyLimit.value = amount
    }
    
    fun generateTips() {
        val goals = allGoals.value
        if (goals.isEmpty()) {
            _savingTip.value = "Add some savings goals to get personalized AI tips."
            return
        }
        _savingTip.value = "Analyzing your savings data..."
        viewModelScope.launch {
            val totalTarget = goals.sumOf { it.targetAmount }
            val totalSaved = goals.sumOf { it.currentAmount }
            val formattedCategories = goals.map { it.category }.distinct().joinToString(", ")
            val prompt = "Based on a total target of $totalTarget and current savings of $totalSaved across categories like $formattedCategories, provide some brief, encouraging financial tips."
            _savingTip.value = com.example.api.GeminiHelper.generateContent(prompt)
        }
    }

    fun setBiometricAuthenticated(auth: Boolean) {
        _isBiometricAuthenticated.value = auth
    }

    init {
        if (!sharedPrefs.getBoolean("reminderInitialized", false)) {
            com.example.util.ReminderUtils.scheduleMonthlyReminder(application, _reminderHour.value, _reminderMinute.value)
            sharedPrefs.edit().putBoolean("reminderInitialized", true).apply()
        }
        
        if (_isUserLoggedIn.value) {
            syncData()
        }
    }

    fun login() {
        _isUserLoggedIn.value = true
        _isBiometricAuthenticated.value = true
        syncData()
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
        _isUserLoggedIn.value = false
        repository.stopSyncing()
    }
    
    fun syncData() {
        if (_isUserLoggedIn.value) {
            repository.startSyncing(viewModelScope)
        }
    }
    
    fun addGoal(title: String, amount: Double, deadline: Long, frequency: String, category: String, isAutoDepositEnabled: Boolean = false, autoDepositDayOfMonth: Int = 1) {
        viewModelScope.launch {
            repository.insert(SavingsGoal(title = title, targetAmount = amount, targetDateMillis = deadline, depositFrequency = frequency, category = category, isAutoDepositEnabled = isAutoDepositEnabled, autoDepositDayOfMonth = autoDepositDayOfMonth))
            syncData()
        }
    }
    
    fun addFunds(id: String, amount: Double) {
        viewModelScope.launch {
            val goalBefore = repository.getGoalById(id).firstOrNull()
            repository.addFunds(id, amount)
            val goalAfter = repository.getGoalById(id).firstOrNull()
            
            if (goalBefore != null && goalAfter != null && goalAfter.targetAmount > 0) {
                val progressBefore = goalBefore.currentAmount / goalBefore.targetAmount
                val progressAfter = goalAfter.currentAmount / goalAfter.targetAmount
                
                checkAndSendNotification(goalAfter.title, progressBefore, progressAfter)
            }
            
            syncData()
        }
    }
    
    private fun checkAndSendNotification(title: String, prevProgress: Double, newProgress: Double) {
        val thresholds = listOf(1.0, 0.75, 0.5) // Sort descending so we only trigger the highest crossed
        for (threshold in thresholds) {
            if (prevProgress < threshold && newProgress >= threshold) {
                sendNotification(title, (threshold * 100).toInt())
                break
            }
        }
    }
    
    private fun sendNotification(title: String, percentage: Int) {
        val context = getApplication<Application>()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "taka_trek_milestone_channel"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Goal Milestones", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Notifications for reaching savings milestones"
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_takatrek_app_icon)
            .setContentTitle("Milestone Reached! \uD83C\uDF89")
            .setContentText("You've reached $percentage% of your goal: $title")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
            
        notificationManager.notify(title.hashCode(), notification)
    }
    
    fun deleteGoal(id: String) {
        viewModelScope.launch {
            repository.deleteById(id)
            syncData() // Trigger a push
        }
    }
    
    fun getGoal(id: String): StateFlow<SavingsGoal?> {
        return repository.getGoalById(id).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }

    fun joinSharedGoal(inviteCode: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.joinSharedGoal(inviteCode)
            if (success) {
                syncData()
            }
            onResult(success)
        }
    }
}
