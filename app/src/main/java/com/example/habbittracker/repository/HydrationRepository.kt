package com.example.habbittracker.repository

import android.content.Context
import com.example.habbittracker.data.HabitDatabase
import com.example.habbittracker.data.HydrationEntry
import com.example.habbittracker.data.HydrationSettings
import java.text.SimpleDateFormat
import java.util.*

class HydrationRepository(private val database: HabitDatabase, private val context: Context) {

    private val habitDao = database.habitDao()
    private val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)

    private fun getCurrentUserId(): String {
        return prefs.getString("current_user_email", "") ?: ""
    }

    suspend fun initializeSettings() {
        try {
            val userId = getCurrentUserId()
            if (habitDao.getHydrationSettingsCountForUser(userId) == 0) {//Checks if hydration settings exist for the user.
                val defaultSettings = HydrationSettings(
                    id = 1,
                    currentLevel = 0,
                    lastDate = "",
                    reminderEnabled = false,
                    reminderInterval = 2,
                    user_id = userId
                )
                habitDao.insertHydrationSettings(defaultSettings)
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    suspend fun handleDailyReset() {
        val userId = getCurrentUserId()
        val today = getTodayDate()
        val lastDate = getLastDate()

        if (lastDate != today) {
            if (lastDate.isNotEmpty()) {
                val yesterdayEntry = getHydrationEntryForUser(lastDate, userId)
                if (yesterdayEntry == null) {
                    val yesterdayLevel = getCurrentLevel()
                    saveDailyEntryForDate(lastDate, yesterdayLevel)
                }
            }
            setCurrentLevel(0)
            setLastDate(today)
            clearSharedPreferences()
        }
    }

    private fun clearSharedPreferences() {
        try {
            val prefs = context.getSharedPreferences("HydrationPrefs", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
        } catch (e: Exception) {
            // ignore
        }
    }

    suspend fun getCurrentLevel(): Int {
        val userId = getCurrentUserId()
        return try {
            habitDao.getHydrationSettingsForUser(userId)?.currentLevel ?: 0
        } catch (e: Exception) {
            0
        }
    }

    suspend fun setCurrentLevel(level: Int) {
        val userId = getCurrentUserId()
        try {
            val settings = habitDao.getHydrationSettingsForUser(userId) ?: HydrationSettings(user_id = userId)
            habitDao.insertHydrationSettings(settings.copy(currentLevel = level))
        } catch (e: Exception) {
            // ignore
        }
    }

    suspend fun getLastDate(): String {
        val userId = getCurrentUserId()
        return try {
            habitDao.getHydrationSettingsForUser(userId)?.lastDate ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun setLastDate(date: String) {
        val userId = getCurrentUserId()
        try {
            val settings = habitDao.getHydrationSettingsForUser(userId) ?: HydrationSettings(user_id = userId)
            habitDao.insertHydrationSettings(settings.copy(lastDate = date))
        } catch (e: Exception) {
            // ignore
        }
    }

    suspend fun getReminderSettings(): Pair<Boolean, Int> {
        val userId = getCurrentUserId()
        return try {
            val settings = habitDao.getHydrationSettingsForUser(userId) ?: HydrationSettings(user_id = userId)
            Pair(settings.reminderEnabled, settings.reminderInterval)
        } catch (e: Exception) {
            Pair(false, 2)
        }
    }

    suspend fun setReminderSettings(enabled: Boolean, interval: Int) {
        val userId = getCurrentUserId()
        try {
            val settings = habitDao.getHydrationSettingsForUser(userId) ?: HydrationSettings(user_id = userId)
            habitDao.insertHydrationSettings(
                settings.copy(
                    reminderEnabled = enabled,
                    reminderInterval = interval
                )
            )
        } catch (e: Exception) {
            // ignore
        }
    }

    suspend fun saveDailyEntry(cups: Int) {
        val userId = getCurrentUserId()
        try {
            val today = getTodayDate()
            val entry = HydrationEntry(date = today, cups = cups, user_id = userId)
            habitDao.insertHydrationEntry(entry)
        } catch (e: Exception) {
            // ignore
        }
    }

    suspend fun saveDailyEntryForDate(date: String, cups: Int) {
        val userId = getCurrentUserId()
        try {
            val entry = HydrationEntry(date = date, cups = cups, user_id = userId)
            habitDao.insertHydrationEntry(entry)
        } catch (e: Exception) {
            // ignore
        }
    }

    suspend fun getHydrationEntry(date: String): HydrationEntry? {
        val userId = getCurrentUserId()
        return try {
            habitDao.getHydrationEntryForUser(date, userId)
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun getHydrationEntryForUser(date: String, userId: String): HydrationEntry? {
        return try {
            habitDao.getHydrationEntryForUser(date, userId)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getHistory(): List<HydrationEntry> {
        val userId = getCurrentUserId()
        return try {
            habitDao.getHydrationHistoryForUser(30, userId)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getTodayEntry(): HydrationEntry? {
        val userId = getCurrentUserId()
        return try {
            habitDao.getHydrationEntryForUser(getTodayDate(), userId)
        } catch (e: Exception) {
            null
        }
    }

    private fun getTodayDate(): String {
        return SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
    }
}