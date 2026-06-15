package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goal ORDER BY timestamp DESC")
    fun getAllGoals(): Flow<List<SavingsGoal>>

    @Query("SELECT * FROM savings_goal WHERE id = :id LIMIT 1")
    fun getGoalById(id: String): Flow<SavingsGoal>

    @Query("SELECT * FROM savings_goal WHERE id = :id LIMIT 1")
    suspend fun getGoalByIdSync(id: String): SavingsGoal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingsGoal)

    @Query("UPDATE savings_goal SET currentAmount = currentAmount + :amount, isSynced = 0 WHERE id = :id")
    suspend fun addFunds(id: String, amount: Double)

    @Query("UPDATE savings_goal SET isDeleted = 1, isSynced = 0 WHERE id = :id")
    suspend fun deleteGoalById(id: String)
    
    @Query("SELECT * FROM savings_goal WHERE isSynced = 0")
    suspend fun getUnsyncedGoals(): List<SavingsGoal>
    
    @Query("UPDATE savings_goal SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)

    @Query("SELECT * FROM savings_goal WHERE isDeleted = 0 ORDER BY timestamp DESC")
    fun getAllActiveGoals(): Flow<List<SavingsGoal>>
}
