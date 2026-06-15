package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transaction_ledger WHERE goalId = :goalId ORDER BY timestamp DESC")
    fun getTransactionsForGoal(goalId: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transaction_ledger WHERE isSynced = 0")
    suspend fun getUnsyncedTransactions(): List<TransactionEntity>

    @Query("UPDATE transaction_ledger SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
}
