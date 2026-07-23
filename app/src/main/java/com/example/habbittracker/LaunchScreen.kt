package com.example.habbittracker

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LaunchScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_launch_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //check if first time user
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        val firstTime = prefs.getBoolean("firstTime", true)
        val rememberMe = prefs.getBoolean("rememberMe", false)


        Handler(Looper.getMainLooper()).postDelayed({
            if (firstTime) {
                startActivity(Intent(this, Onboarding1::class.java))
            } else if (rememberMe) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, Login::class.java))
            }

            finish()
        },3000)
    }
}