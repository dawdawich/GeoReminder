package com.gooldy.georeminder.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gooldy.georeminder.R

class ReminderItemAdapter(private val reminders: Set<Reminder>, private val consumer: (Reminder) -> Unit) : RecyclerView.Adapter<ReminderItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val areaItemView = inflater.inflate(R.layout.item_reminder, parent, false)

        return ViewHolder(areaItemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reminder = reminders.elementAt(position)

        val textView = holder.tvItemReminder
        holder.view.setOnClickListener {
            consumer.invoke(reminder)
        }
        textView.text = "${reminder.reminderName} -------- ${reminder.reminderText}"
    }

    override fun getItemCount(): Int {
        return reminders.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvItemReminder: TextView = itemView.findViewById(R.id.tvReminderItem)
        val view: View = itemView
    }
}
