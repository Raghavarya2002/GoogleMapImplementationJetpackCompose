package com.example.googlemapimplement
import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import com.google.maps.android.SphericalUtil
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.googlemapimplement.ui.theme.GoogleMapImplementTheme
import com.google.android.gms.maps.model.LatLng




class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GoogleMapImplementTheme {
                // This is the main Composable function where our map UI is set up
                MapScreen()
            }
        }
    }
}

@Composable
fun MapScreen() {
    // Getting the context of the current activity
    val context = LocalContext.current

    // Creating a fusedLocationClient to get the device's last known location
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    // Initializing mutable state to hold the user's current location, initially null
    val currentLocation = remember { mutableStateOf<LatLng?>(null) }

    // Setting a fixed location for Raj Medicos in Sonipat
    val rajMedicos = LatLng(28.9967, 77.0199)

    // Variable to check if the app has the necessary location permission
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Launcher for the permission request that is triggered when needed
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            // Update the permission status once the user responds to the permission request
            hasLocationPermission = granted
        }
    )

    // Request permission if it's not already granted
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Column layout to organize the button and map components
    Column(modifier = Modifier.fillMaxSize()) {
        // Button to get the current location and calculate the distance
        Button(
            modifier = Modifier
                .padding(16.dp) // Adding padding around the button
                .align(Alignment.CenterHorizontally), // Aligning button to the center horizontally
            onClick = {
                if (hasLocationPermission) {
                    // If permission is granted, get the last known location from the device
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            // Update the state with the current location (latitude, longitude)
                            currentLocation.value = LatLng(it.latitude, it.longitude)

                            // Calculate the distance from the current location to Raj Medicos using SphericalUtil
                            val distance = SphericalUtil.computeDistanceBetween(currentLocation.value!!, rajMedicos)

                            // Show a Toast message displaying the calculated distance
                            Toast.makeText(
                                context,
                                "Distance: ${"%.2f".format(distance)} meters",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } else {
                    // If permission is not granted, show a message informing the user
                    Toast.makeText(context, "Location Permission Required", Toast.LENGTH_LONG).show()
                }
            }
        ) {
            Text(text = "Get Distance to Raj Medicos") // Button label
        }

        // State for the camera position of the Google Map (focused on Raj Medicos initially)
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(rajMedicos, 17f) // Zoom level 17 for close view
        }

        // Google Map composable to display the map and add markers and polylines
        GoogleMap(
            modifier = Modifier.fillMaxSize(), // Make map take up the entire screen
            cameraPositionState = cameraPositionState, // Set the camera's initial position
            uiSettings = MapUiSettings(zoomControlsEnabled = true), // Enable zoom controls on the map
            properties = MapProperties(
                mapType = MapType.HYBRID // Set the map type to hybrid (satellite with labels)
            )
        ) {

            // Marker for Raj Medicos on the map
            Marker(
                state = MarkerState(position = rajMedicos), // Position the marker at Raj Medicos
                title = "Raj Medicos", // Title for the marker
                snippet = "Sonipat" // Additional information for the marker
            )

            // If current location is available, draw a polyline to Raj Medicos
            currentLocation.value?.let { currentLatLng ->
                Polyline(
                    points = listOf(currentLatLng, rajMedicos), // Define the polyline's start and end points
                    color = Color.Blue, // Set the color of the polyline
                    width = 5f // Set the width of the polyline
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    GoogleMapImplementTheme {
        MapScreen() // Preview the map screen with mock data
    }
}
