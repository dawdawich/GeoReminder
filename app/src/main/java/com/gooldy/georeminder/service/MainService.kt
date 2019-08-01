package com.gooldy.georeminder.service

import android.content.Context
import com.gooldy.georeminder.dao.DatabaseFactory
import com.gooldy.georeminder.data.Area
import com.gooldy.georeminder.data.Reminder
import java.util.*

class MainService(context: Context) {

    private val dbFactory: DatabaseFactory = DatabaseFactory.getInstance(context)

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

    fun updateReminder(reminder: Reminder) {
        dbFactory.getReminderDao {
            it.updateReminder(reminder)
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

    fun getAllRemindersByAreaId(aId: UUID): Set<Reminder> {
        val reminders: MutableSet<Reminder> = mutableSetOf()
        dbFactory.inTransaction { _, daoR, daoB ->
            val reminderIds = daoB.getRemindersByAreaId(aId)
            reminderIds.forEach {
                daoR.getReminder(UUID.fromString(it))?.let {
                    reminders.add(it)
                }
            }
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

    fun getAllActiveAreas(): Set<Area> {
        val activeAreas = mutableSetOf<Area>()
        dbFactory.inTransaction { daoA, _, daoB ->
            val areaIds: Set<String> = daoB.getAllAreaIds()
            areaIds.forEach { id ->
                daoA.getArea(UUID.fromString(id))?.let { activeAreas.add(it) }
            }
        }
        return activeAreas
    }

    fun boundReminderWithAreas(rId: UUID, areaIds: Set<UUID>) {
        dbFactory.getReminderAreaDao {
            areaIds.forEach { id ->
                it.addRelation(id, rId)
            }
        }
    }
}
