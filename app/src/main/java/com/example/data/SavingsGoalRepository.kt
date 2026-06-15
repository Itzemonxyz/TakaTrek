package com.example.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SavingsGoalRepository(private val dao: SavingsGoalDao) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var snapshotListener: ListenerRegistration? = null

    val allActiveGoals: Flow<List<SavingsGoal>> = dao.getAllActiveGoals()

    fun getGoalById(id: String): Flow<SavingsGoal> = dao.getGoalById(id)

    suspend fun insert(goal: SavingsGoal) {
        dao.insertGoal(goal.copy(isSynced = false))
    }
    
    suspend fun addFunds(id: String, amount: Double) {
        dao.addFunds(id, amount)
    }

    suspend fun deleteById(id: String) {
        dao.deleteGoalById(id)
    }
    
    suspend fun getUnsyncedGoals(): List<SavingsGoal> = dao.getUnsyncedGoals()
    
    suspend fun markAsSynced(id: String) = dao.markAsSynced(id)
    
    suspend fun insertFromRemote(goal: SavingsGoal) = dao.insertGoal(goal.copy(isSynced = true))

    fun startSyncing(coroutineScope: CoroutineScope) {
        val user = auth.currentUser ?: return
        val userId = user.uid
        val goalsRef = firestore.collection("users").document(userId).collection("goals")

        // 1. Push any local unsynced changes first
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val unsyncedGoals = dao.getUnsyncedGoals()
                for (goal in unsyncedGoals) {
                    if (goal.isDeleted) {
                        goalsRef.document(goal.id).delete().await()
                    } else {
                        val goalMap = mapOf(
                            "id" to goal.id,
                            "title" to goal.title,
                            "targetAmount" to goal.targetAmount,
                            "currentAmount" to goal.currentAmount,
                            "targetDateMillis" to goal.targetDateMillis,
                            "depositFrequency" to goal.depositFrequency,
                            "category" to goal.category,
                            "timestamp" to goal.timestamp
                        )
                        goalsRef.document(goal.id).set(goalMap, SetOptions.merge()).await()
                    }
                    dao.markAsSynced(goal.id)
                }
            } catch (e: Exception) {
                Log.e("SavingsGoalRepository", "Error pushing local data", e)
            }
        }

        // 2. Set up real-time listener for remote changes
        snapshotListener?.remove()
        snapshotListener = goalsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("SavingsGoalRepository", "Listen failed.", error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                coroutineScope.launch(Dispatchers.IO) {
                    for (doc in snapshot.documents) {
                        val remoteGoal = SavingsGoal(
                            id = doc.getString("id") ?: doc.id,
                            title = doc.getString("title") ?: "",
                            targetAmount = doc.getDouble("targetAmount") ?: 0.0,
                            currentAmount = doc.getDouble("currentAmount") ?: 0.0,
                            targetDateMillis = doc.getLong("targetDateMillis") ?: 0L,
                            depositFrequency = doc.getString("depositFrequency") ?: "Monthly",
                            category = doc.getString("category") ?: "Other",
                            timestamp = doc.getLong("timestamp") ?: 0L,
                            isSynced = true,
                            isDeleted = false
                        )
                        // Local priority: if a local copy is unsynced, keep it. Wait, the prompt simplifies it.
                        // Let's just always insert to keep it updated.
                        val localGoal = dao.getGoalByIdSync(remoteGoal.id)
                        if (localGoal == null || localGoal.isSynced || localGoal.timestamp < remoteGoal.timestamp) {
                            dao.insertGoal(remoteGoal)
                        }
                    }
                    
                    // Handle document removals 
                    for (change in snapshot.documentChanges) {
                        if (change.type == com.google.firebase.firestore.DocumentChange.Type.REMOVED) {
                            dao.deleteGoalById(change.document.id)
                        }
                    }
                }
            }
        }
    }

    fun stopSyncing() {
        snapshotListener?.remove()
        snapshotListener = null
    }
}

