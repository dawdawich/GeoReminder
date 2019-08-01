package com.gooldy.georeminder.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.gooldy.georeminder.R
import com.gooldy.georeminder.bgservice.PosCheckService
import com.gooldy.georeminder.constants.ERROR_DIALOG_REQUEST
import com.gooldy.georeminder.constants.PARAM_AREA
import com.gooldy.georeminder.constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
import com.gooldy.georeminder.constants.PERMISSIONS_REQUEST_ENABLE_GPS
import com.gooldy.georeminder.data.Area
import com.gooldy.georeminder.data.Reminder
import com.gooldy.georeminder.data.ReminderItemAdapter
import com.gooldy.georeminder.fragments.CardContent
import com.gooldy.georeminder.service.MainService
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.time.LocalDateTime
import java.util.*


class MainActivity : AppCompatActivity(), CardContent.OnFragmentInteractionListener {

    private var isEditContentEnable = false
    private var mLocationPermissionGranted = false
    private lateinit var cardContentFragment: CardContent
    private val dbService: MainService = MainService(this)

    private val reminders: MutableList<Reminder> = mutableListOf()
    private lateinit var itemAdapter: ReminderItemAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        reminders.addAll(dbService.getAllReminders())

        itemAdapter = ReminderItemAdapter(reminders)

        fab.setOnClickListener {
            if (!isEditContentEnable) {
                supportFragmentManager.beginTransaction().apply {
                    addToBackStack(null)
                    setCustomAnimations(
                        R.anim.fragment_anim_slide_in_up,
                        R.anim.fragment_anim_slide_out_up,
                        R.anim.fragment_anim_slide_in_up,
                        R.anim.fragment_anim_slide_out_up
                    )
                    cardContentFragment = CardContent.newInstance("", "")
                    replace(cardFragment.id, cardContentFragment)
                }.commit()
                fab.hide()
                isEditContentEnable = true
            }
        }
        reminderContainer.adapter = itemAdapter
        reminderContainer.layoutManager = LinearLayoutManager(this)

        val intent = Intent(this, PosCheckService::class.java)
        startService(intent)
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
    override fun onFragmentInteraction(params: Map<String, Any>) {
        fab.show()
        isEditContentEnable = false
        val areas = params["areas"] as List<Area>
        val reminder = Reminder(UUID.randomUUID(), params["reminderName"] as String,
            params["reminderDescription"] as String, areas.toSet(),
            LocalDateTime.now(), LocalDateTime.now())
        dbService.saveReminder(reminder, areas.toSet())
        reminders.add(reminder)

        val itemAdapter = ReminderItemAdapter(reminders)
        reminderContainer.adapter = itemAdapter

        itemAdapter.notifyDataSetChanged()

        val intent = Intent(this, PosCheckService::class.java)
        stopService(intent)
        startService(intent)
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
        startActivityForResult(mapIntent, MAP_CIRCLE_REQUEST)
    }

    companion object {
        const val TAG = "MainActivity"
        const val MAP_CIRCLE_REQUEST = 1001
    }
}
