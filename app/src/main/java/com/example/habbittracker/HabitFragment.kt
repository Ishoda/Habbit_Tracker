package com.example.habbittracker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HabitFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HabitFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    // keys
    private lateinit var prefs: SharedPreferences
    private lateinit var btnEditPreferences: Button
    private lateinit var btnViewHistory: Button
    private lateinit var btnResetNow: Button
    private val selectedHabits = mutableSetOf<String>()
    private val todayCompletedHabits = mutableSetOf<String>()

    private val originalHabitOrder = mutableListOf<String>()
    private val KEY_HISTORY = "habitHistoryJson" // Added history key
    // Map habit names to their LinearLayout views
    private lateinit var habitMap: Map<String, LinearLayout>

    private lateinit var habitContainer: GridLayout

    private var rootView: View? = null
    private var isDarkMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        rootView =  inflater.inflate(R.layout.fragment_habit, container, false)
        return rootView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)

        btnEditPreferences = view.findViewById(R.id.btnEditPreferences)
        btnViewHistory = view.findViewById(R.id.btnViewHistory)
        btnResetNow = view.findViewById(R.id.btnResetNow)

        // Get the parent container (GridLayout) for reordering habits
        habitContainer = view.findViewById(R.id.gridHabits)

        // Load selected habits from PersonalizeHabit
        val savedHabits = prefs.getStringSet("selectedHabits", emptySet()) ?: emptySet()
        selectedHabits.addAll(savedHabits)

        //Load today's completed habits separately
        val todayProgress = prefs.getStringSet("todayCheckedSet", emptySet()) ?: emptySet()
        todayCompletedHabits.addAll(todayProgress)

        // Map LinearLayouts to habit names (8 habits)
        habitMap = mapOf(
            "Wake up early" to view.findViewById(R.id.itemHabit1),
            "Plan your day" to view.findViewById(R.id.itemHabit2),
            "Mindfulness" to view.findViewById(R.id.itemHabit3),
            "Move your body" to view.findViewById(R.id.itemHabit4),
            "Eat healthy" to view.findViewById(R.id.itemHabit5),
            "Practice gratitude" to view.findViewById(R.id.itemHabit6),
            "Take a digital break" to view.findViewById(R.id.itemHabit7),
            "Get enough sleep" to view.findViewById(R.id.itemHabit8)
        )

        isDarkMode = (activity as? MainActivity)?.isDarkMode ?: false
        setManualBackground(isDarkMode)

        updateHabitVisibility()
        reorderHabits()

        btnEditPreferences.setOnClickListener {
            startActivity(Intent(requireContext(), PersonalizeHabit::class.java))
        }

        btnViewHistory.setOnClickListener {
            startActivity(Intent(requireContext(), HistoryActivity::class.java))
        }

        btnResetNow.setOnClickListener {
            saveCurrentProgressToHistory()
            todayCompletedHabits.clear()
            updateHabitVisibility()
            Toast.makeText(requireContext(), "Reset performed", Toast.LENGTH_SHORT).show()
            saveTodayProgress()
        }

        val tvToday = view.findViewById<TextView>(R.id.tvToday)
        val df = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault())
        tvToday.text = "Today: ${df.format(Date())}"
    }

    fun setManualBackground(dark: Boolean) {
        isDarkMode = dark

        // Light-mode fragment background kept as before
        val bgColorLight = Color.parseColor("#EEF1E2")
        rootView?.setBackgroundColor(if (dark) Color.rgb(27,27,27) else bgColorLight)

        // Buttons should be logo color; fallback provided
        val logoColor = try {
            ContextCompat.getColor(requireContext(), R.color.logo_color)
        } catch (e: Exception) {
            Color.parseColor("#606E54")
        }
        val btnBgColor = logoColor
        val btnTextColor = Color.BLACK

        // When dark mode requested, header color should be black and fragment background should be that header color
        val headerTextColor = if (dark) {
            Color.WHITE
        } else {
            // keep existing light header color
            try {
                Color.parseColor("#417978")
            } catch (e: Exception) {
                Color.parseColor("#417978")
            }
        }

        // Habit item colors: keep current logic (so light-mode habits unchanged)
        //val unselectedColor = if (dark) Color.parseColor("#2B2B2B") else Color.WHITE
        //val selectedColor = if (dark) Color.parseColor("#2E7D32") else Color.parseColor("#E8F5E9")

        // Fragment background: if dark -> use header color (black), else use original bg
        rootView?.setBackgroundColor(if (dark) Color.BLACK else Color.parseColor("#EEF1E2"))

        // Buttons use logo color and black text
        btnEditPreferences.backgroundTintList = ColorStateList.valueOf(btnBgColor)
        btnEditPreferences.setTextColor(btnTextColor)
        btnViewHistory.backgroundTintList = ColorStateList.valueOf(btnBgColor)
        btnViewHistory.setTextColor(btnTextColor)
        btnResetNow.backgroundTintList = ColorStateList.valueOf(btnBgColor)
        btnResetNow.setTextColor(btnTextColor)

        // Header text color (keeps readable contrast)
        rootView?.findViewById<TextView>(R.id.tvToday)?.setTextColor(if (dark) Color.WHITE else headerTextColor)

        habitMap.forEach { (habit, layout) ->
            val isCompleted = todayCompletedHabits.contains(habit)
            //layout.setBackgroundColor(if (isCompleted) selectedColor else unselectedColor)

            for (i in 0 until layout.childCount) {
                val child = layout.getChildAt(i)
                if (child is TextView) {
                    child.setTextColor(if (isDarkMode) Color.BLACK else Color.parseColor("#101010"))
                }
            }

            layout.setOnClickListener {
                toggleHabitSelection(habit, layout)
            }
        }
    }


    // New method to update habit visibility based on selection and completion status
    private fun updateHabitVisibility() {
        habitMap.forEach { (habit, layout) ->
            // First check if habit is selected at all
            if (selectedHabits.contains(habit)) {
                // Always make selected habits visible
                layout.visibility = View.VISIBLE

                // Update background based on completion status
                if (todayCompletedHabits.contains(habit)) {
                    // Highlight completed habits with selected background
                    layout.setBackgroundResource(R.drawable.bg_habit_selected)
                } else {
                    // Use unselected background for incomplete habits
                    layout.setBackgroundResource(R.drawable.bg_habit_unselected)
                }
            } else {
                layout.visibility = View.GONE
            }

            // Set click listener
            layout.setOnClickListener {
                toggleHabitSelection(habit, layout)
            }
        }
    }

    // CHANGED: Toggle habit completion for today, not selection
    private fun toggleHabitSelection(habit: String, layout: LinearLayout) {
        if (todayCompletedHabits.contains(habit)) {
            todayCompletedHabits.remove(habit)
            layout.setBackgroundResource(R.drawable.bg_habit_unselected)
        } else {
            todayCompletedHabits.add(habit)
            layout.setBackgroundResource(R.drawable.bg_habit_selected)
        }
        saveTodayProgress()
        // NEW: Reorder habits after toggling completion status
        reorderHabits()
    }


    // NEW: Method to reorder habits so completed ones appear at the bottom
    private fun reorderHabits() {
        // Get all selected habits (both completed and uncompleted)
        val allSelectedHabits = selectedHabits.toList()

        // Separate into completed and uncompleted lists
        val uncompleted = allSelectedHabits.filter { !todayCompletedHabits.contains(it) }
        val completed = allSelectedHabits.filter { todayCompletedHabits.contains(it) }

        // Create the ordered list (uncompleted first, then completed)
        val orderedHabits = uncompleted + completed

        // Remove all views from parent container first
        for ((_, layout) in habitMap) {
            val parent = layout.parent as? ViewGroup
            parent?.removeView(layout)
        }

        // Add back in the right order
        for (habit in orderedHabits) {
            val layout = habitMap[habit] ?: continue
            if (layout.visibility == View.VISIBLE) {
                habitContainer.addView(layout)
            }
        }
    }
    // Save today's completed habits with a different key
    private fun saveTodayProgress() {
        prefs.edit().putStringSet("todayCheckedSet", todayCompletedHabits).apply()
    }

    private fun saveCurrentProgressToHistory() {
        if (todayCompletedHabits.isEmpty()) return

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayKey = sdf.format(Date())

        // Load existing history
        val historyJson = prefs.getString(KEY_HISTORY, "{}")
        val mapType = object : TypeToken<MutableMap<String, List<String>>>() {}.type
        val historyMap: MutableMap<String, List<String>> = try {
            Gson().fromJson(historyJson, mapType) ?: mutableMapOf()
        } catch (e: Exception) {
            mutableMapOf()
        }

        // Save today's completed habits
        historyMap[todayKey] = todayCompletedHabits.toList()

        // Update history in preferences
        prefs.edit().putString(KEY_HISTORY, Gson().toJson(historyMap)).apply()
    }

    // CHANGED: Update the selected habits when returning from PersonalizeHabit
    override fun onResume() {
        super.onResume()
        // Reload selected habits in case they changed
        val savedHabits = prefs.getStringSet("selectedHabits", emptySet()) ?: emptySet()
        selectedHabits.clear()
        selectedHabits.addAll(savedHabits)

        // Reload today's completed habits
        val todayProgress = prefs.getStringSet("todayCheckedSet", emptySet()) ?: emptySet()
        todayCompletedHabits.clear()
        todayCompletedHabits.addAll(todayProgress)

        // Re-apply theme in case it changed
        isDarkMode = (activity as? MainActivity)?.isDarkMode ?: false
        setManualBackground(isDarkMode)

        // Update visibility based on current state
        updateHabitVisibility()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HabitFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HabitFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}