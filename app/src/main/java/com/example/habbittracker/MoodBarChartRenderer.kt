package com.example.habbittracker

import android.graphics.Color
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter

class MoodAxisFormatter(private val moodLabels: Array<String>) : ValueFormatter() {
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        val index = value.toInt()
        return if (index >= 0 && index < moodLabels.size) {
            moodLabels[index]
        } else {
            ""
        }
    }
}