package com.example.habbittracker

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.os.Handler
import android.os.Looper
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var tvGreeting: TextView
    private lateinit var tvDateTime: TextView
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var updateTimeRunnable: Runnable
    private lateinit var themeToggle: ImageView
    var isDarkMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        tvGreeting = findViewById(R.id.tvGreeting)
        tvDateTime = findViewById(R.id.tvDateTime)
        themeToggle = findViewById(R.id.themeToggle)

        // Load user's name from SharedPreferences
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        val userName = prefs.getString("userName", "User")

        // Set greeting with name
        setGreeting(userName)

        // Start updating date and time every minute
        startUpdatingDateTime()

        // Theme toggle click
        themeToggle.setOnClickListener {
            isDarkMode = !isDarkMode
            applyTheme(isDarkMode)
            prefs.edit().putBoolean("darkMode", isDarkMode).apply()
        }

        // Logout functionality
        btnLogout.setOnClickListener {
            prefs.edit().clear().apply() // removes all saved data
            startActivity(Intent(this, Login::class.java))
            finish()
        }

        // Default fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, HomeFragment())
            .commit()

        // Bottom navigation handling
        bottomNav.setOnItemSelectedListener { item ->
            val selectedFragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                //R.id.logo -> HomeFragment()
                R.id.nav_mood -> MoodFragment()
                R.id.nav_hydration -> HydrationFragment()
                R.id.nav_profile -> ProfileFragment()
                R.id.nav_habits -> HabitFragment()
                //R.id.profileIcon -> ProfileFragment()

                else -> HomeFragment()
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, selectedFragment)
                .commit()

            true
        }
    }

    private fun setGreeting(userName: String?) {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 0..11 -> "Good Morning"
            in 12..17 -> "Good Afternoon"
            else -> "Good Evening"
        }
        tvGreeting.text = "$greeting, $userName!"
    }

    private fun startUpdatingDateTime() {
        updateTimeRunnable = object : Runnable {
            override fun run() {
                val dateFormat = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
                val dateTime = dateFormat.format(Date())
                tvDateTime.text = dateTime
                handler.postDelayed(this, 60000) // update every minute
            }
        }
        handler.post(updateTimeRunnable) // start immediately
    }

    private fun applyTheme(dark: Boolean) {
        val header = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.headerLayout)
        val navView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)

        if (dark) {
            header.setBackgroundColor(getColor(R.color.black))
            //tvGreeting.setTextColor(getColor(R.color.white))
            tvDateTime.setTextColor(getColor(R.color.white))
            themeToggle.setImageResource(R.drawable.baseline_light_mode_24)

            navView.setBackgroundColor(getColor(R.color.black))
            navView.itemIconTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.whitish  ))
            navView.itemTextColor = android.content.res.ColorStateList.valueOf(getColor(R.color.white))
        } else {
            header.setBackgroundColor(getColor(R.color.whitish))
            //tvGreeting.setTextColor(getColor(R.color.black))
            tvDateTime.setTextColor(getColor(R.color.black))
            themeToggle.setImageResource(R.drawable.baseline_dark_mode_24)

            navView.setBackgroundColor(getColor(R.color.whitish))
            navView.itemIconTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.black))
            navView.itemTextColor = android.content.res.ColorStateList.valueOf(getColor(R.color.black))
        }

        // Update HomeFragment background color
        if (fragment is HomeFragment) {
            fragment.setManualBackground(dark)
        }else if (fragment is HabitFragment) {
            fragment.setManualBackground(dark)
        }else if (fragment is MoodFragment) {
            fragment.setManualBackground(dark)
        }else if (fragment is HydrationFragment) {
            fragment.setManualBackground(dark)
        }else if (fragment is ProfileFragment) {
            fragment.setManualBackground(dark)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateTimeRunnable) // stop updates when activity destroyed
    }
}