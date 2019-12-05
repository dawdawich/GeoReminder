package com.gooldy.georeminder.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alespero.expandablecardview.ExpandableCardView
import com.gooldy.georeminder.R
import com.gooldy.georeminder.dao.entites.Reminder

class ReminderItemAdapter(private val reminders: Set<Reminder>, private val editFun: (Reminder) -> Unit,
                          private val removeFun: (Reminder) -> Unit) : RecyclerView.Adapter<ReminderItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val areaItemView = inflater.inflate(R.layout.item_reminder, parent, false)

        return ViewHolder(areaItemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reminder = reminders.elementAt(position)

        val cardReminder = holder.ecvItemReminder
        val cardDescription: TextView = cardReminder.findViewById(R.id.card_description)
        cardDescription.text = reminder.reminderText

        cardReminder.setOnExpandedListener { v, _ ->
            val cardEdit: TextView = v.findViewById(R.id.edit_reminder)
            cardEdit.setOnClickListener { editFun.invoke(reminder) }
            val cardRemove: TextView = v.findViewById(R.id.remove_reminder)
            cardRemove.setOnClickListener {
                cardReminder.collapse()
                removeFun.invoke(reminder)
            }
        }
        cardReminder.setTitle(reminder.reminderName)
    }

    override fun getItemCount(): Int {
        return reminders.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ecvItemReminder: ExpandableCardView = itemView.findViewById(R.id.reminder_card)
    }
}
