package com.example.habbittracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey
    val id: String,
    val name: String,
    val displayName: String,
    val iconResId: Int,
    var isSelected: Boolean = false,
    val orderIndex: Int = 0,
    val user_id: String = ""
) {
    companion object {
        // We'll handle icons in the UI, not in the data model
        fun getPredefinedHabits(): List<Habit> {
            return listOf(
                Habit("wake_up_early", "Wake up early", "Wake up early", 0, orderIndex = 0),
                Habit("plan_day", "Plan your day", "Plan your day", 0, orderIndex = 1),
                Habit("mindfulness", "Mindfulness", "Mindfulness", 0, orderIndex = 2),
                Habit("move_body", "Move your body", "Move your body", 0, orderIndex = 3),
                Habit("eat_healthy", "Eat healthy", "Eat healthy", 0, orderIndex = 4),
                Habit("practice_gratitude", "Practice gratitude", "Practice gratitude", 0, orderIndex = 5),
                Habit("digital_break", "Take a digital break", "Take a digital break", 0, orderIndex = 6),
                Habit("enough_sleep", "Get enough sleep", "Get enough sleep", 0, orderIndex = 7)
            )
        }
    }
}