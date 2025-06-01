package com.app.citytailor.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.app.citytailor.model.MapAnnotation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapView(
    region: CameraPosition,
    annotations: List<MapAnnotation>,
    selectedAnnotation: MapAnnotation?,
    onMapClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cameraPositionState = rememberCameraPositionState {
        position = region
    }
    
    // Update camera position when region changes
    LaunchedEffect(region) {
        cameraPositionState.animate(
            CameraUpdateFactory.newCameraPosition(region),
            1000
        )
    }
    
    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onMapClick = { onMapClick() }
    ) {
        annotations.forEach { annotation ->
            Marker(
                state = MarkerState(position = annotation.coordinate),
                title = annotation.title,
                snippet = annotation.subtitle,
                alpha = if (selectedAnnotation == annotation) 1.0f else 0.8f
            )
        }
    }
}
