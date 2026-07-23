package com.example.habbittracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
class HabitAdapter
    (
    private val originalOrder: List<String>, // original order from preferences
    private val checkedOrderProvider: () -> List<String>, // provider for the list of checked items in their checked order
    private val checkedSetProvider: () -> Set<String>,
    private val onCheckChanged: (habit: String, checked: Boolean) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    // Compose display list: unchecked (in original order) then checked (in checked order)
    private fun buildDisplayList(): List<String> {
        val checked = checkedSetProvider()
        val checkedOrder = checkedOrderProvider()
        val unchecked = originalOrder.filter { it !in checked }
        // Ensure checked order contains only items that are currently checked and preserves order
        val checkedFiltered = checkedOrder.filter { it in checked }
        return unchecked + checkedFiltered
    }

    inner class HabitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val chk: CheckBox = view.findViewById(R.id.chkHabit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(v)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val displayList = buildDisplayList()
        val habit = displayList[position]
        holder.chk.setOnCheckedChangeListener(null) // reset old listener
        holder.chk.text = habit
        holder.chk.isChecked = habit in checkedSetProvider()

        holder.chk.setOnCheckedChangeListener { _, isChecked ->
            onCheckChanged(habit, isChecked)
            // notify dataset changed so list reorders (simple approach)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = buildDisplayList().size

}