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

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPrefs = application.getSharedPreferences("TakaTrekPrefs", android.content.Context.MODE_PRIVATE)
    
    private val _isUsd = MutableStateFlow(sharedPrefs.getBoolean("isUsd", false))
    val isUsd = _isUsd.asStateFlow()
    
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

    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java, "savings_db"
    ).fallbackToDestructiveMigration().build()

    private val repository = SavingsGoalRepository(db.savingsGoalDao())

    val allGoals: StateFlow<List<SavingsGoal>> = repository.allActiveGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isUserLoggedIn = MutableStateFlow(FirebaseAuth.getInstance().currentUser != null)
    val isUserLoggedIn = _isUserLoggedIn.asStateFlow()
    
    private val _isBiometricAuthenticated = MutableStateFlow(false)
    val isBiometricAuthenticated = _isBiometricAuthenticated.asStateFlow()
    
    private val _savingTip = MutableStateFlow<String>("Loading insights...")
    val savingTip = _savingTip.asStateFlow()
    
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
    
    fun addGoal(title: String, amount: Double, deadline: Long, frequency: String, category: String) {
        viewModelScope.launch {
            repository.insert(SavingsGoal(title = title, targetAmount = amount, targetDateMillis = deadline, depositFrequency = frequency, category = category))
            syncData()
        }
    }
    
    fun addFunds(id: String, amount: Double) {
        viewModelScope.launch {
            repository.addFunds(id, amount)
            syncData()
        }
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
}
