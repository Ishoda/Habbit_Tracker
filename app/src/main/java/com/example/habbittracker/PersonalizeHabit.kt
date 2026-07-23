package com.example.habbittracker

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PersonalizeHabit : AppCompatActivity() {

    private lateinit var itemWakeUp: LinearLayout
    private lateinit var itemPlanDay: LinearLayout
    private lateinit var itemMindfulness: LinearLayout
    private lateinit var itemMoveBody: LinearLayout
    private lateinit var itemEatHealthy: LinearLayout
    private lateinit var itemGratitude: LinearLayout
    private lateinit var itemDigitalBreak: LinearLayout
    private lateinit var itemSleep: LinearLayout
    private val selectedHabits = mutableSetOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_personalize_habit)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // 🔹 Initialize views
        itemWakeUp = findViewById(R.id.itemWakeUp)
        itemPlanDay = findViewById(R.id.itemPlanDay)
        itemMindfulness = findViewById(R.id.itemMindfulness)
        itemMoveBody = findViewById(R.id.itemMoveBody)
        itemEatHealthy = findViewById(R.id.itemEatHealthy)
        itemGratitude = findViewById(R.id.itemGratitude)
        itemDigitalBreak = findViewById(R.id.itemDigitalBreak)
        itemSleep = findViewById(R.id.itemSleep)
        val btnHabit = findViewById<Button>(R.id.btnHabit)

        // 🔹 Shared Preferences
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        val saved = prefs.getStringSet("selectedHabits", emptySet()) ?: emptySet()
        selectedHabits.addAll(saved)

        // 🔹 Restore selections
        restoreSelection(itemWakeUp, "Wake up early")
        restoreSelection(itemPlanDay, "Plan your day")
        restoreSelection(itemMindfulness, "Mindfulness")
        restoreSelection(itemMoveBody, "Move your body")
        restoreSelection(itemEatHealthy, "Eat healthy")
        restoreSelection(itemGratitude, "Practice gratitude")
        restoreSelection(itemDigitalBreak, "Take a digital break")
        restoreSelection(itemSleep, "Get enough sleep")

        // 🔹 Toggle selections
        itemWakeUp.setOnClickListener { toggleSelection(itemWakeUp, "Wake up early") }
        itemPlanDay.setOnClickListener { toggleSelection(itemPlanDay, "Plan your day") }
        itemMindfulness.setOnClickListener { toggleSelection(itemMindfulness, "Mindfulness") }
        itemMoveBody.setOnClickListener { toggleSelection(itemMoveBody, "Move your body") }
        itemEatHealthy.setOnClickListener { toggleSelection(itemEatHealthy, "Eat healthy") }
        itemGratitude.setOnClickListener { toggleSelection(itemGratitude, "Practice gratitude") }
        itemDigitalBreak.setOnClickListener { toggleSelection(itemDigitalBreak, "Take a digital break") }
        itemSleep.setOnClickListener { toggleSelection(itemSleep, "Get enough sleep") }

        // 🔹 Save button click
        btnHabit.setOnClickListener {
            if (selectedHabits.isEmpty()) {
                Toast.makeText(this, "Please select at least one habit", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            prefs.edit().putStringSet("selectedHabits", selectedHabits).apply()
            Toast.makeText(this, "Habits saved!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    // 🔸 Toggle background when selected/unselected
    private fun toggleSelection(view: LinearLayout, habit: String) {
        if (selectedHabits.contains(habit)) {
            selectedHabits.remove(habit)
            view.setBackgroundResource(R.drawable.bg_habit_unselected)
        } else {
            selectedHabits.add(habit)
            view.setBackgroundResource(R.drawable.bg_habit_selected)
        }
    }

    private fun restoreSelection(view: LinearLayout, habit: String) {
        if (selectedHabits.contains(habit))
            view.setBackgroundResource(R.drawable.bg_habit_selected)
        else
            view.setBackgroundResource(R.drawable.bg_habit_unselected)
    }
}