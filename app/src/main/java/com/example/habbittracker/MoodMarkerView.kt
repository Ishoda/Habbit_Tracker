package com.example.habbittracker

import android.content.Context
import android.widget.ImageView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight


class MoodMarkerView(
    context: Context,
    layoutResource: Int,
    private val moodImages: Array<Int>
) : MarkerView(context, layoutResource) {

    private val imageView: ImageView = findViewById(R.id.moodIcon)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        e?.let {
            val moodIndex = e.y.toInt()
            val iconRes = when (moodIndex) {
                0 -> R.drawable.energized
                1 -> R.drawable.motivated
                2 -> R.drawable.calm
                3 -> R.drawable.hopeful
                4 -> R.drawable.bottle_c1
                else -> R.drawable.grateful
            }
            imageView.setImageResource(iconRes)
        }
        super.refreshContent(e, highlight)
    }
}