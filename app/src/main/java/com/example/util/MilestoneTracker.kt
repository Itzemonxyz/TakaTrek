package com.example.util

import com.example.data.SavingsGoal
import com.example.R

data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val iconRes: Int,
    val isUnlocked: Boolean
)

class MilestoneTracker {
    fun evaluateBadges(goals: List<SavingsGoal>): List<Badge> {
        val totalSaved = goals.sumOf { it.currentAmount }
        val hasFirstTransaction = goals.any { it.currentAmount > 0 }
        val hasHalfwayGoal = goals.any { it.targetAmount > 0 && (it.currentAmount / it.targetAmount) >= 0.5 }
        val takaMagnet = totalSaved >= 50000.0

        return listOf(
            Badge(
                id = "first_step",
                title = "First Step",
                description = "Unlocked by adding your first transaction.",
                iconRes = R.drawable.ic_trophy,
                isUnlocked = hasFirstTransaction
            ),
            Badge(
                id = "halfway_there",
                title = "Halfway There",
                description = "Unlocked when any goal reaches 50% progress.",
                iconRes = R.drawable.ic_trophy,
                isUnlocked = hasHalfwayGoal
            ),
            Badge(
                id = "taka_magnet",
                title = "Taka Magnet",
                description = "Unlocked when total saved exceeds ৳50,000.",
                iconRes = R.drawable.ic_trophy,
                isUnlocked = takaMagnet
            )
        )
    }
}
