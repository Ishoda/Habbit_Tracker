package com.example.habbittracker

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Spinner

class PersonalizeName : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_personalize_name)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val nameText = findViewById<EditText>(R.id.textName)
        val genderSpinner = findViewById<Spinner>(R.id.spinnerGender)
        val btnName = findViewById<Button>(R.id.btnName)


        // Load spinner options
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.gender_options,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = adapter


        btnName.setOnClickListener {
            val name = nameText.text.toString()
            val gender = genderSpinner.selectedItem.toString()

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
            }
            else {
                // Save name
                val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
                prefs.edit()
                    .putString("userName", name)
                    .putString("gender", gender)
                    .apply()
                // Move to habit selection
                startActivity(Intent(this, PersonalizeHabit::class.java))
                finish()
            }
        }
    }
}