package com.gooldy.georeminder.bgservice

import android.app.PendingIntent
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.gooldy.georeminder.dao.entites.Area

class GeofenceService(val context: AppCompatActivity) {

    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)

    private fun setGeofences(areas: List<Area>) {
        val geofences = areas.map {
            Geofence.Builder()
                .setRequestId(it.id.toString())
                .setCircularRegion(it.latitude, it.longitude, it.radius.toFloat())
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setLoiteringDelay(0)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL)
                .build()
        }
        geofencingClient.addGeofences(GeofencingRequest.Builder()
            .addGeofences(geofences)
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL)
            .build(), getGeofencePendingIntent()).addOnCanceledListener {
            // TODO: complete oncomplete task
        }
    }

    private fun getGeofencePendingIntent(): PendingIntent {
        val intent = Intent(context, null)
        return PendingIntent.getService(context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT)
    }

}
