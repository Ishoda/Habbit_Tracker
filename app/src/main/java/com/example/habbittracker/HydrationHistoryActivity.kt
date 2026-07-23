package com.example.habbittracker

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream


class HydrationHistoryActivity : AppCompatActivity() {

    private lateinit var lvHistory: ListView
    private lateinit var btnSharePdf: Button
    private val PREFS_NAME = "HydrationPrefs"
    private val KEY_HISTORY = "hydration_history"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContentView(R.layout.activity_hydration_history)
        //ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
        //    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        //    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
        //    insets
        //}
        lvHistory = findViewById(R.id.lvHistory)
        btnSharePdf = findViewById(R.id.btnSharePdf)

        displayHistory()

        btnSharePdf.setOnClickListener {
            generateAndSharePdf()
        }
    }

    private fun displayHistory() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val json = prefs.getString(KEY_HISTORY, "[]") ?: "[]"
        val arr = JSONArray(json)

        if (arr.length() == 0) {
            Toast.makeText(this, "No history available", Toast.LENGTH_SHORT).show()
            lvHistory.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listOf("No history"))
            return
        }

        val list = mutableListOf<String>()
        for (i in arr.length() - 1 downTo 0) {
            val obj = arr.getJSONObject(i)
            val date = obj.getString("date")
            val level = obj.getInt("level")
            list.add("$date  →  $level / 8")
        }
        lvHistory.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
    }

    private fun generateAndSharePdf() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val json = prefs.getString(KEY_HISTORY, "[]") ?: "[]"
        val arr = JSONArray(json)

        if (arr.length() == 0) {
            Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show()
            return
        }

        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = doc.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()
        paint.textSize = 14f

        var y = 50f
        paint.isFakeBoldText = true
        paint.textSize = 18f
        canvas.drawText("Hydration History", 40f, y, paint)
        y += 30f
        paint.isFakeBoldText = false
        paint.textSize = 12f

        // Write from newest to oldest
        for (i in arr.length() - 1 downTo 0) {
            val o = arr.getJSONObject(i)
            val date = o.getString("date")
            val level = o.getInt("level")
            canvas.drawText("$date → $level / 8", 40f, y, paint)
            y += 20f
            if (y > 800f) {
                doc.finishPage(page)
                page = doc.startPage(pageInfo)
                y = 50f
            }
        }
        doc.finishPage(page)

        // Save to cache (allowed by file_paths cache-path)
        val file = File(cacheDir, "hydration_history.pdf")
        try {
            doc.writeTo(FileOutputStream(file))
            doc.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error generating PDF", Toast.LENGTH_SHORT).show()
            return
        }

        val uri: Uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        val share = Intent(Intent.ACTION_SEND)
        share.type = "application/pdf"
        share.putExtra(Intent.EXTRA_STREAM, uri)
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(share, "Share PDF"))
    }
}