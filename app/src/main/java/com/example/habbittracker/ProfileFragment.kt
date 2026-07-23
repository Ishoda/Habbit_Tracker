package com.example.habbittracker

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import java.text.SimpleDateFormat
import java.util.*
import kotlin.sequences.forEach

class ProfileFragment : Fragment() {

    private lateinit var tvUserName: TextView
    private lateinit var tvMemberSince: TextView
    private lateinit var tvTotalHabits: TextView
    private lateinit var tvCurrentStreak: TextView
    private lateinit var ivProfilePicture: ImageView

    private lateinit var layoutEditProfile: LinearLayout
    private lateinit var layoutEditPreferences: LinearLayout
    private lateinit var layoutTheme: LinearLayout
    private lateinit var layoutHistory: LinearLayout
    private lateinit var layoutLogout: LinearLayout

    private var isDarkMode: Boolean = false
    private var rootView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        // Set initial background color based on MainActivity's theme
        val isDarkMode = (activity as? MainActivity)?.let { it.isDarkMode } ?: false
        setManualBackground(isDarkMode)

        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        rootView = view

        initViews(view)
        loadUserData()
        setupClickListeners()

        return view
    }


    fun setManualBackground(darkMode: Boolean) {
        rootView?.setBackgroundColor(

            if (darkMode) Color.rgb(27,27,27) else Color.parseColor("#EEF1E2")
        )
    }
    private fun initViews(view: View) {
        tvUserName = view.findViewById(R.id.tvUserName)
        tvMemberSince = view.findViewById(R.id.tvMemberSince)
        tvTotalHabits = view.findViewById(R.id.tvTotalHabits)
        tvCurrentStreak = view.findViewById(R.id.tvCurrentStreak)
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture)

        layoutEditProfile = view.findViewById(R.id.layoutEditProfile)
        layoutEditPreferences = view.findViewById(R.id.layoutEditPreferences)
        layoutTheme = view.findViewById(R.id.layoutTheme)
        layoutHistory = view.findViewById(R.id.layoutHistory)
        layoutLogout = view.findViewById(R.id.layoutLogout)
    }

    private fun loadUserData() {
        val prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)

        // Load user name
        val userName = prefs.getString("userName", "User")
        tvUserName.text = userName

        // Load member since date
        val memberSince = prefs.getLong("memberSince", System.currentTimeMillis())
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        tvMemberSince.text = "Member since ${dateFormat.format(Date(memberSince))}"

        // Load total habits
        val selectedHabits = prefs.getStringSet("selectedHabits", emptySet())
        tvTotalHabits.text = selectedHabits?.size.toString()

        // Calculate current streak (simplified - you can enhance this)
        val currentStreak = calculateStreak()
        tvCurrentStreak.text = currentStreak.toString()
    }

    private fun calculateStreak(): Int {
        val prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)

        // Get all completion dates and calculate streak
        // This is a simplified version - you can enhance it based on your data structure
        val completionDates = prefs.getStringSet("completionDates", emptySet()) ?: emptySet()

        // For now, return a sample streak
        return completionDates.size % 30 // Simplified calculation
    }

    private fun setupClickListeners() {
        layoutEditProfile.setOnClickListener {
            // Navigate to PersonalizeNameActivity to edit profile
            val intent = Intent(requireContext(), PersonalizeName::class.java)
            startActivity(intent)
        }

        layoutEditPreferences.setOnClickListener {
            // Navigate to PersonalizeHabitActivity to edit preferences
            val intent = Intent(requireContext(), PersonalizeHabit::class.java)
            startActivity(intent)
        }

        layoutTheme.setOnClickListener {
            // Toggle theme or show theme selection dialog
            toggleTheme()
        }

        layoutHistory.setOnClickListener {
            // Navigate to HistoryActivity
            val intent = Intent(requireContext(), HistoryActivity::class.java)
            startActivity(intent)
        }

        layoutLogout.setOnClickListener {
            // Handle logout
            logout()
        }
    }

    private fun toggleTheme() {
        val prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("isDarkMode", false)

        // Toggle theme preference
        prefs.edit().putBoolean("isDarkMode", !isDarkMode).apply()

        // You can implement theme switching logic here
        // For now, just restart the activity to apply changes
        requireActivity().recreate()
    }

    private fun logout() {
        // Clear user preferences
        val prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        // Navigate back to login screen
        val intent = Intent(requireContext(), Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onResume() {
        super.onResume()
        // Refresh user data when returning to this fragment
        loadUserData()
    }

    private fun applyTheme(root: View, dark: Boolean) {
        val lightBg = Color.parseColor("#EEF1E2")
        val darkBg = Color.rgb(27,27,27)
        root.setBackgroundColor(if (dark) darkBg else lightBg)
/*
        val textPrimary = if (dark) Color.WHITE else Color.parseColor("#101010")
        val textSecondary = if (dark) Color.parseColor("#DDDDDD") else Color.parseColor("#666666")

        tvUserName.setTextColor(textPrimary)
        tvMemberSince.setTextColor(textSecondary)
        tvTotalHabits.setTextColor(textPrimary)
        tvCurrentStreak.setTextColor(textPrimary)

        // Slight tint for profile image in dark mode to better integrate with dark background (no-op if image is full-color)
        if (dark) {
            ivProfilePicture.alpha = 0.95f
        } else {
            ivProfilePicture.alpha = 1.0f
        }

        // Optionally adjust background of clickable rows for contrast if desired
        val rowBg = if (dark) Color.parseColor("#121212") else Color.parseColor("#121212")
        listOf(layoutEditProfile, layoutEditPreferences, layoutTheme, layoutHistory, layoutLogout).forEach { row ->
            row.setBackgroundColor(rowBg)
            // Ensure all TextView children in the row get updated color so labels are visible
            /*row.children.forEach { child ->
                if (child is TextView) {
                    child.setTextColor(textPrimary)
                }
            }*/
        }
    }

    // Exposed so MainActivity can call setManualBackground(...) on fragments uniformly.
    fun setManualBackground(dark: Boolean) {
        isDarkMode = dark
        view?.let { applyTheme(it, dark) }
    }*/

    }
}

