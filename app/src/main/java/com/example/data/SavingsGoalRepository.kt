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

class SavingsGoalRepository(private val dao: SavingsGoalDao, private val transactionDao: TransactionDao) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var snapshotListener: ListenerRegistration? = null
    private val transactionListeners = mutableListOf<ListenerRegistration>()

    val allActiveGoals: Flow<List<SavingsGoal>> = dao.getAllActiveGoals()

    fun getGoalById(id: String): Flow<SavingsGoal> = dao.getGoalById(id)

    fun getTransactionsForGoal(goalId: String): Flow<List<TransactionEntity>> = transactionDao.getTransactionsForGoal(goalId)

    suspend fun insert(goal: SavingsGoal) {
        dao.insertGoal(goal.copy(isSynced = false))
    }
    
    suspend fun addFunds(id: String, amount: Double, note: String = "Deposit") {
        dao.addFunds(id, amount)
        transactionDao.insertTransaction(
            TransactionEntity(
                goalId = id,
                amount = amount,
                note = note,
                isSynced = false
            )
        )
    }

    suspend fun deleteById(id: String) {
        dao.deleteGoalById(id)
    }
    
    suspend fun getUnsyncedGoals(): List<SavingsGoal> = dao.getUnsyncedGoals()
    
    suspend fun markAsSynced(id: String) = dao.markAsSynced(id)
    
    suspend fun insertFromRemote(goal: SavingsGoal) = dao.insertGoal(goal.copy(isSynced = true))

    fun generateInviteCode(goalId: String): String {
        return goalId.substring(0, 6).uppercase()
    }

    suspend fun joinSharedGoal(inviteCode: String): Boolean {
        val user = auth.currentUser ?: return false
        val userId = user.uid
        
        return try {
            val snapshot = firestore.collection("goals")
                .whereEqualTo("inviteCode", inviteCode.uppercase())
                .limit(1)
                .get()
                .await()
                
            if (!snapshot.isEmpty) {
                val doc = snapshot.documents[0]
                val contributors = (doc.get("contributorIds") as? List<String>)?.toMutableList() ?: mutableListOf()
                if (!contributors.contains(userId)) {
                    contributors.add(userId)
                    doc.reference.update("contributorIds", contributors).await()
                }
                true
            } else {
                false
            }
        } catch(e: Exception) {
            Log.e("SavingsGoalRepository", "Error joining goal", e)
            false
        }
    }

    fun startSyncing(coroutineScope: CoroutineScope) {
        val user = auth.currentUser ?: return
        val userId = user.uid
        val goalsRef = firestore.collection("goals")

        // 1. Push any local unsynced changes first
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val unsyncedGoals = dao.getUnsyncedGoals()
                for (goal in unsyncedGoals) {
                    if (goal.isDeleted) {
                        goalsRef.document(goal.id).delete().await()
                    } else {
                        val contributors = if (goal.contributorIds.isEmpty()) listOf(userId) else goal.contributorIds
                        val invite = if (goal.inviteCode.isEmpty()) goal.id.substring(0, 6).uppercase() else goal.inviteCode
                        
                        val goalMap = mapOf(
                            "id" to goal.id,
                            "title" to goal.title,
                            "targetAmount" to goal.targetAmount,
                            "currentAmount" to goal.currentAmount,
                            "targetDateMillis" to goal.targetDateMillis,
                            "depositFrequency" to goal.depositFrequency,
                            "category" to goal.category,
                            "timestamp" to goal.timestamp,
                            "contributorIds" to contributors,
                            "inviteCode" to invite
                        )
                        goalsRef.document(goal.id).set(goalMap, SetOptions.merge()).await()
                    }
                    dao.markAsSynced(goal.id)
                }

                val unsyncedTransactions = transactionDao.getUnsyncedTransactions()
                for (tx in unsyncedTransactions) {
                    val txMap = mapOf(
                        "id" to tx.id,
                        "goalId" to tx.goalId,
                        "amount" to tx.amount,
                        "timestamp" to tx.timestamp,
                        "note" to tx.note
                    )
                    goalsRef.document(tx.goalId).collection("transactions").document(tx.id)
                        .set(txMap, SetOptions.merge()).await()
                    transactionDao.markAsSynced(tx.id)
                }
            } catch (e: Exception) {
                Log.e("SavingsGoalRepository", "Error pushing local data", e)
            }
        }

        // 2. Set up real-time listener for remote changes
        snapshotListener?.remove()
        snapshotListener = goalsRef.whereArrayContains("contributorIds", userId).addSnapshotListener { snapshot, error ->
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
                            contributorIds = doc.get("contributorIds") as? List<String> ?: emptyList(),
                            inviteCode = doc.getString("inviteCode") ?: "",
                            timestamp = doc.getLong("timestamp") ?: 0L,
                            isSynced = true,
                            isDeleted = false
                        )
                        val localGoal = dao.getGoalByIdSync(remoteGoal.id)
                        if (localGoal == null || localGoal.isSynced || localGoal.timestamp < remoteGoal.timestamp) {
                            dao.insertGoal(remoteGoal)
                        }

                        // Also listen to transactions for this goal
                        setupTransactionListener(remoteGoal.id, goalsRef, coroutineScope)
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

    private fun setupTransactionListener(goalId: String, goalsRef: com.google.firebase.firestore.CollectionReference, coroutineScope: CoroutineScope) {
        val listener = goalsRef.document(goalId).collection("transactions").addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            if (snapshot != null) {
                coroutineScope.launch(Dispatchers.IO) {
                    for (doc in snapshot.documents) {
                        val tx = TransactionEntity(
                            id = doc.getString("id") ?: doc.id,
                            goalId = doc.getString("goalId") ?: goalId,
                            amount = doc.getDouble("amount") ?: 0.0,
                            timestamp = doc.getLong("timestamp") ?: 0L,
                            note = doc.getString("note") ?: "",
                            isSynced = true
                        )
                        transactionDao.insertTransaction(tx)
                    }
                }
            }
        }
        transactionListeners.add(listener)
    }

    fun stopSyncing() {
        snapshotListener?.remove()
        snapshotListener = null
        transactionListeners.forEach { it.remove() }
        transactionListeners.clear()
    }
}

