package com.example.habbittracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hydration_settings")
data class HydrationSettings(
    @PrimaryKey val id: Int = 1, // Always 1 for single settings row
    val currentLevel: Int = 0,
    val lastDate: String = "", // For daily reset check
    val reminderEnabled: Boolean = false,
    val reminderInterval: Int = 2, // hours
    val user_id: String = ""
)