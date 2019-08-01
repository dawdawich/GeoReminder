package com.gooldy.georeminder.bgservice

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.SettingsClient
import com.gooldy.georeminder.R
import com.gooldy.georeminder.activities.MainActivity
import com.gooldy.georeminder.constants.NOTIFICATION_CHANNEL_ID

class PosCheckService : Service() {

    companion object {
        private const val TAG = "PosCheckService"
        private const val UPDATE_INTERVAL_IN_MILLISECONDS = 5000L
        private const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 500L
    }

    // google map api
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var settingsClient: SettingsClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationSettingRequest: LocationSettingsRequest
    private lateinit var locationCallback: LocationCallback
    // ---------------

    private lateinit var areaProcessing: AreaProcessing


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Getting location for geo reminder")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        settingsClient = LocationServices.getSettingsClient(this)
        locationCallback = object : LocationCallback() {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                receiveLocation(locationResult)
            }
        }
        locationRequest = LocationRequest()
        locationRequest.interval = UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        locationSettingRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()
        areaProcessing = AreaProcessing(this)

        startLocationUpdates()

        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        settingsClient
            .checkLocationSettings(locationSettingRequest)
            .addOnSuccessListener {
                Log.i(TAG, "All location settings are satisfied. No MissingPermission")

                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
            }
            .addOnFailureListener {
                when ((it as ApiException).statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> Log.w(TAG, "Location settings are not satisfied. Attempting to upgrade location settings")
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> Log.w(TAG, "Location settings are inadequate, and cannot be fixed here. Fix in Settings.")
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun receiveLocation(locationResult: LocationResult) {
        val accordanceAreas = areaProcessing.getAccordanceLocation(locationResult.lastLocation)
        accordanceAreas.forEach {
            if (!it.notified) {
                val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle(it.reminderName)
                    .setContentText(it.reminderText)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setDefaults(NotificationCompat.DEFAULT_SOUND)

                val notificationIntent = Intent(this, MainActivity::class.java)
                val contentIntent: PendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                builder.setContentIntent(contentIntent)

                val manager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                checkOrCreateNotificationChannel(manager)
                manager.notify(0, builder.build())

                it.notified = true
            }
        }
        areaProcessing.updateReminders(accordanceAreas)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkOrCreateNotificationChannel(nm: NotificationManager) {
        var isChannelCreated = false
        nm.notificationChannels.forEach {
            if (it.id == NOTIFICATION_CHANNEL_ID) {
                isChannelCreated = true
                return
            }
        }
        if (!isChannelCreated) {
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "geoReminderChannel",
                NotificationManager.IMPORTANCE_DEFAULT).apply {
                enableVibration(true)
                enableLights(true)
                lightColor = Color.YELLOW
            }
            nm.createNotificationChannel(notificationChannel)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

}
