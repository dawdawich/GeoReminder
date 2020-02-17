package com.gooldy.georeminder.bgservice

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.LocationResult
import com.gooldy.georeminder.constants.ACTION_DELETE
import com.gooldy.georeminder.constants.EXTRA_REMINDER_ID_KEY
import com.gooldy.georeminder.data.Reminder
import com.gooldy.georeminder.service.MainService
import java.util.*


class LocationUpdatesBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_PROCESS_UPDATES = "com.google.android.gms.location.sample.backgroundlocationupdates.action.PROCESS_UPDATES"
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
            if (ACTION_DELETE == action) {
                if (intent.hasExtra(EXTRA_REMINDER_ID_KEY)) {
                    val reminderId = intent.getSerializableExtra(EXTRA_REMINDER_ID_KEY) as UUID
                    intent.removeExtra(EXTRA_REMINDER_ID_KEY)
                    // TODO update main list if reminder was deleted
                    MainService(context!!).removeReminder(reminderId)
                    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    manager.cancel(reminderId.hashCode())
                }
            }
        }
    }
}