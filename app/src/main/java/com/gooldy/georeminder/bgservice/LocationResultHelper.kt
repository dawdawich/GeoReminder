package com.gooldy.georeminder.bgservice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import com.gooldy.georeminder.R
import com.gooldy.georeminder.activities.MainActivity
import com.gooldy.georeminder.constants.NOTIFICATION_CHANNEL_ID
import com.gooldy.georeminder.dao.entites.Reminder

@RequiresApi(Build.VERSION_CODES.O)
class LocationResultHelper(val context: Context) {

    companion object {
        const val KEY_LOCATION_UPDATE_RESULT = "location_update_result"
    }

    private var notificationManager: NotificationManager? = null
        get() {
            field?.let { return field }
                ?: run { field = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
            return field
        }

    init {
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID,
            context.getString(R.string.default_channel), NotificationManager.IMPORTANCE_DEFAULT)
        channel.lightColor = Color.YELLOW
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        notificationManager?.createNotificationChannel(channel)
    }


    fun showNotification(reminder: Reminder) {
        val notificationIntent = Intent(context, MainActivity::class.java)
        // Construct a task stack.
        val stackBuilder: TaskStackBuilder = TaskStackBuilder.create(context)

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity::class.java)

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent)

        // Get a PendingIntent containing the entire back stack.
        val notificationPendingIntent: PendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationBuilder = Notification.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(reminder.reminderName)
            .setContentText(reminder.reminderText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .setContentIntent(notificationPendingIntent)

        notificationManager?.notify(reminder.hashCode(), notificationBuilder.build())
    }

}