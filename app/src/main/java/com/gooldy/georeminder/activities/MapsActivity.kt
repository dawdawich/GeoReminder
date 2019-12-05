package com.gooldy.georeminder.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.gooldy.georeminder.R
import com.gooldy.georeminder.constants.DEFAULT_ZOOM
import com.gooldy.georeminder.constants.MAP_VIEW_BUNDLE_KEY
import com.gooldy.georeminder.constants.PARAM_AREA
import com.gooldy.georeminder.dao.entites.Area
import com.mancj.materialsearchbar.MaterialSearchBar
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter
import kotlinx.android.synthetic.main.activity_maps.*
import java.util.*
import java.util.stream.Collectors

// Implemented like https://www.youtube.com/watch?v=ifoVBdtXsv0
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient
    private lateinit var predictionList: List<AutocompletePrediction>
    private lateinit var materialSearchBar: MaterialSearchBar
    private lateinit var geocoder: Geocoder

    private var mapView: View? = null
    private var lastLocation: Location? = null
    private var locationCallback: LocationCallback? = null
    private var addresses: List<Address>? = null

    private var isAreaStarted: Boolean = false
    private var activeCircle: Circle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        materialSearchBar = findViewById(R.id.searchBar)

        geocoder = Geocoder(this, Locale.getDefault())

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mapView = mapFragment.view

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastKnownLocation()
        Places.initialize(this, resources.getString(R.string.google_maps_key))
        placesClient = Places.createClient(this)

        val token: AutocompleteSessionToken = AutocompleteSessionToken.newInstance()

        materialSearchBar.setOnSearchActionListener(object : MaterialSearchBar.OnSearchActionListener {
            override fun onSearchStateChanged(enabled: Boolean) {
            }

            override fun onSearchConfirmed(text: CharSequence?) {
                startSearch(text.toString(), true, null, true)
            }

            override fun onButtonClicked(buttonCode: Int) {
                when (buttonCode) {
                    MaterialSearchBar.BUTTON_NAVIGATION -> return
                    MaterialSearchBar.BUTTON_BACK -> materialSearchBar.disableSearch()
                }
            }
        })

        val mainActivity = this

        materialSearchBar.addTextChangeListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val request = FindAutocompletePredictionsRequest.builder()
                    // TODO: need to know in which country need to search
//                    .setCountry(resources.configuration.locales[0].country)
                    .setTypeFilter(TypeFilter.ADDRESS)
                    .setSessionToken(token)
                    .setQuery(s.toString())
                    .build()

                placesClient.findAutocompletePredictions(request)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            task.result?.let {
                                materialSearchBar.clearSuggestions()
                                predictionList = it.autocompletePredictions
                                val suggestionList: List<String> = predictionList.stream()
                                    .map { prediction -> prediction.getFullText(null).toString() }
                                    .collect(Collectors.toList())
                                materialSearchBar.updateLastSuggestions(suggestionList)
                                if (!materialSearchBar.isSuggestionsVisible) {
                                    materialSearchBar.showSuggestionsList()
                                }
                            }
                        } else {
                            Log.i("TAG", "prediction fetching task unsuccessful")
                        }
                    }
                    .addOnFailureListener { ex ->
                        Toast.makeText(mainActivity, "Doesn't handle response: ${ex.message}", Toast.LENGTH_SHORT)
                            .show()
                    }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        materialSearchBar.setSuggstionsClickListener(object : SuggestionsAdapter.OnItemViewClickListener {
            override fun OnItemClickListener(position: Int, v: View?) {
                if (position >= predictionList.size) {
                    return
                }
                materialSearchBar.text = materialSearchBar.lastSuggestions[position].toString()
                Handler().postDelayed({
                    materialSearchBar.clearSuggestions()
                }, 1000)
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(materialSearchBar.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)

                val placeRequest: FetchPlaceRequest = FetchPlaceRequest
                    .builder(predictionList[position].placeId, mutableListOf(Place.Field.LAT_LNG))
                    .build()
                placesClient.fetchPlace(placeRequest)
                    .addOnSuccessListener { fetchPlaceResponse ->
                        val place = fetchPlaceResponse.place
                        Log.i("TAG", "Place found '${place.name}'")
                        place.latLng?.let {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 18f))
                        }
                    }
                    .addOnFailureListener { ex ->
                        if (ex is ApiException) {
                            ex.printStackTrace()
                            Log.i("TAG", "place not found '${ex.message}'")
                            Log.i("TAG", "status code '${ex.statusCode}'")
                        }
                    }
            }

            override fun OnItemDeleteListener(position: Int, v: View?) {

            }
        })

        btnChoose.setOnClickListener {
            if (!isAreaStarted) {
                // TODO: if user need it?
                mMap.uiSettings.isScrollGesturesEnabled = false
                mMap.uiSettings.isRotateGesturesEnabled = false
                mMap.uiSettings.isTiltGesturesEnabled = false
                mMap.uiSettings.isMyLocationButtonEnabled = false
                materialSearchBar.visibility = GONE
                marker.visibility = GONE
                btnChoose.visibility = GONE
                btnConfirm.visibility = VISIBLE
                btnCancel.visibility = VISIBLE
                btnMinus.visibility = VISIBLE
                btnPlus.visibility = VISIBLE
                tvRadius.visibility = VISIBLE

                val position = mMap.cameraPosition.target

                val circleOptions = CircleOptions()
                circleOptions.radius(20.0)
                circleOptions.center(position)
                circleOptions.fillColor(0x60FF8633) // orange
                circleOptions.strokeColor(0xF39C12)
                circleOptions.strokeWidth(5.0F)
                activeCircle = mMap.addCircle(circleOptions)

                tvRadius.text = "Radius: ${circleOptions.radius}m"

                addresses = geocoder.getFromLocation(position.latitude, position.longitude, 1)

                isAreaStarted = true
            }
        }
        btnMinus.setOnClickListener {
            if (isAreaStarted && activeCircle?.radius!! > 5) {
                activeCircle?.radius = activeCircle?.radius as Double - 5
                tvRadius.text = "Radius: ${activeCircle?.radius}m"
            }
        }
        btnPlus.setOnClickListener {
            if (isAreaStarted) {
                activeCircle?.radius = activeCircle?.radius as Double + 5
                tvRadius.text = "Radius: ${activeCircle?.radius}m"
            }
        }
        btnCancel.setOnClickListener {
            addresses = null
            cancelFromAreaSetUp()
        }
        btnConfirm.setOnClickListener {
            val center = activeCircle!!.center
            val radius = activeCircle!!.radius

            val area = Area(UUID.randomUUID(), center.latitude, center.longitude, radius, addresses!![0].getAddressLine(0), null)
            val intent = Intent()
            intent.putExtra(PARAM_AREA, area)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

//        var mapViewBundle: Bundle? = null
//        if (savedInstanceState != null) {
//            mapViewBundle =
//        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // TODO: add processing if self location do not granted
        if (checkSelfLocationPermission()) return
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

        mMap.setOnMyLocationButtonClickListener {
            if (materialSearchBar.isSuggestionsVisible) {
                materialSearchBar.clearSuggestions()
            }
            if (materialSearchBar.isSearchEnabled) {
                materialSearchBar.disableSearch()
            }
            false
        }

        if (mapView != null && mapView!!.findViewById<View>("1".toInt()) != null) {
            val locationButton =
                ((mapView as View).findViewById<View>("1".toInt()).parent as View).findViewById<View>("2".toInt())
            val layoutParams = locationButton.layoutParams as RelativeLayout.LayoutParams
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
            layoutParams.setMargins(0, 0, 40, 40)
        }

        val locationRequest = getLocationRequest()

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = settingsClient.checkLocationSettings(builder.build())

        task.addOnSuccessListener(this) { getDeviceLocation() }
        task.addOnFailureListener(this) { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(this, 51)
                } catch (e1: IntentSender.SendIntentException) {
                    e1.printStackTrace()
                }
            }
        }
    }

    private fun getLocationRequest(): LocationRequest {
        val locationRequest = LocationRequest.create()
        with(locationRequest) {
            interval = 10000
            fastestInterval = 5000
            setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        }
        return locationRequest
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 51 && resultCode == Activity.RESULT_OK) {
            getDeviceLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        fusedLocationClient.lastLocation.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result?.let {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), DEFAULT_ZOOM))
                } ?: run {
                    val locationRequest = getLocationRequest()
                    locationCallback = object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult?) {
                            super.onLocationResult(locationResult)
                            if (locationResult == null) {
                                return
                            }
                            lastLocation = locationResult.lastLocation
                            mMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        lastLocation!!.latitude,
                                        lastLocation!!.longitude
                                    ), DEFAULT_ZOOM
                                )
                            )
                            fusedLocationClient.removeLocationUpdates(this)
                        }
                    }
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
                }
            } else {
                Toast.makeText(this, "Unable to get last known location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkSelfLocationPermission(): Boolean {
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)

        var mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle)
        }
    }

    override fun onBackPressed() {
        if (isAreaStarted) {
            cancelFromAreaSetUp()
        } else {
            super.onBackPressed()
        }
    }

    private fun cancelFromAreaSetUp() {
        if (isAreaStarted) {
            mMap.uiSettings.isScrollGesturesEnabled = true
            mMap.uiSettings.isRotateGesturesEnabled = true
            mMap.uiSettings.isTiltGesturesEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = true
            materialSearchBar.visibility = VISIBLE
            marker.visibility = VISIBLE
            btnChoose.visibility = VISIBLE
            btnConfirm.visibility = GONE
            btnCancel.visibility = GONE
            btnMinus.visibility = GONE
            btnPlus.visibility = GONE
            tvRadius.visibility = GONE

            activeCircle?.remove()
            activeCircle = null

            isAreaStarted = false
        }
    }

    private fun getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation: called.")
        if (checkSelfLocationPermission()) return
        fusedLocationClient.lastLocation.addOnCompleteListener { task ->
            task.result?.let {
                val position = LatLng(it.latitude, it.longitude)
                Log.d(TAG, "onComplete: latitude: ${position.latitude}")
                Log.d(TAG, "onComplete: longitude: ${position.longitude}")
            }
        }
    }

    companion object {
        const val TAG = "MapsActivity"
    }
}
