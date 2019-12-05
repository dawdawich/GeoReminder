package com.gooldy.georeminder.bgservice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.LocationResult
import com.gooldy.georeminder.dao.entites.Reminder

class LocationUpdatesBroadcastReceiver : BroadcastReceiver() {

    companion object {
        public const val ACTION_PROCESS_UPDATES = "com.google.android.gms.location.sample.backgroundlocationupdates.action.PROCESS_UPDATES"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            val action = it.action
            if (ACTION_PROCESS_UPDATES == action) {
                val result: LocationResult? = LocationResult.extractResult(it)
                result?.let { notNullResult ->
                    val areaProcessing = AreaProcessing(context!!)
                    val locationResultHelper = LocationResultHelper(context)
                    val reminders: Set<Reminder> = areaProcessing.getAccordanceLocation(notNullResult.locations)
                    reminders.forEach { reminder ->
                        locationResultHelper.showNotification(reminder)
                    }
                }
            }
        }

    }
}