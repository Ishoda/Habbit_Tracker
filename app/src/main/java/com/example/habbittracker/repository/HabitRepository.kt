package com.example.habbittracker.repository

import android.content.Context
import com.example.habbittracker.data.Habit
import com.example.habbittracker.data.HabitCompletion
import com.example.habbittracker.data.HabitDatabase
import kotlinx.coroutines.flow.Flow//reactive data, and date utilities.
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HabitRepository(private val database: HabitDatabase, private val context: Context) {

    private val habitDao = database.habitDao()
    private val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)//Initializes SharedPreferences to store current user’s data

    private fun getCurrentUserId(): String {
        return prefs.getString("current_user_email", "") ?: ""
    }

    suspend fun initializeHabits() {
        try {
            val userId = getCurrentUserId()
            if (habitDao.getHabitCountForUser(userId) == 0) {
                // Create habits with first 3 selected by default
                val defaultHabits = Habit.getPredefinedHabits().mapIndexed { index, habit ->
                    habit.copy(
                        isSelected = index < 3,
                        user_id = userId
                    )
                }
                habitDao.insertAllHabits(defaultHabits)
            }
        } catch (e: Exception) {
            val userId = getCurrentUserId()
            val habitsWithUser = Habit.getPredefinedHabits().map { it.copy(user_id = userId) }
            habitDao.insertAllHabits(habitsWithUser)
        }
    }

    fun getAllHabits(): Flow<List<Habit>> {
        val userId = getCurrentUserId()
        return habitDao.getAllHabitsForUser(userId)// Returns Flow for automatic updates
    }

    fun getSelectedHabits(): Flow<List<Habit>> {//enables reactive UI updates when data changes.
        val userId = getCurrentUserId()
        return habitDao.getSelectedHabitsForUser(userId)
    }

    suspend fun updateHabit(habit: Habit) {
        // Ensure habit has current user_id
        val updatedHabit = habit.copy(user_id = getCurrentUserId())
        habitDao.updateHabit(updatedHabit)
    }

    suspend fun toggleHabitCompletion(habitId: String, date: String = getTodayDate()) {
        val userId = getCurrentUserId()
        try {
            val existingCompletion = habitDao.getCompletionForUser(habitId, date, userId)
            if (existingCompletion != null) {
                habitDao.insertCompletion(existingCompletion.copy(completed = !existingCompletion.completed))
            } else {
                habitDao.insertCompletion(HabitCompletion(
                    habitId = habitId,
                    completionDate = date,
                    user_id = userId  // ← ADD USER ID
                ))
            }
        } catch (e: Exception) {
            habitDao.insertCompletion(HabitCompletion(
                habitId = habitId,
                completionDate = date,
                user_id = userId  // ← ADD USER ID
            ))
        }
    }

    suspend fun getTodayCompletions(): List<HabitCompletion> {
        val userId = getCurrentUserId()
        return try {
            habitDao.getCompletionsForDateAndUser(getTodayDate(), userId)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun isHabitCompletedToday(habitId: String): Boolean {
        val userId = getCurrentUserId()
        return try {
            habitDao.getCompletionForUser(habitId, getTodayDate(), userId)?.completed == true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun saveTodayToHistoryAndReset(): Boolean {
        val userId = getCurrentUserId()
        return try {
            val today = getTodayDate()
            val todayCompletions = habitDao.getCompletionsForDateAndUser(today, userId)

            if (todayCompletions.isNotEmpty()) {
                // Clear today's completions for fresh start
                habitDao.deleteCompletionsForDateAndUser(today, userId)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getHistoryData(): Map<String, List<String>> {
        val userId = getCurrentUserId()
        return try {
            // Get completions for the last 30 days
            val completions = getCompletionsForLast30Days(userId)

            // Get all habits to map IDs to names
            val habitList = getAllHabits().first()
            val habitMap = habitList.associate { habit -> habit.id to habit.displayName }

            // Convert to map of date -> list of habit names
            val historyMap = mutableMapOf<String, List<String>>()

            // Group completions by date
            completions.forEach { completion ->
                if (completion.completed) {
                    val habitName = habitMap[completion.habitId]
                    if (habitName != null) {
                        val currentList = historyMap.getOrDefault(completion.completionDate, emptyList())
                        historyMap[completion.completionDate] = currentList + habitName
                    }
                }
            }

            historyMap
        } catch (e: Exception) {
            emptyMap()
        }
    }

    suspend fun clearTodayCompletions() {
        val userId = getCurrentUserId()
        try {
            habitDao.deleteCompletionsForDateAndUser(getTodayDate(), userId)
        } catch (e: Exception) {
            // Ignore if table doesn't exist
        }
    }

    private suspend fun getCompletionsForLast30Days(userId: String): List<HabitCompletion> {
        return try {
            val calendar = Calendar.getInstance()
            val endDate = getTodayDate()
            calendar.add(Calendar.DAY_OF_YEAR, -30)
            val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            habitDao.getCompletionsBetweenDatesForUser(startDate, endDate, userId)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun getTodayDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}