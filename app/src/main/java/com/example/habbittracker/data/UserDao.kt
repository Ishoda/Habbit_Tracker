package com.example.habbittracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {

    @Insert//Room automatically generates INSERT SQL
    suspend fun insertUser(user: User)// Room generates SQL for insert

    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    suspend fun login(email: String, password: String): User?// Custom SQL query

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?
    //ensures they run off the main thread in a coroutine, preventing UI freezes.
}