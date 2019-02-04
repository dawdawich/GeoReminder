package com.gooldy.georeminder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.gooldy.georeminder.constants.MAPVIEW_BUNDLE_KEY

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        var mapViewBundle : Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastKnownLocation()

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (checkSelfLocationPermission()) return
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnCompleteListener { task ->
            task.result?.let {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 17f))
            }
        }
    }

    private fun checkSelfLocationPermission(): Boolean {
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        var mapViewBundle = outState?.getBundle(MAPVIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState?.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle)
        }

        mapView.onSaveInstanceState(mapViewBundle)
    }

    private fun getLastKnownLocation() {
        Log.d(MapsActivity.TAG, "getLastKnownLocation: called.")
        if (checkSelfLocationPermission()) return
        fusedLocationClient.lastLocation.addOnCompleteListener { task ->
            task.result?.let {
                val position = LatLng(it.latitude, it.longitude)
                Log.d(MapsActivity.TAG, "onComplete: latitude: ${position.latitude}")
                Log.d(MapsActivity.TAG, "onComplete: longitude: ${position.longitude}")
            }
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()

    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    companion object {
        const val TAG = "MapsActivity"
    }
}
