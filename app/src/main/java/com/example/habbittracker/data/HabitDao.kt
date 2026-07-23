package com.example.habbittracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    // ========== EXISTING METHODS (KEEP THESE) ==========

    @Query("SELECT * FROM habits ORDER BY orderIndex")
    fun getAllHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE isSelected = 1 ORDER BY orderIndex")
    fun getSelectedHabits(): Flow<List<Habit>>

    @Update
    suspend fun updateHabit(habit: Habit)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllHabits(habits: List<Habit>)

    @Query("SELECT COUNT(*) FROM habits")
    suspend fun getHabitCount(): Int

    // HabitCompletion operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: HabitCompletion)

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND completionDate = :date")
    suspend fun getCompletion(habitId: String, date: String): HabitCompletion?

    @Query("SELECT * FROM habit_completions WHERE completionDate = :date")
    suspend fun getCompletionsForDate(date: String): List<HabitCompletion>

    @Query("SELECT * FROM habit_completions WHERE completionDate BETWEEN :startDate AND :endDate")
    suspend fun getCompletionsBetweenDates(startDate: String, endDate: String): List<HabitCompletion>

    @Query("DELETE FROM habit_completions WHERE completionDate = :date")
    suspend fun deleteCompletionsForDate(date: String)

    // Hydration Entry operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHydrationEntry(entry: HydrationEntry)

    @Query("SELECT * FROM hydration_entries WHERE date = :date")
    suspend fun getHydrationEntry(date: String): HydrationEntry?

    @Query("SELECT * FROM hydration_entries ORDER BY date DESC LIMIT :limit")
    suspend fun getHydrationHistory(limit: Int = 30): List<HydrationEntry>

    @Query("DELETE FROM hydration_entries WHERE date = :date")
    suspend fun deleteHydrationEntry(date: String)

    // Hydration Settings operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHydrationSettings(settings: HydrationSettings)

    @Query("SELECT * FROM hydration_settings WHERE id = 1")
    suspend fun getHydrationSettings(): HydrationSettings?

    @Update
    suspend fun updateHydrationSettings(settings: HydrationSettings)

    @Query("SELECT COUNT(*) FROM hydration_settings")
    suspend fun getHydrationSettingsCount(): Int

    // MOOD OPERATIONS
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoodEntry(entry: MoodEntry)

    @Query("SELECT * FROM mood_entries WHERE date = :date")
    suspend fun getMoodEntry(date: String): MoodEntry?

    @Query("SELECT * FROM mood_entries ORDER BY date DESC LIMIT :limit")
    suspend fun getMoodHistory(limit: Int = 30): List<MoodEntry>

    @Query("SELECT * FROM mood_entries WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getMoodEntriesBetweenDates(startDate: String, endDate: String): List<MoodEntry>

    @Query("DELETE FROM mood_entries WHERE date = :date")
    suspend fun deleteMoodEntry(date: String)

    @Query("SELECT * FROM habits")
    suspend fun getAllHabitsNoFlow(): List<Habit>

    // ========== NEW USER-SPECIFIC METHODS ==========

    // HABIT USER-SPECIFIC QUERIES
    @Query("SELECT * FROM habits WHERE user_id = :userId ORDER BY orderIndex")
    fun getAllHabitsForUser(userId: String): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE isSelected = 1 AND user_id = :userId ORDER BY orderIndex")
    fun getSelectedHabitsForUser(userId: String): Flow<List<Habit>>

    @Query("SELECT COUNT(*) FROM habits WHERE user_id = :userId")
    suspend fun getHabitCountForUser(userId: String): Int

    // HABIT COMPLETION USER-SPECIFIC QUERIES
    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND completionDate = :date AND user_id = :userId")
    suspend fun getCompletionForUser(habitId: String, date: String, userId: String): HabitCompletion?

    @Query("SELECT * FROM habit_completions WHERE completionDate = :date AND user_id = :userId")
    suspend fun getCompletionsForDateAndUser(date: String, userId: String): List<HabitCompletion>

    @Query("SELECT * FROM habit_completions WHERE completionDate BETWEEN :startDate AND :endDate AND user_id = :userId")
    suspend fun getCompletionsBetweenDatesForUser(startDate: String, endDate: String, userId: String): List<HabitCompletion>

    @Query("DELETE FROM habit_completions WHERE completionDate = :date AND user_id = :userId")
    suspend fun deleteCompletionsForDateAndUser(date: String, userId: String)

    // HYDRATION USER-SPECIFIC QUERIES
    @Query("SELECT * FROM hydration_entries WHERE date = :date AND user_id = :userId")
    suspend fun getHydrationEntryForUser(date: String, userId: String): HydrationEntry?

    @Query("SELECT * FROM hydration_entries WHERE user_id = :userId ORDER BY date DESC LIMIT :limit")
    suspend fun getHydrationHistoryForUser(limit: Int = 30, userId: String): List<HydrationEntry>

    @Query("SELECT * FROM hydration_settings WHERE user_id = :userId")
    suspend fun getHydrationSettingsForUser(userId: String): HydrationSettings?

    @Query("SELECT COUNT(*) FROM hydration_settings WHERE user_id = :userId")
    suspend fun getHydrationSettingsCountForUser(userId: String): Int

    // MOOD USER-SPECIFIC QUERIES
    @Query("SELECT * FROM mood_entries WHERE date = :date AND user_id = :userId")
    suspend fun getMoodEntryForUser(date: String, userId: String): MoodEntry?

    @Query("SELECT * FROM mood_entries WHERE user_id = :userId ORDER BY date DESC LIMIT :limit")
    suspend fun getMoodHistoryForUser(limit: Int = 30, userId: String): List<MoodEntry>

    @Query("SELECT * FROM mood_entries WHERE date BETWEEN :startDate AND :endDate AND user_id = :userId")
    suspend fun getMoodEntriesBetweenDatesForUser(startDate: String, endDate: String, userId: String): List<MoodEntry>
}