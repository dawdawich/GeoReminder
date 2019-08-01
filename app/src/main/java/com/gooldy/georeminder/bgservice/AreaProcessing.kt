package com.gooldy.georeminder.bgservice

import android.content.Context
import android.location.Location
import com.gooldy.georeminder.data.Area
import com.gooldy.georeminder.data.Reminder
import com.gooldy.georeminder.service.MainService

class AreaProcessing(context: Context) {

    private val dbService: MainService = MainService(context)
    private val activeAreas: Set<Area>

    init {
        activeAreas = dbService.getAllActiveAreas()
    }

    fun getAccordanceLocation(location: Location): Set<Reminder> {
        val latitude = location.latitude
        val longitude = location.longitude
        val result = mutableSetOf<Reminder>()
        activeAreas.forEach {
            if (isPosInArea(latitude, longitude, it)) {
                result.addAll(dbService.getAllRemindersByAreaId(it.id))
            }
        }
        return result
    }

    private fun isPosInArea(x: Double, y: Double, area: Area): Boolean {
        val results = FloatArray(1)
        Location.distanceBetween(x, y, area.latitude, area.longitude, results)
        return results[0] <= area.radius
    }

    fun updateReminders(reminders: Set<Reminder>) {
        reminders.forEach {
            dbService.updateReminder(it)
        }
    }

}