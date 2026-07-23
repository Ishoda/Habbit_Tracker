package com.example.habbittracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import android.widget.EditText
import android.widget.Button
import android.widget.Toast

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.Password)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvSignup = findViewById<TextView>(R.id.tvSignup)

        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)

        btnLogin.setOnClickListener {
            val emailText = email.text.toString()
            val pwdText = password.text.toString()

            val savedEmail = prefs.getString("email", "")
            val savedPassword = prefs.getString("password", "")

            if (emailText == savedEmail && pwdText == savedPassword) {
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()

                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }

        // Go to signup if new user
        tvSignup.setOnClickListener {
            startActivity(Intent(this, Signup::class.java))
            finish()
        }
    }
}