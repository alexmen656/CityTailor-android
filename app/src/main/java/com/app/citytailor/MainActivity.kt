package com.app.citytailor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.app.citytailor.ui.components.LocationPermissionHandler
import com.app.citytailor.ui.screens.ContentView
import com.app.citytailor.ui.theme.CityTailorTheme
import com.app.citytailor.util.LocationManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CityTailorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val locationManager = remember { LocationManager(context) }
                    
                    LocationPermissionHandler(locationManager = locationManager) {
                        ContentView()
                    }
                }
            }
        }
    }
}