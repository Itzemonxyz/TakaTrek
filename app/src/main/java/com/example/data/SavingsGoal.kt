package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_goal")
data class SavingsGoal(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val targetDateMillis: Long,
    val depositFrequency: String, // "Weekly", "Monthly"
    val timestamp: Long = System.currentTimeMillis(),
    val category: String = "Other",
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false
)
