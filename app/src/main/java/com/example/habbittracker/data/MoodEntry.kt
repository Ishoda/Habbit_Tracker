package com.example.habbittracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_entries")
data class MoodEntry(
    @PrimaryKey val date: String, // yyyyMMdd format
    val moodIndex: Int,           // 0-4
    val note: String = "",
    val user_id: String = ""
)