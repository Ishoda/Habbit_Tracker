package com.example.habbittracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class HydrationResetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val KEY_HISTORY = "hydrationHistoryJson"

        // Get current date
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        //  Load existing hydration history
        val historyJson = prefs.getString(KEY_HISTORY, "{}") ?: "{}"
        val type = object : TypeToken<MutableMap<String, Int>>() {}.type
        val historyMap: MutableMap<String, Int> =
            try {
                gson.fromJson(historyJson, type) ?: mutableMapOf()
            } catch (e: Exception) {
                mutableMapOf()
            }

        // Record today’s hydration progress before resetting
        val currentLevel = prefs.getInt("currentLevel", 0)
        historyMap[today] = currentLevel // overwrite today's record if it already exists

        //  Save updated history + reset hydration
        prefs.edit().apply {
            putString(KEY_HISTORY, gson.toJson(historyMap))
            putInt("currentLevel", 0)
            putString("lastDate", today) // optional: keep last reset date
            apply()
        }

        Toast.makeText(context, "Hydration progress saved and reset for a new day!", Toast.LENGTH_SHORT).show()
    }
}