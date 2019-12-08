package com.gooldy.georeminder.activities

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.gooldy.georeminder.R
import com.gooldy.georeminder.bgservice.LocationResultHelper
import com.gooldy.georeminder.bgservice.LocationUpdatesBroadcastReceiver
import com.gooldy.georeminder.constants.ERROR_DIALOG_REQUEST
import com.gooldy.georeminder.constants.PARAM_AREA
import com.gooldy.georeminder.constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
import com.gooldy.georeminder.constants.PERMISSIONS_REQUEST_ENABLE_GPS
import com.gooldy.georeminder.dao.entites.Area
import com.gooldy.georeminder.dao.entites.Reminder
import com.gooldy.georeminder.data.ReminderItemAdapter
import com.gooldy.georeminder.fragments.CardContent
import com.gooldy.georeminder.service.MainService
import com.gooldy.georeminder.service.MainService.Companion.observeOn
import io.reactivex.Observable.fromCallable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity(), CardContent.OnFragmentInteractionListener, GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private var isEditContentEnable = false
    private var mLocationPermissionGranted = false

    // Fragments
    private lateinit var cardContentFragment: CardContent

    // DB service
    private lateinit var dbService: MainService

    // RecyclerView
    private val reminders: MutableSet<Reminder> = mutableSetOf()
    private lateinit var itemAdapter: ReminderItemAdapter

    // Google Play Map Services
    private lateinit var googleApiClient: GoogleApiClient
    private lateinit var locationRequest: LocationRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dbService = MainService(this)

        observeOn {
            fromCallable { dbService.getAllReminders() }
                .doOnNext { reminders.addAll(it) }
                .doOnComplete {
                    itemAdapter = ReminderItemAdapter(reminders, { reminder ->
                        cardContentFragment = CardContent.newInstance(reminder)
                        instantiateFragment()
                    }) { reminder ->
                        observeOn {
                            fromCallable { dbService.removeReminder(reminder) }
                                .doOnNext {
                                    reminders.remove(reminder)
                                    itemAdapter.notifyDataSetChanged()
                                }
                        }
                    }
                    reminderContainer.adapter = itemAdapter
                    reminderContainer.layoutManager = LinearLayoutManager(this)
                }
        }

        fab.setOnClickListener {
            cardContentFragment = CardContent.newInstance(null)
            instantiateFragment()
        }

        if (!checkPermission()) {
            requestPermission()
        }
        buildGoogleApiClient()
        requestLocationUpdates()
    }

    override fun onStart() {
        super.onStart()
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)
    }

    private fun instantiateFragment() {
        if (!isEditContentEnable) {
            supportFragmentManager.beginTransaction().apply {
                addToBackStack(null)
                setCustomAnimations(
                    R.anim.fragment_anim_slide_in_up,
                    R.anim.fragment_anim_slide_out_up,
                    R.anim.fragment_anim_slide_in_up,
                    R.anim.fragment_anim_slide_out_up
                )
                replace(cardFragment.id, cardContentFragment)
            }.commit()
            fab.hide()
            isEditContentEnable = true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will

        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onFragmentInteraction(reminder: Reminder, isUpdate: Boolean) {
        fab.show()
        isEditContentEnable = false

        if (!isUpdate) {
            observeOn {
                fromCallable { dbService.saveReminderWithAreas(reminder, reminder.areas) }
            }
            reminders.add(reminder)
            val itemAdapter = ReminderItemAdapter(reminders, { reminderTransfer ->
                cardContentFragment = CardContent.newInstance(reminderTransfer)
                instantiateFragment()
            }) { reminderTransfer ->
                observeOn {
                    fromCallable { dbService.removeReminder(reminderTransfer) }
                        .doOnComplete {
                            reminders.remove(reminderTransfer)
                            itemAdapter.notifyDataSetChanged()
                        }
                }
            }
            reminderContainer.adapter = itemAdapter
        } else {
            observeOn {
                fromCallable { dbService.updateReminder(reminder) }
            }
            reminders.add(reminder)
            reminderContainer.adapter?.notifyDataSetChanged()
        }

    }

    override fun onBackPressed() {
        val count = supportFragmentManager.backStackEntryCount

        if (count == 0) {
            super.onBackPressed()
        } else {
            if (count == 1) {
                fab.show()
                isEditContentEnable = false
            }
            supportFragmentManager.popBackStack()
        }
    }

    fun checkMapServices(): Boolean {
        return isServiceOk() && isMapsEnabled()
    }

    private fun isServiceOk(): Boolean {
        Log.d(TAG, "isServicesOK: checking google services version")

        val available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)

        when {
            available == ConnectionResult.SUCCESS -> {
                //everything is fine and the user can make map requests
                Log.d(TAG, "isServicesOK: Google Play Services is working")
                return true
            }
            GoogleApiAvailability.getInstance().isUserResolvableError(available) -> {
                //an error occured but we can resolve it
                Log.d(TAG, "isServicesOK: an error occurred but we can fix it")
                val dialog = GoogleApiAvailability.getInstance().getErrorDialog(
                    this, available,
                    ERROR_DIALOG_REQUEST
                )
                dialog.show()
            }
            else -> Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show()
        }

        return false
    }

    private fun isMapsEnabled(): Boolean {
        val manager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
            return false
        }
        return true
    }

    private fun buildAlertMessageNoGps() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                val enableGpsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS)
            }
        val alert = builder.create()
        alert.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        mLocationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ENABLE_GPS -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true
                }
            }
        }
    }

    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */

        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mLocationPermissionGranted = true
            startMapActivity()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: called.")
        when (requestCode) {
            PERMISSIONS_REQUEST_ENABLE_GPS -> if (mLocationPermissionGranted) {
                startMapActivity()
            } else {
                getLocationPermission()
            }
            MAP_CIRCLE_REQUEST -> if (resultCode == Activity.RESULT_OK) {
                cardContentFragment.setMapCoordinate(data?.getSerializableExtra(PARAM_AREA) as Area)
            }
        }
    }

    fun startMapActivity() {
        val mapIntent = Intent(this, MapsActivity::class.java)
//        startActivityFromFragment(cardContentFragment, mapIntent, MAP_CIRCLE_REQUEST)
        startActivityForResult(mapIntent, MAP_CIRCLE_REQUEST)
    }

    fun returnFab() {
        fab.show()
        isEditContentEnable = false
    }

    private fun checkPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        val shouldProvideRationable: Boolean = ActivityCompat.shouldShowRequestPermissionRationale(this,
            Manifest.permission.ACCESS_FINE_LOCATION)
        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationable) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
            Snackbar.make(findViewById(R.id.activity_main), R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_PERMISSIONS_REQUEST_CODE)
                }.show()
        } else {
            Log.i(TAG, "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSIONS_REQUEST_CODE)
        }
    }

    private fun buildGoogleApiClient() {
        if (!::googleApiClient.isInitialized) {
            googleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addApi(LocationServices.API)
                .build()
            createLocationRequest()
        }
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest().apply {
            interval = UPDATE_INTERVAL.toLong()

            // Sets the fastest rate for active location updates. This interval is exact, and your
            // application will never receive updates faster than this value.
            fastestInterval = FASTEST_UPDATE_INTERVAL.toLong()
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY

            // Sets the maximum time when batched location updates are delivered. Updates may be
            // delivered sooner than this interval.
            maxWaitTime = MAX_WAIT_TIME.toLong()
        }
    }

    override fun onConnected(bundle: Bundle?) {
        Log.i(TAG, "GoogleApiClient connected.")
    }

    override fun onConnectionSuspended(errorCode: Int) {
        Log.w(TAG, "Connection suspended. Error code '$errorCode'")
        showSnackbar("Connection suspended")
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.w(TAG, "Exception while connection to Google Play Service. Error message '${connectionResult.errorMessage}'")
        showSnackbar("Exception while connection to Google Play Service")
    }

    private fun showSnackbar(text: String) {
        val container = findViewById<View>(R.id.activity_main)
        container?.let { Snackbar.make(it, text, Snackbar.LENGTH_LONG).show() }
    }

    private fun requestLocationUpdates() {
        try {
            Log.i(TAG, "Starting location updates")
            setRequesting(this, true)
            LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, getPendingIntent())
        } catch (e: SecurityException) {
            setRequesting(this, false)
            e.printStackTrace()
        }
    }

    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(this, LocationUpdatesBroadcastReceiver::class.java)
        intent.action = LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, s: String?) {
        if (s == LocationResultHelper.KEY_LOCATION_UPDATE_RESULT) {
            // need to be implemented
        }

    }

    companion object {
        private const val KEY_LOCATION_UPDATES_REQUESTED = "location-updates-requested"

        const val TAG = "MainActivity"
        const val MAP_CIRCLE_REQUEST = 1001
        const val REQUEST_PERMISSIONS_REQUEST_CODE = 34
        const val UPDATE_INTERVAL = 10 * 1000
        const val FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 2

        const val MAX_WAIT_TIME = UPDATE_INTERVAL * 3

        fun setRequesting(context: Context, value: Boolean) {
            PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_LOCATION_UPDATES_REQUESTED, value)
                .apply()
        }
    }
}
