package com.gooldy.georeminder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.gooldy.georeminder.constants.MAPVIEW_BUNDLE_KEY

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        var mapViewBundle : Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mMap.isMyLocationEnabled = true
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
}
