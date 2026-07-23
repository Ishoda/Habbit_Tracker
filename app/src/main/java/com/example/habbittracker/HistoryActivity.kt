package com.example.habbittracker

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.graphics.Color
import android.os.Environment
import android.widget.Button
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import kotlin.compareTo
import kotlin.div
import kotlin.printStackTrace
import kotlin.text.toInt
import kotlin.times

class HistoryActivity : AppCompatActivity() {

    private val KEY_HISTORY = "habitHistoryJson"
    private val gson = Gson()
    private lateinit var btnSharePdf: Button
    private lateinit var lvDates: ListView

    private val PREFS_NAME = "prefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContentView(R.layout.activity_history)
        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }*/


        lvDates = findViewById<ListView>(R.id.lvDates)
        btnSharePdf = findViewById(R.id.btnSharePdf)

        displayHistory()

        btnSharePdf.setOnClickListener {
            generateAndSharePdf()
        }
    }

    private fun displayHistory() {
        val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val historyJson = prefs.getString(KEY_HISTORY, "{}") ?: "{}"
        val mapType = object : TypeToken<Map<String, List<String>>>() {}.type
        val historyMap: Map<String, List<String>> =
            gson.fromJson(historyJson, mapType) ?: emptyMap()

        if (historyMap.isEmpty()) {
            Toast.makeText(this, "No history found", Toast.LENGTH_SHORT).show()
            return
        }

        val dates = historyMap.keys.sortedDescending()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, dates)
        lvDates.adapter = adapter

        lvDates.setOnItemClickListener { _, _, position, _ ->
            val date = dates[position]
            val items = historyMap[date] ?: emptyList()
            val message = if (items.isEmpty()) "No completed habits for $date"
            else items.joinToString("\n") { "- $it" }
            AlertDialog.Builder(this)
                .setTitle("Progress for $date")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun generateAndSharePdf() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val historyJson = prefs.getString(KEY_HISTORY, "{}") ?: "{}"
        val mapType = object : com.google.gson.reflect.TypeToken<Map<String, List<String>>>() {}.type
        val historyMap: Map<String, List<String>> =
            gson.fromJson(historyJson, mapType) ?: emptyMap()

        if (historyMap.isEmpty()) {
            Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show()
            return
        }

        val pageWidth = 595
        val pageHeight = 842
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        var page = doc.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()
        val borderPaint = Paint().apply {
            color = android.graphics.Color.rgb(71, 95, 71)
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }

        // Draw border
        canvas.drawRect(
            10f,
            10f,
            (pageWidth - 10).toFloat(),
            (pageHeight - 10).toFloat(),
            borderPaint
        )

        // Draw centered logo
        val logo = BitmapFactory.decodeResource(resources, R.drawable.ic_logo)
        val maxLogoWidth = 180 // Optional: set a max width if needed
        val logoWidth = if (logo.width > maxLogoWidth) maxLogoWidth else logo.width
        val logoHeight = (logo.height * (logoWidth.toFloat() / logo.width)).toInt()
        val scaledLogo = Bitmap.createScaledBitmap(logo, logoWidth, logoHeight, true)
        val logoX = ((pageWidth - logoWidth) / 2f)
        val logoY = 30f
        canvas.drawBitmap(scaledLogo, logoX, logoY, null)

        // Draw centered app name below logo
        paint.textSize = 28f
        paint.isFakeBoldText = true
        paint.color = android.graphics.Color.rgb(71, 95, 71)
        val appName = "happyMe"
        val appNameWidth = paint.measureText(appName)
        val appNameX = (pageWidth - appNameWidth) / 2f
        val appNameY = logoY + logoHeight + 36f
        canvas.drawText(appName, appNameX, appNameY, paint)

        // Draw title below app name
        paint.textSize = 20f
        paint.isFakeBoldText = false
        val title = "Habit History"
        val titleWidth = paint.measureText(title)
        val titleX = (pageWidth - titleWidth) / 2f
        val titleY = appNameY + 32f
        canvas.drawText(title, titleX, titleY, paint)

        // Draw habit history content
        paint.textSize = 14f
        var y = titleY + 36f

        val sortedDates = historyMap.keys.sortedDescending()
        for (date in sortedDates) {
            // Highlight date
            paint.color = android.graphics.Color.rgb(73, 122, 74)
            paint.isFakeBoldText = true
            canvas.drawText(date, 60f, y, paint)
            y += 20f
            paint.color = android.graphics.Color.DKGRAY
            paint.isFakeBoldText = false
            val habits = historyMap[date] ?: emptyList()
            if (habits.isEmpty()) {
                canvas.drawText("No completed habits", 80f, y, paint)
                y += 20f
            } else {
                for (habit in habits) {
                    canvas.drawText("• $habit", 80f, y, paint)
                    y += 20f
                }
            }
            y += 10f
            if (y > pageHeight - 40) {
                doc.finishPage(page)
                page = doc.startPage(pageInfo)
                canvas.drawRect(
                    10f,
                    10f,
                    (pageWidth - 10).toFloat(),
                    (pageHeight - 10).toFloat(),
                    borderPaint
                )
                y = 50f
            }
        }
        doc.finishPage(page)

        // Save to cache
        val file = File(cacheDir, "habit_history.pdf")
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