package com.example.habbittracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "habit_completions")
data class HabitCompletion(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val habitId: String,
    val completionDate: String, // Format: "yyyy-MM-dd"
    val completed: Boolean = true,
    val user_id: String = ""
)