package com.example.myapplication.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.entity.DailyReminder

class DailyReminderAdapter(
    private val reminders: List<DailyReminder>,
    private val onItemClick: (DailyReminder) -> Unit
) : RecyclerView.Adapter<DailyReminderAdapter.DailyReminderViewHolder>() {

    inner class DailyReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameReminderTextView: TextView = itemView.findViewById(R.id.nameReminderTV)
        private val titleReminderTextView: TextView = itemView.findViewById(R.id.titleReminderTV)

        @SuppressLint("SetTextI18n", "DefaultLocale")
        fun bind(reminder: DailyReminder) {
            nameReminderTextView.text = reminder.name
            val formattedTime = String.format("%02d:%02d", reminder.hour, reminder.minute)
            titleReminderTextView.text = formattedTime
            itemView.setOnClickListener {
                onItemClick(reminder)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyReminderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout_reminder, parent, false)
        return DailyReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: DailyReminderViewHolder, position: Int) {
        val reminder = reminders[position]
        holder.bind(reminder)
    }

    override fun getItemCount(): Int {
        return reminders.size
    }
}
