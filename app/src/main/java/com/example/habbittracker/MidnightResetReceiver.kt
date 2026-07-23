package com.example.habbittracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.google.gson.reflect.TypeToken

class MidnightResetReceiver  : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        // Create a tiny context to call performMidnightReset
        // We will instantiate HabitFragment's logic through direct SharedPreferences manipulation
        // (Safer than trying to instantiate a fragment)
        val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val gson = com.google.gson.Gson()
        val KEY_CHECKED_SET = "todayCheckedSet"  // Use the correct key
        val KEY_HISTORY = "habitHistoryJson"

        // Read current checked set
        val checkedSet = prefs.getStringSet(KEY_CHECKED_SET, emptySet())?.toSet() ?: emptySet()

        // If there are completed habits, add them to history
        if (checkedSet.isNotEmpty()) {
            // Build today's key
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val todayKey = sdf.format(java.util.Date())

            // Load existing history map
            val historyJson = prefs.getString(KEY_HISTORY, "{}")
            val mapType = object : TypeToken<MutableMap<String, List<String>>>() {}.type
            val historyMap: MutableMap<String, List<String>> = try {
                gson.fromJson(historyJson, mapType) ?: mutableMapOf()
            } catch (e: Exception) {
                mutableMapOf()
            }

            // Add today's completed habits to history
            historyMap[todayKey] = checkedSet.toList()

            // Save updated history and reset today's progress
            prefs.edit()
                .putString(KEY_HISTORY, gson.toJson(historyMap))
                .putStringSet(KEY_CHECKED_SET, emptySet()) // Reset today's progress
                .apply()

            // Optional: toast (won't always be visible)
            Toast.makeText(context, "Daily habit progress saved and reset", Toast.LENGTH_SHORT).show()
        }
    }
}