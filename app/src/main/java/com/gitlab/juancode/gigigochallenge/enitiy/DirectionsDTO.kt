package com.gitlab.juancode.gigigochallenge.enitiy

data class DirectionsDTO(
    val geocoded_waypoints: List<GeocodedWaypoint>,
    val routes: List<Route>,
    val status: String
)