package com.gooldy.georeminder.service

import android.content.Context
import com.gooldy.georeminder.dao.DatabaseFactory
import com.gooldy.georeminder.data.Area
import com.gooldy.georeminder.data.Reminder
import java.util.*

class MainService(context: Context) {

    val dbFactory: DatabaseFactory = DatabaseFactory.getInstance(context)

    fun saveReminder(reminder: Reminder) {
        dbFactory.getReminderDao {
            it.addReminder(reminder)
        }
    }

    fun saveReminder(reminder: Reminder, areas: Set<Area>) {
        dbFactory.inTransaction { daoA, daoR, daoB ->
            daoR.addReminder(reminder)
            areas.forEach {
                daoA.addArea(it)
                daoB.addRelation(it.id, reminder.id)
            }
        }
    }

    fun saveArea(area: Area) {
        dbFactory.getAreaDao {
            it.addArea(area)
        }
    }

    fun getAllReminders(): Set<Reminder> {
        var reminders: Set<Reminder> = emptySet()
        dbFactory.getReminderDao {
            reminders = it.getAllReminders()
        }
        return reminders
    }

    fun getAllAreas(): Set<Area> {
        var areas: Set<Area> = emptySet()
        dbFactory.getAreaDao {
            areas = it.getAllAreas()
        }
        return areas
    }

    fun boundReminderWithAreas(rId: UUID, areaIds: Set<UUID>) {
        dbFactory.getReminderAreaDao {
            areaIds.forEach { id ->
                it.addRelation(id, rId)
            }
        }
    }
}
