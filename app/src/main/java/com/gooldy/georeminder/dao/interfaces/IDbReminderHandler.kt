package com.gooldy.georeminder.dao.interfaces

import com.gooldy.georeminder.data.Reminder
import java.util.*

interface IDbReminderHandler {

    fun getAllReminders(): Set<Reminder>
    fun getAllActiveReminders(): Set<Reminder>
    fun getReminder(id: UUID): Reminder?
    fun addReminder(reminder: Reminder)
    fun updateReminder(reminder: Reminder)
    fun deleteReminder(id: UUID)
    fun deleteAll()

}