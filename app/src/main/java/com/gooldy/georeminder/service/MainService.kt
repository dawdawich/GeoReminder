package com.gooldy.georeminder.service

import android.content.Context
import com.gooldy.georeminder.dao.DatabaseFactory
import com.gooldy.georeminder.data.Area
import com.gooldy.georeminder.data.Reminder
import java.util.*

class MainService(context: Context) {

    private val dbFactory: DatabaseFactory = DatabaseFactory.getInstance(context)

    fun saveReminder(reminder: Reminder) {
        dbFactory.inTransaction { daoA, daoR, daoB ->
            daoR.addReminder(reminder)
            reminder.reminderAreas.forEach {
                daoA.addArea(it)
                daoB.addRelation(it.id, reminder.id)
            }
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
        dbFactory.inTransaction { daoA, daoR, daoB ->
            daoR.updateReminder(reminder)
            reminder.reminderAreas.forEach {
                // FIXME: not add already added areas
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
        dbFactory.inTransaction { daoA, daoR, daoB ->
            reminders = daoR.getAllReminders()
            reminders.forEach { reminder ->
                val areaIds = daoB.getAreaIds(reminder.id)
                val areas = mutableSetOf<Area>()
                areaIds.forEach {
                    daoA.getArea(UUID.fromString(it))?.let { area ->
                        areas.add(area)
                    }
                }
                reminder.reminderAreas = areas
            }
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

    fun getAllActiveReminders(): Set<Reminder> {
        var activeReminders: Set<Reminder> = setOf()
        dbFactory.inTransaction { daoA, daoR, daoB ->
            activeReminders = daoR.getAllActiveReminders()
            activeReminders.forEach { reminder ->
                val areaIds = daoB.getAreaIds(reminder.id)
                val areas = mutableSetOf<Area>()
                areaIds.forEach { areaId -> daoA.getArea(UUID.fromString(areaId))?.let { area -> areas += area } }
                reminder.reminderAreas = areas
            }
        }
        return activeReminders
    }

    fun boundReminderWithAreas(rId: UUID, areaIds: Set<UUID>) {
        dbFactory.getReminderAreaDao {
            areaIds.forEach { id ->
                it.addRelation(id, rId)
            }
        }
    }

    fun removeReminder(reminder: Reminder) {
        dbFactory.inTransaction { daoA, daoR, daoB ->
            daoB.deleteRelations(reminder.id)
            reminder.reminderAreas.forEach {
                daoA.deleteArea(it.id)
            }
            daoR.deleteReminder(reminder.id)
        }
    }

    fun removeReminder(reminderId: UUID) {
        dbFactory.inTransaction { daoA, daoR, daoB ->
            val reminder = daoR.getReminder(reminderId)
            daoB.deleteRelations(reminderId)
            reminder?.reminderAreas?.forEach {
                daoA.deleteArea(it.id)
            }
            reminder?.id?.let { daoR.deleteReminder(it) }
        }
    }
}
