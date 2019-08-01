package com.gooldy.georeminder.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gooldy.georeminder.R

class ReminderItemAdapter(private val reminders: List<Reminder>) : RecyclerView.Adapter<ReminderItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val areaItemView = inflater.inflate(R.layout.item_reminder, parent, false)

        return ViewHolder(areaItemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reminder = reminders[position]

        val textView = holder.tvItemReminder
        textView.text = "${reminder.reminderName} -------- ${reminder.reminderText}"
    }

    override fun getItemCount(): Int {
        return reminders.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvItemReminder: TextView = itemView.findViewById(R.id.tvReminderItem)
    }
}
