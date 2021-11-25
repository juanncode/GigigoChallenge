package com.gitlab.juancode.gigigochallenge.ui.main

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.gitlab.juancode.gigigochallenge.R
import com.gitlab.juancode.gigigochallenge.R.id
import com.gitlab.juancode.gigigochallenge.databinding.ActivityMainBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private var destinyMarker: Marker? = null
    private var currentPolyline: Polyline? = null
    private val db = FirebaseFirestore.getInstance()
    private lateinit var mMapView: MapView
    private lateinit var binding: ActivityMainBinding
    private lateinit var map: GoogleMap
    private lateinit var viewModel: MainViewModel

    private val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
    private val AUTOCOMPLETE_REQUEST_CODE = 1
    private var nameDestinyMarker = ""
    private var latLngDestiny: LatLng? = null
    private var latLngOrigin: LatLng? = null
    private var distanceAndTime = Pair("", "")

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.forEach { isGrantedMap ->
                if (isGrantedMap.key == Manifest.permission.ACCESS_FINE_LOCATION) {
                    viewModel.onFinePermissionRequested(this)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }

        mMapView = findViewById<View>(id.mapView) as MapView
        mMapView.onCreate(mapViewBundle)

        mMapView.getMapAsync(this)

        observers()

        Places.initialize(applicationContext, getString(R.string.google_maps_key))
        val placesClient = Places.createClient(this)

        binding.layoutSearch.setOnClickListener {
            goToAutocompleteForm()
        }

    }

    private fun goToAutocompleteForm() {
        val fields = listOf(Place.Field.ID, Place.Field.NAME)

        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .build(this)
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }

    private fun observers() {
        viewModel.requestPermission.observe(this, {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                )
            )
        })

        viewModel.locationLive.observe(this, {
            if (it != null) {
                latLngOrigin = LatLng(it.latitude, it.longitude)
                map.addMarker(
                    MarkerOptions().position(LatLng(it.latitude, it.longitude)).title("Your origin")
                )
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(it.latitude, it.longitude),
                        12f
                    )
                )
            }
        })
        viewModel.distanceAndTimeLive.observe(this, {
            distanceAndTime = it
        })

        viewModel.dataDirectionsLive.observe(this, {
            paintPolyline(it)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        val place = Autocomplete.getPlaceFromIntent(data)
                        nameDestinyMarker = place.name
                        latLngDestiny = getLatLngOfAddress(nameDestinyMarker)

                        if (latLngDestiny != null) {
                            destinyMarker?.remove()
                            destinyMarker = map.addMarker(
                                MarkerOptions().position(latLngDestiny!!)
                                    .title("$nameDestinyMarker")
                            )
                            latLngOrigin?.let { it1 ->
                                viewModel.getDataFromDirections(
                                    it1,
                                    latLngDestiny!!
                                )
                            }
                        }
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {

                }
            }
            return
        }

        super.onActivityResult(requestCode, resultCode, data)

    }

    private fun getLatLngOfAddress(address: String): LatLng? {
        val geocoder = Geocoder(this)
        val addressSinceGeocoder = geocoder.getFromLocationName(address, 1)
        return if (addressSinceGeocoder != null) {
            LatLng(addressSinceGeocoder[0].latitude, addressSinceGeocoder[0].longitude)
        } else {
            null
        }
    }

    private fun paintPolyline(listLatLng: List<LatLng>) {
        currentPolyline?.remove()

        val lineOption = PolylineOptions()
        listLatLng.forEach {
            lineOption.add(it)
            lineOption.width(10f)
            lineOption.color(Color.BLACK)
            lineOption.geodesic(true)
        }
        currentPolyline = map.addPolyline(lineOption)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        var mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle)
        }

        mMapView.onSaveInstanceState(mapViewBundle)
    }

    override fun onResume() {
        super.onResume()
        mMapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mMapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mMapView.onStop()
    }

    override fun onMapReady(map: GoogleMap) {
        this.map = map
        this.map.setOnMarkerClickListener(this)

    }

    override fun onPause() {
        mMapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mMapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView.onLowMemory()
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        if (marker.title.toString() == nameDestinyMarker) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(nameDestinyMarker)
            builder.setMessage("Latitud: ${marker.position.latitude}\nLongitud: ${marker.position.longitude}\nDistancia: ${distanceAndTime.first}\nTiempo: ${distanceAndTime.second}")
            builder.show()
        }
        return true

    }
}