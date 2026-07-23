package com.example.habbittracker.repository

import android.content.Context
import com.example.habbittracker.data.HabitDatabase
import com.example.habbittracker.data.MoodEntry
import java.text.SimpleDateFormat
import java.util.*

class MoodRepository(private val database: HabitDatabase, private val context: Context) {

    private val habitDao = database.habitDao()
    private val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)

    private fun getCurrentUserId(): String {
        return prefs.getString("current_user_email", "") ?: ""
    }

    // Save mood for today
    suspend fun saveMood(moodIndex: Int, note: String) {
        val userId = getCurrentUserId()
        val today = getTodayDate()
        val entry = MoodEntry(date = today, moodIndex = moodIndex, note = note, user_id = userId)
        habitDao.insertMoodEntry(entry)
    }

    // Save mood for specific date
    suspend fun saveMoodForDate(date: String, moodIndex: Int, note: String) {
        val userId = getCurrentUserId()
        val entry = MoodEntry(date = date, moodIndex = moodIndex, note = note, user_id = userId)
        habitDao.insertMoodEntry(entry)
    }

    // Get today's mood
    suspend fun getTodayMood(): MoodEntry? {
        val userId = getCurrentUserId()
        val today = getTodayDate()
        return habitDao.getMoodEntryForUser(today, userId)
    }

    // Get mood for specific date
    suspend fun getMoodForDate(date: String): MoodEntry? {
        val userId = getCurrentUserId()
        return habitDao.getMoodEntryForUser(date, userId)
    }

    // Get mood history (last 30 days)
    suspend fun getMoodHistory(): List<MoodEntry> {
        val userId = getCurrentUserId()
        return habitDao.getMoodHistoryForUser(30, userId)
    }

    // Get mood entries for weekly chart (last 7 days)
    suspend fun getWeeklyMoodEntries(): List<MoodEntry> {
        val userId = getCurrentUserId()
        val calendar = Calendar.getInstance()
        val endDate = getTodayDate()

        calendar.add(Calendar.DAY_OF_YEAR, -6)
        val startDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(calendar.time)

        return habitDao.getMoodEntriesBetweenDatesForUser(startDate, endDate, userId)
    }

    // Utility function
    private fun getTodayDate(): String {
        return SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
    }
}