package com.example.habbittracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hydration_entries")
data class HydrationEntry(
    @PrimaryKey val date: String, // yyyyMMdd format
    val cups: Int,
    val user_id: String = ""
)