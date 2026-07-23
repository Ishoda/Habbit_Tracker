package com.example.habbittracker

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.*

class Signup : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val btnSignup = findViewById<Button>(R.id.btnSignup)
        val email = findViewById<EditText>(R.id.email)
        val pwd = findViewById<EditText>(R.id.Password)
        val confirmPwd = findViewById<EditText>(R.id.ConfirmPassword)
        val rememberMeCheck = findViewById<CheckBox>(R.id.checkRememberMe)
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        val tvLogin = findViewById<TextView>(R.id.tvlogin)


        // If already logged in & rememberMe true → go directly to MainActivity
        if (prefs.getBoolean("rememberMe", false)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        btnSignup.setOnClickListener {
            val emailText = email.text.toString()
            val password = pwd.text.toString()
            val confirmPassword = confirmPwd.text.toString()


            if (emailText.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {//check if there are empty fields
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }else if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {// Check if email is valid
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            } else if (password == confirmPassword) {//password should match to confirm password
                Toast.makeText(this, "Signup successful", Toast.LENGTH_SHORT).show()

                // Save login state
                prefs.edit()
                    .putString("email", emailText)
                    .putString("password", password)
                    .putBoolean("firstTime", false)
                    .putBoolean("rememberMe", rememberMeCheck.isChecked)
                    .putString("password", password)

                    .apply()
                Toast.makeText(this, "Signup successful", Toast.LENGTH_SHORT).show()

                // Navigate to MainActivity
                startActivity(Intent(this, PersonalizeName::class.java))
                finish()
            } else {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
        }
        // Go to login if already have account
        tvLogin.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
            finish()
        }
    }
}