package com.example.habbittracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MoodHistoryAdapter(
    private val moodList: List<MoodEntry>,
    private val moodImages: Array<Int>
) : RecyclerView.Adapter<MoodHistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgMood: ImageView = itemView.findViewById(R.id.imgMood)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvNote: TextView = itemView.findViewById(R.id.tvNote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val entry = moodList[position]
        holder.imgMood.setImageResource(moodImages[entry.moodIndex])
        holder.tvDate.text = entry.date
        holder.tvNote.text = entry.note
    }

    override fun getItemCount(): Int = moodList.size
}
