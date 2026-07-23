package com.example.habbittracker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Switch
import android.widget.Spinner
import android.widget.AdapterView
import androidx.core.content.ContextCompat

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HydrationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HydrationFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var bottleOutline: ImageView
    private lateinit var bottleParts: List<ImageView>
    private lateinit var tvCups: TextView
    private lateinit var btnAdd: Button
    private lateinit var btnRemove: Button
    private lateinit var btnHistory: Button
    //private lateinit var btnSharePdf: Button
    private lateinit var sharedPref: SharedPreferences
    private lateinit var btnReset: Button
    //private lateinit var lvHistory: ListView

    private lateinit var switchReminder: Switch
    private lateinit var spinnerInterval: Spinner

    private val totalCups = 8
    private var currentLevel = 0

    private var isDarkMode: Boolean = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_hydration, container, false)

        switchReminder = view.findViewById(R.id.switchReminder)
        spinnerInterval = view.findViewById(R.id.spinnerInterval)

        val prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val reminderEnabled = prefs.getBoolean("reminder_enabled", false)
        val interval = prefs.getInt("reminder_interval", 2)

        // find views (IDs match the fragment_hydration.xml below)
        bottleOutline = view.findViewById(R.id.bottleOutline)
        tvCups = view.findViewById(R.id.tvCups)
        btnAdd = view.findViewById(R.id.btnAdd)
        btnRemove = view.findViewById(R.id.btnRemove)
        btnHistory = view.findViewById(R.id.btnHistory)
        btnReset = view.findViewById(R.id.btnReset)
        //lvHistory = view.findViewById(R.id.lvHistory)
        //btnSharePdf = view.findViewById(R.id.btnSharePdf) // optional button for PDF



        bottleParts = listOf(
            view.findViewById(R.id.bottle_c1),
            view.findViewById(R.id.bottle_c2),
            view.findViewById(R.id.bottle_c3),
            view.findViewById(R.id.bottle_c4),
            view.findViewById(R.id.bottle_c5),
            view.findViewById(R.id.bottle_c6),
            view.findViewById(R.id.bottle_c7),
            view.findViewById(R.id.bottle_c8)
        )

        sharedPref = requireContext().getSharedPreferences("HydrationPrefs", Context.MODE_PRIVATE)

        // daily reset if needed, load saved level, update UI
        resetIfNewDay()
        loadCurrentLevel()
        updateBottleUI()
        //scheduleHydrationReminder()
        //cancelHydrationReminder()
        //displayHistoryList()

        if (reminderEnabled) {
            scheduleReminder(interval)
        } else {
            cancelReminder()
        }
        // Button actions
        // Button actions
        btnAdd.setOnClickListener { addCup() }
        btnRemove.setOnClickListener { removeCup() }
        btnReset.setOnClickListener { resetToday() }

        // HISTORY button opens the history activity
        btnHistory.setOnClickListener {
            val intent = Intent(requireContext(), HydrationHistoryActivity::class.java)
            startActivity(intent)
        }


        switchReminder.isChecked = reminderEnabled
        spinnerInterval.setSelection(interval - 1)

        switchReminder.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("reminder_enabled", isChecked).apply()
            if (isChecked) {
                scheduleReminder(prefs.getInt("reminder_interval", 2))
                Toast.makeText(context, "Reminder enabled", Toast.LENGTH_SHORT).show()
            } else {
                cancelReminder()
                Toast.makeText(context, "Reminder disabled", Toast.LENGTH_SHORT).show()
            }
        }

        spinnerInterval.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val hours = parent.getItemAtPosition(position).toString().toInt()
                prefs.edit().putInt("reminder_interval", hours).apply()
                if (switchReminder.isChecked) scheduleReminder(hours)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Apply theme after views are initialized. Respect MainActivity state if available.
        isDarkMode = (activity as? MainActivity)?.isDarkMode ?: false
        applyTheme(view, isDarkMode)
        return view
    }

    private fun addCup() {
        if (currentLevel < totalCups) {
            currentLevel++
            saveCurrentLevel()
            saveHistory()
            updateBottleUI()
            //displayHistoryList()

            if (currentLevel == totalCups) {
                // User reached goal, cancel reminder
                cancelReminder()
                Toast.makeText(
                    requireContext(),
                    "Goal reached! Reminder turned off.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(requireContext(), "Goal already completed!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeCup() {
        if (currentLevel > 0) {
            currentLevel--
            saveCurrentLevel()
            saveHistory()
            updateBottleUI()
            //displayHistoryList()
        } else {
            Toast.makeText(requireContext(), "No cups to remove!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetToday() {
        currentLevel = 0
        saveCurrentLevel()
        saveHistory()
        updateBottleUI()// If user removed water and reminders are enabled, restart reminder
        val prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val reminderEnabled = prefs.getBoolean("reminder_enabled", false)
        if (reminderEnabled && currentLevel < totalCups) {
            val interval = prefs.getInt("reminder_interval", 2)
            scheduleReminder(interval)
            Toast.makeText(requireContext(), "Reminder restarted again", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateBottleUI() {
        tvCups.text = "$currentLevel / $totalCups cups"
        // show first N parts (c1..cN). cN is drawn above c(N-1) because of XML order.
        for (i in bottleParts.indices) {
            bottleParts[i].visibility = if (i < currentLevel) View.VISIBLE else View.GONE
        }
    }

    private fun saveCurrentLevel() {
        val editor = sharedPref.edit()
        editor.putInt("currentLevel", currentLevel)
        editor.putString("lastDate", getTodayDate())
        editor.apply()
    }

    private fun loadCurrentLevel() {
        currentLevel = sharedPref.getInt("currentLevel", 0)
    }

    private fun resetIfNewDay() {
        val lastDate = sharedPref.getString("lastDate", "")
        if (lastDate != getTodayDate()) {
            currentLevel = 0
            saveCurrentLevel()

            // Re-enable reminder switch
            switchReminder.isEnabled = true
            if (switchReminder.isChecked) {
                scheduleReminder(sharedPref.getInt("reminder_interval", 2))
            }
        }
    }

    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return sdf.format(Date())
    }

    /**
     * Maintain hydration_history as a JSON array in shared prefs under:
     * Pref name: "HydrationPrefs"
     * Key: "hydration_history"
     * Format: [ { "date":"yyyyMMdd", "level":3 }, ... ] (max 30 items)
     */
    private fun saveHistory() {
        val historyJson = sharedPref.getString("hydration_history", "[]")
        val history = JSONArray(historyJson)

        val today = getTodayDate()
        // remove existing entry for today
        var removedIndex = -1
        for (i in 0 until history.length()) {
            val obj = history.getJSONObject(i)
            if (obj.getString("date") == today) {
                removedIndex = i
                break
            }
        }
        if (removedIndex >= 0) history.remove(removedIndex)

        // append today's object
        val obj = JSONObject()
        obj.put("date", today)
        obj.put("level", currentLevel)
        history.put(obj)

        // keep last 30
        val newHistory = JSONArray()
        val start = if (history.length() > 30) history.length() - 30 else 0
        for (i in start until history.length()) {
            newHistory.put(history.getJSONObject(i))
        }

        sharedPref.edit().putString("hydration_history", newHistory.toString()).apply()
    }

    private fun scheduleReminder(intervalHours: Int) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), HydrationReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val intervalMillis = intervalHours * 30 * 1000L
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + intervalMillis,
            intervalMillis,
            pendingIntent
        )
    }

    private fun cancelReminder() {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), HydrationReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun applyTheme(root: View, dark: Boolean) {
        // Background: black when dark, keep original light background otherwise
        val lightBg = Color.parseColor("#EEF1E2")
        val darkBg = Color.rgb(27,27,27)
        root.setBackgroundColor(if (dark) darkBg else lightBg)

        // Buttons: use logo color with black text (fallback provided)
        val logoColor = try {
            ContextCompat.getColor(requireContext(), R.color.logo_color)
        } catch (e: Exception) {
            Color.parseColor("#606E54")
        }
        val tint = android.content.res.ColorStateList.valueOf(logoColor)
        listOf(btnAdd, btnRemove, btnHistory, btnReset).forEach { btn ->
            btn.backgroundTintList = tint
            btn.setTextColor(Color.BLACK)
        }

        // Text colors readable in dark mode
        tvCups.setTextColor(if (dark) Color.BLACK else Color.parseColor("#222222"))
        switchReminder.setTextColor(if (dark) Color.WHITE else Color.BLACK)

        // Optionally tint bottle parts for contrast (keep images visible)
        val partTint = if (dark) Color.parseColor("#80FFFFFF") else Color.TRANSPARENT
        bottleParts.forEach { part ->
            part.setColorFilter(if (dark) partTint else 0)
        }
    }

    fun setManualBackground(dark: Boolean) {
        isDarkMode = dark
        view?.let { applyTheme(it, dark) }
    }



    /**
     * Render the given bottle view into a PNG in cacheDir and trigger widget update.
     * File name: widget_bottle.png
     */
    private fun saveBottleViewForWidget(bottleView: View, fileName: String = "widget_bottle.png") {
        if (bottleView.width == 0 || bottleView.height == 0) {
            bottleView.post { saveBottleViewForWidget(bottleView, fileName) }
            return
        }

        val bmp = Bitmap.createBitmap(bottleView.width, bottleView.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        try {
            bottleView.draw(canvas)
        } catch (e: Exception) {
            // drawing failed, skip saving
            return
        }

        val outFile = File(requireContext().cacheDir, fileName)
        try {
            FileOutputStream(outFile).use { fos ->
                bmp.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.flush()
            }
        } catch (e: Exception) {
            // ignore write errors
        }

        // Notify widget provider to refresh (widget provider must exist in project)
        try {
            HomeWidgetProvider.updateAllWidgets(requireContext())
        } catch (e: Exception) {
            // ignore if provider class not available at compile/run time
        }
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HydrationFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HydrationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}