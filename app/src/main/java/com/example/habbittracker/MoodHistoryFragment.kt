package com.example.habbittracker

import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MoodHistoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MoodHistoryFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var rvMoodHistory: RecyclerView
    private lateinit var tvSelectedMood: TextView
    private lateinit var btnExportPDF: Button

    private val moodImages = arrayOf(
        R.drawable.energized,
        R.drawable.motivated,
        R.drawable.calm,
        R.drawable.hopeful,
        R.drawable.grateful
    )

    private val moodList = mutableListOf<MoodEntry>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_mood_history, container, false)
        // Highlighted: Initialize calendar view & text
        tvSelectedMood = view.findViewById(R.id.tvSelectedMood)
        btnExportPDF = view.findViewById(R.id.btnExportPDF)
        rvMoodHistory = view.findViewById(R.id.rvMoodHistory)

        loadMoodHistory()
        rvMoodHistory.layoutManager = LinearLayoutManager(requireContext())
        rvMoodHistory.adapter = MoodHistoryAdapter(moodList, moodImages)

        btnExportPDF.setOnClickListener {
            exportMoodHistoryAsPDF()
        }

        return view
    }

    private fun loadMoodHistory() {
        val prefs = requireContext().getSharedPreferences("MoodJournal", 0)
        val calendar = Calendar.getInstance()
        moodList.clear()

        for (i in 0..29) {
            val dateKey = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(calendar.time)
            if (prefs.contains("mood_$dateKey")) {
                val moodIndex = prefs.getInt("mood_$dateKey", 0)
                val note = prefs.getString("note_$dateKey", "") ?: ""
                moodList.add(MoodEntry(dateKey, moodIndex, note))
            }
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        if (moodList.isEmpty()) {
            tvSelectedMood.text = "No past moods recorded."
        } else {
            tvSelectedMood.text = "Past Moods"
        }
    }

    private fun exportMoodHistoryAsPDF() {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)

        val canvas = page.canvas
        var y = 50
        val paint = android.graphics.Paint()
        paint.textSize = 14f

        canvas.drawText("Mood History", 50f, y.toFloat(), paint)
        y += 30

        moodList.forEach {
            canvas.drawText("${it.date} - Mood: ${it.moodIndex + 1}, Note: ${it.note}", 50f, y.toFloat(), paint)
            y += 25
        }

        document.finishPage(page)

        try {
            val dir = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "MoodReports")
            if (!dir.exists()) dir.mkdirs()

            val file = File(dir, "Mood_History_${System.currentTimeMillis()}.pdf")
            document.writeTo(FileOutputStream(file))
            document.close()

            sharePDF(file)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sharePDF(file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(shareIntent, "Share or Print PDF"))
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MoodHistoryFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MoodHistoryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}