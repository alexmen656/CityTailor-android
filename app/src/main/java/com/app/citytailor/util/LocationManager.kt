package com.app.citytailor.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LocationManager(private val context: Context) : ViewModel() {
    
    private val fusedLocationClient: FusedLocationProviderClient = 
        LocationServices.getFusedLocationProviderClient(context)
    
    private val _location = MutableStateFlow<LatLng?>(null)
    val location: StateFlow<LatLng?> = _location.asStateFlow()
    
    private val _hasLocationPermission = MutableStateFlow(false)
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission.asStateFlow()
    
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                _location.value = LatLng(location.latitude, location.longitude)
            }
        }
    }
    
    fun setLocationPermission(hasPermission: Boolean) {
        _hasLocationPermission.value = hasPermission
        if (hasPermission) {
            startLocationUpdates()
        } else {
            stopLocationUpdates()
        }
    }
    
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (_hasLocationPermission.value) {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                10000L // 10 seconds
            ).apply {
                setMinUpdateDistanceMeters(50f) // 50 meters
                setMaxUpdateDelayMillis(15000L) // 15 seconds max delay
            }.build()
            
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
            
            // Get last known location immediately
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    _location.value = LatLng(it.latitude, it.longitude)
                }
            }
        }
    }
    
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
    
    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
}
