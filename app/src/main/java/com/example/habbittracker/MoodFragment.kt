package com.example.habbittracker

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import java.text.SimpleDateFormat
import java.util.*

class MoodFragment : Fragment() {

    private lateinit var moodViewPager: ViewPager2
    private lateinit var moodQuote: TextView
    private lateinit var moodNote: EditText
    private lateinit var saveButton: Button
    private lateinit var historyButton: Button

    private val moodImages = arrayOf(
        R.drawable.energized,
        R.drawable.motivated,
        R.drawable.calm,
        R.drawable.hopeful,
        R.drawable.grateful
    )

    private val moodQuotes = arrayOf(
        "You have the power to make today amazing",
        "Small steps every day lead to big changes.",
        "Breathe in peace, breathe out stress.",
        "Hope is the heartbeat of the soul.",
        "Count your blessings, not your worries."
    )

    private var moodIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_mood, container, false)

        moodViewPager = view.findViewById(R.id.moodViewPager)
        moodQuote = view.findViewById(R.id.moodQuote)
        moodNote = view.findViewById(R.id.moodNote)
        saveButton = view.findViewById(R.id.saveMood)
        historyButton = view.findViewById(R.id.btnMoodHistory)

        // Adapter
        moodViewPager.adapter = MoodAdapter(moodImages)

        // Determine dark mode from MainActivity (fallback false)
        val isDarkMode = (activity as? MainActivity)?.isDarkMode ?: false

        applyDarkMode(view, isDarkMode)

        // On swipe, update quote and note
        moodViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                moodIndex = position
                moodQuote.text = moodQuotes[position]

                // Load saved note
                val prefs = requireContext().getSharedPreferences("MoodJournal", Context.MODE_PRIVATE)
                val dateKey = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                moodNote.setText(prefs.getString("note_$dateKey", ""))
            }
        })


        // Initialize first quote
        moodQuote.text = moodQuotes[moodIndex]

        // Save button
        saveButton.setOnClickListener {
            val prefs = requireContext().getSharedPreferences("MoodJournal", Context.MODE_PRIVATE)
            val editor = prefs.edit()
            val dateKey = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            editor.putInt("mood_$dateKey", moodIndex)
            editor.putString("note_$dateKey", moodNote.text.toString())
            editor.apply()
            Toast.makeText(requireContext(), "Mood saved!", Toast.LENGTH_SHORT).show()
        }



        val historyButton: Button = view.findViewById(R.id.btnMoodHistory)
        historyButton.setOnClickListener {
            // Navigate to MoodHistoryFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MoodHistoryFragment())
                .addToBackStack(null) // allows back navigation
                .commit()
        }

        return view
    }

    private fun applyDarkMode(root: View, dark: Boolean) {
        // Fragment background: black when dark, original when light
        val lightBg = Color.parseColor("#EEF1E2")
        val darkBg = Color.BLACK
        root.setBackgroundColor(if (dark) darkBg else lightBg)

        // Buttons: use logo color and black text
        val logoColor = try {
            ContextCompat.getColor(requireContext(), R.color.logo_color)
        } catch (e: Exception) {
            Color.parseColor("#606E54")
        }
        val btnTint = ColorStateList.valueOf(logoColor)
        saveButton.backgroundTintList = btnTint
        saveButton.setTextColor(Color.BLACK)
        historyButton.backgroundTintList = btnTint
        historyButton.setTextColor(Color.BLACK)

        // Mood quote and note text colors
        if (dark) {
            // keep quote color readable (use existing warm accent if desired, or white)
            moodQuote.setTextColor(Color.parseColor("#FFB86B"))
            moodNote.setTextColor(Color.WHITE)
            // card backgrounds left as in layout; ensure EditText hint remains visible
            moodNote.setHintTextColor(Color.parseColor("#BDBDBD"))
        } else {
            moodQuote.setTextColor(Color.parseColor("#FFB86B"))
            moodNote.setTextColor(Color.parseColor("#222222"))
            moodNote.setHintTextColor(Color.parseColor("#888888"))
        }
    }
    fun setManualBackground(dark: Boolean) {
        view?.let { applyDarkMode(it, dark) }
    }
}

