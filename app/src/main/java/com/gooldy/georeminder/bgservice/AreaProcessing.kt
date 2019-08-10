package com.gooldy.georeminder.bgservice

import android.content.Context
import android.location.Location
import com.gooldy.georeminder.constants.ARROR_FOR_AN_ERROR
import com.gooldy.georeminder.data.Area
import com.gooldy.georeminder.data.Reminder
import com.gooldy.georeminder.service.MainService
import java.time.Instant

class AreaProcessing(context: Context) {

    private val dbService: MainService = MainService(context)
    private val activeReminders: Set<Reminder>

    init {
        activeReminders = dbService.getAllActiveReminders()
    }

    fun getAccordanceLocation(locations: List<Location>): Set<Reminder> {
        val result = mutableSetOf<Reminder>()
        val repeatReminders = mutableSetOf<Reminder>()
        locations.forEach { location ->
            val latitude = location.latitude
            val longitude = location.longitude
            val locationTime = Instant.ofEpochMilli(location.time)
            activeReminders.forEach { reminder ->
                if (reminder.modifyTime!!.isBefore(locationTime)) {
                    if (reminder.reminderAreas
                            .filter { isPosInArea(latitude, longitude, it) }
                            .any()) {
                        if (!reminder.notified) {
                            result += reminder
                            if (!reminder.repeatable) {
                                reminder.isActive = false
                            }
                            reminder.notified = true
                            reminder.modifyTime = Instant.now()
                        }
                    } else if (reminder.repeatable && reminder.notified && reminder.reminderAreas
                            .none { isPosInArea(latitude, longitude, it, ARROR_FOR_AN_ERROR) }) {
                        repeatReminders += reminder
                        reminder.notified = false
                        reminder.modifyTime = Instant.now()
                    }
                }
            }
        }
        updateReminders(repeatReminders)
        updateReminders(result)
        return result
    }

    private fun isPosInArea(x: Double, y: Double, area: Area, computationalErrorForResult: Double = 0.0): Boolean {
        val result = FloatArray(1)
        Location.distanceBetween(x, y, area.latitude, area.longitude, result)
        return result[0] + computationalErrorForResult <= area.radius
    }

    fun updateReminders(reminders: Set<Reminder>) {
        reminders.forEach {
            dbService.updateReminder(it)
        }
    }

}