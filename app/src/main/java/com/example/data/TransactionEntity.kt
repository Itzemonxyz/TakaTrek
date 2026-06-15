package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "transaction_ledger")
data class TransactionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val goalId: String,
    val amount: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = "",
    val isSynced: Boolean = false
)
