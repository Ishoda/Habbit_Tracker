package com.example.habbittracker

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import kotlin.rem
import kotlin.text.get
import kotlin.text.toInt
import kotlin.times


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var gridHabits: GridLayout
    private lateinit var tvCongrats: TextView
    private lateinit var hydrationProgressBar: CircularProgressBar
    private lateinit var tvHydrationPercent: TextView
    private lateinit var tvHydrationMessage: TextView
    private lateinit var barChart: BarChart

    private val habitIcons = arrayOf(
        R.drawable.c_wakeup,
        R.drawable.c_daily_plan,
        R.drawable.c_mindful,
        R.drawable.c_excercise,
        R.drawable.c_eat_healthy,
        R.drawable.c_gratitude,
        R.drawable.c_digital_break,
        R.drawable.c_enough_sleep
    )

    //
    private val moodImages = arrayOf(
        R.drawable.energized,
        R.drawable.motivated,
        R.drawable.calm,
        R.drawable.hopeful,
        R.drawable.grateful
    )
    private var rootView: View? = null
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
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        rootView = view

        // Set initial background color based on MainActivity's theme
        val isDarkMode = (activity as? MainActivity)?.let { it.isDarkMode } ?: false
        setManualBackground(isDarkMode)



        // Initialize views
        gridHabits = view.findViewById(R.id.gridHabits)
        tvCongrats = view.findViewById(R.id.tvCongrats)
        hydrationProgressBar = view.findViewById(R.id.hydrationProgressBar)
        tvHydrationPercent = view.findViewById(R.id.tvHydrationPercent)
        tvHydrationMessage = view.findViewById(R.id.tvHydrationMessage)
        barChart = view.findViewById(R.id.barChart)

        displayTodoHabits()
        setupHydrationProgress()
        setupMoodChart()

        return view
    }

    // Call this from MainActivity to update background
    fun setManualBackground(dark: Boolean) {
        rootView?.setBackgroundColor(
            if (dark) Color.rgb(27,27,27) else Color.parseColor("#EEF1E2")
        )
    }

    private fun displayTodoHabits() {
        gridHabits.removeAllViews()

        val prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val allHabits = prefs.getStringSet(
            "selectedHabits", setOf(
                "Wake up early",
                "Move your body",
                "Mindfulness",
                "Plan your day",
                "Eat healthy",
                "Practice gratitude",
                "Digital detox",
                "Sleep 8 hours"
            )
        )!!.toList()

        val doneHabits = prefs.getStringSet("todayCheckedSet", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        val todoHabits = allHabits.filter { !doneHabits.contains(it) }

        if (todoHabits.isEmpty()) {
            tvCongrats.visibility = View.VISIBLE
            gridHabits.visibility = View.GONE
            return
        } else {
            tvCongrats.visibility = View.GONE
            gridHabits.visibility = View.VISIBLE
        }

        // Responsive image size: 28% of screen width
        val displayMetrics = resources.displayMetrics
        val imageSize = (displayMetrics.widthPixels * 0.23).toInt()
        val cornerRadius = (imageSize * 0.25f)

        todoHabits.forEachIndexed { index, habit ->
            // Container for icon + label
            val container = LinearLayout(requireContext())
            container.orientation = LinearLayout.VERTICAL
            container.gravity = Gravity.CENTER
            val paramsContainer = GridLayout.LayoutParams().apply {
                width = GridLayout.LayoutParams.WRAP_CONTENT
                height = GridLayout.LayoutParams.WRAP_CONTENT
                setMargins(16, 16, 16, 16)
            }
            container.layoutParams = paramsContainer


            // Habit icon
            val imageView = ImageView(requireContext())
            val paramsImage = LinearLayout.LayoutParams(imageSize, imageSize)
            paramsImage.gravity = Gravity.CENTER
            imageView.layoutParams = paramsImage
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.setImageResource(habitIcons[index % habitIcons.size])

            // Fade if done
            imageView.alpha = 1.0f

            // 3D look: elevation (shadow)
            imageView.elevation = 16f
            // Rounded corners using GradientDrawable
            val drawable = android.graphics.drawable.GradientDrawable()
            drawable.cornerRadius = cornerRadius
            drawable.setColor(android.graphics.Color.WHITE)
            imageView.background = drawable
            imageView.clipToOutline = true

            // Habit label
            val textView = TextView(requireContext())
            textView.text = habit
            textView.textSize = 12f
            textView.setTextColor(Color.GRAY)
            textView.highlightColor
            textView.gravity = Gravity.CENTER


            // Click toggles done/not done
            container.setOnClickListener {
                doneHabits.add(habit)
                prefs.edit().putStringSet("todayCheckedSet", doneHabits).apply()
                displayTodoHabits() // Refresh the view after marking as done
            }

            container.addView(imageView)
            container.addView(textView)
            gridHabits.addView(container)
        }
    }

    private fun setupHydrationProgress() {
        val prefs = requireContext().getSharedPreferences("HydrationPrefs", Context.MODE_PRIVATE)
        val currentLevel = prefs.getInt("currentLevel", 0)
        val totalCups = 8
        val percentage = (currentLevel * 100) / totalCups

        hydrationProgressBar.setProgressWithAnimation(percentage.toFloat(), 1000)
        tvHydrationPercent.text = "$percentage%"

        tvHydrationMessage.text = when {
            percentage == 100 -> "Great job! You’ve hit your goal!"
            percentage >= 50 -> "You’re halfway there! Keep going!"
            else -> "Keep drinking water!"
        }
    }

    private fun setupMoodChart() {
        val prefs = requireContext().getSharedPreferences("MoodJournal", 0)
        val moodCounts = IntArray(moodImages.size) { 0 }

        val calendar = java.util.Calendar.getInstance()
        for (i in 0..6) {
            val dateKey = java.text.SimpleDateFormat("yyyyMMdd").format(calendar.time)
            if (prefs.contains("mood_$dateKey")) {
                val mood = prefs.getInt("mood_$dateKey", 0)
                if (mood in 0 until moodImages.size) moodCounts[mood]++
            }
            calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
        }

        val entries = moodCounts.mapIndexed { index, count -> BarEntry(index.toFloat(), count.toFloat()) }
        val dataSet = BarDataSet(entries, "Mood Counts").apply {
            colors = listOf(
                Color.parseColor("#2A4E17"),
                Color.parseColor("#396929"),
                Color.parseColor("#49843E"),
                Color.parseColor("#5D9F59"),
                Color.parseColor("#78B97B")
            )
            valueTextColor = Color.BLACK
            valueTextSize = 14f
        }

        barChart.data = BarData(dataSet)
        barChart.description.isEnabled = false
        barChart.setFitBars(true)
        barChart.animateY(1000)

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.labelCount = moodImages.size
        xAxis.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
            override fun getFormattedValue(value: Float): String = ""
        }

        barChart.axisRight.isEnabled = false
        barChart.axisLeft.axisMinimum = 0f

        // Set custom marker
        val marker = MoodMarkerView(requireContext(), R.layout.marker_mood, moodImages)
        barChart.marker = marker
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}