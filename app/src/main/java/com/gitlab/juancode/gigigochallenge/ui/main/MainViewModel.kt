package com.gitlab.juancode.gigigochallenge.ui.main

import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitlab.juancode.gigigochallenge.data.PlayServicesLocation
import com.gitlab.juancode.gigigochallenge.service.DirectionRetrofit
import com.gitlab.juancode.gigigochallenge.ui.common.Event
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _requestPermission = MutableLiveData<Event<Unit>>()
    val requestPermission: LiveData<Event<Unit>> get() = _requestPermission

    private val _locationLive = MutableLiveData<Location>()
    val locationLive: LiveData<Location> get() = _locationLive

    private val _dataDirectionsLive = MutableLiveData<List<LatLng>>()
    val dataDirectionsLive: LiveData<List<LatLng>> get() = _dataDirectionsLive

    init {
        loadPermission()
    }

    private fun loadPermission() {
        _requestPermission.value = Event(Unit)
    }

    fun onFinePermissionRequested(context: Context) {
        viewModelScope.launch {
            val location = PlayServicesLocation(context).findLastLocation()
            _locationLive.value = location
        }
    }

    fun getDataFromDirections(origin: LatLng, destiny: LatLng) {
        val listSteps = mutableListOf<LatLng>()
        viewModelScope.launch {
            val response = DirectionRetrofit.service.getDataFromDirection(
                origin = "${origin.latitude},${origin.longitude}",
                destiny = "${destiny.latitude},${destiny.longitude}"
            )

            val steps = response.routes[0].legs[0].steps
            steps.forEach { step ->
                listSteps.add(LatLng(step.start_location.lat, step.start_location.lng))
                listSteps.add(LatLng(step.end_location.lat, step.end_location.lng))
            }

            _dataDirectionsLive.value = listSteps
        }
    }
}