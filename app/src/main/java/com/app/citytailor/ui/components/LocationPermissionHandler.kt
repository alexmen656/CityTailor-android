package com.app.citytailor.ui.components

import android.Manifest
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.app.citytailor.util.LocationManager
import com.google.accompanist.permissions.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionHandler(
    locationManager: LocationManager,
    content: @Composable () -> Unit
) {
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    
    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        locationManager.setLocationPermission(locationPermissionsState.allPermissionsGranted)
    }
    
    LaunchedEffect(Unit) {
        if (!locationPermissionsState.allPermissionsGranted) {
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }
    
    content()
}
