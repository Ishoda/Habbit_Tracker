package com.example.habbittracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Onboarding1 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_onboarding1)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //navigate to 2nd onboarding screen and skip onboarding screens
        val btnNext = findViewById< Button>(R.id.buttonNext1)
        val btnSkip = findViewById<Button>(R.id.buttonSkip1)

        // Next → screen 2
        btnNext.setOnClickListener {
            startActivity(Intent(this, Onboarding2::class.java))
            finish()

        }

        // Skip → directly to screen 3
        btnSkip.setOnClickListener {
            val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
            prefs.edit().putBoolean("firstTime", false).apply()  // save first-time flag
            val intent = Intent(this, Signup::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}