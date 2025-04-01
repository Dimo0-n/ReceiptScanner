package com.example.myapplicationtmppp.ui.game

import android.content.Context
import androidx.core.content.edit
import com.example.myapplicationtmppp.R

class SavingsGameManager(private val context: Context) {

    // Structuri de date
    data class UserProgress(
        var totalSaved: Double = 0.0,
        val badges: MutableList<Badge> = mutableListOf(),
        var level: Int = 1
    )

    data class Badge(
        val id: String,
        val name: String,
        val description: String,
        val iconResId: Int
    )

    data class Goal(
        val targetAmount: Double,
        val badge: Badge
    )

    // Obiective predefinite
    private val goals = listOf(
        Goal(
            targetAmount = 500.0,
            badge = Badge(
                id = "eco_1",
                name = "Economist Începător",
                description = "Ai economisit 500 LEI!",
                iconResId = R.drawable.ic_badge_eco1
            )
        ),
        Goal(
            targetAmount = 1000.0,
            badge = Badge(
                id = "eco_2",
                name = "Economist Avansat",
                description = "Ai economisit 1000 LEI!",
                iconResId = R.drawable.ic_badge_eco2
            )
        )
    )

    // Salvează/încarcă progresul
    fun saveProgress(progress: UserProgress) {
        context.getSharedPreferences("user_progress", Context.MODE_PRIVATE).edit {
            putFloat("total_saved", progress.totalSaved.toFloat())
            putInt("level", progress.level)
            putStringSet("badges", progress.badges.map { it.id }.toSet())
        }
    }

    fun loadProgress(): UserProgress {
        val sharedPref = context.getSharedPreferences("user_progress", Context.MODE_PRIVATE)
        return UserProgress(
            totalSaved = sharedPref.getFloat("total_saved", 0f).toDouble(),
            badges = goals
                .filter { sharedPref.getStringSet("badges", emptySet())?.contains(it.badge.id) == true }
                .map { it.badge }
                .toMutableList(),
            level = sharedPref.getInt("level", 1)
        )
    }

    // Actualizează progresul cu o nouă sumă
    fun updateProgress(newAmount: Double): Pair<UserProgress, Badge?> {
        val progress = loadProgress().apply {
            totalSaved += newAmount
            level = (totalSaved / 100).toInt() + 1
        }

        val newBadge = goals.firstOrNull { goal ->
            progress.totalSaved >= goal.targetAmount &&
            progress.badges.none { it.id == goal.badge.id }
        }?.badge

        newBadge?.let { progress.badges.add(it) }
        saveProgress(progress)

        return Pair(progress, newBadge)
    }

    fun getAllBadges(): List<Badge> {
        return goals.map { it.badge }
    }

}