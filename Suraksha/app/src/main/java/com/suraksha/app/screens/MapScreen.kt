package com.suraksha.app.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.suraksha.app.R
import com.suraksha.app.ui.theme.AccentBlue

private const val DARK_MAP_STYLE_JSON = """
[
  { "elementType": "geometry", "stylers": [ { "color": "#212121" } ] },
  { "elementType": "labels.icon", "stylers": [ { "visibility": "off" } ] },
  { "elementType": "labels.text.fill", "stylers": [ { "color": "#757575" } ] },
  { "elementType": "labels.text.stroke", "stylers": [ { "color": "#212121" } ] },
  { "featureType": "administrative", "elementType": "geometry", "stylers": [ { "color": "#757575" } ] },
  { "featureType": "administrative.country", "elementType": "labels.text.fill", "stylers": [ { "color": "#9e9e9e" } ] },
  { "featureType": "administrative.land_parcel", "stylers": [ { "visibility": "off" } ] },
  { "featureType": "administrative.locality", "elementType": "labels.text.fill", "stylers": [ { "color": "#bdbdbd" } ] },
  { "featureType": "poi", "elementType": "labels.text.fill", "stylers": [ { "color": "#757575" } ] },
  { "featureType": "poi.park", "elementType": "geometry", "stylers": [ { "color": "#181818" } ] },
  { "featureType": "poi.park", "elementType": "labels.text.fill", "stylers": [ { "color": "#616161" } ] },
  { "featureType": "poi.park", "elementType": "labels.text.stroke", "stylers": [ { "color": "#1e1e1e" } ] },
  { "featureType": "road", "elementType": "geometry.fill", "stylers": [ { "color": "#2c2c2c" } ] },
  { "featureType": "road", "elementType": "labels.text.fill", "stylers": [ { "color": "#8a8a8a" } ] },
  { "featureType": "road.arterial", "elementType": "geometry", "stylers": [ { "color": "#373737" } ] },
  { "featureType": "road.highway", "elementType": "geometry", "stylers": [ { "color": "#3c3c3c" } ] },
  { "featureType": "road.highway.controlled_access", "elementType": "geometry", "stylers": [ { "color": "#4e4e4e" } ] },
  { "featureType": "road.local", "elementType": "labels.text.fill", "stylers": [ { "color": "#616161" } ] },
  { "featureType": "transit", "elementType": "labels.text.fill", "stylers": [ { "color": "#757575" } ] },
  { "featureType": "water", "elementType": "geometry", "stylers": [ { "color": "#000000" } ] },
  { "featureType": "water", "elementType": "labels.text.fill", "stylers": [ { "color": "#3d3d3d" } ] }
]
"""

@Composable
fun MapScreen(viewModel: MapViewModel = viewModel()) {

    val context = LocalContext.current
    val mapState by viewModel.mapState.collectAsState()

    // Request location permission
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            android.util.Log.d("MapScreen", "✅ Location permission granted")
        } else {
            android.util.Log.w("MapScreen", "⚠️ Location permission denied")
        }
    }

    // Request permissions on first load
    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(20.5937, 78.9629), 4f)
    }

    LaunchedEffect(mapState.userLocation) {
        mapState.userLocation?.let {
            android.util.Log.d("MapScreen", "Moving camera to user location: $it")
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(it, 15f),
                durationMs = 1500
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Map",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Text(
            text = "Safe Havens",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Summary and Legend
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(8.dp)
        ) {
            // Summary text
            val policeCount = mapState.safeHavens.count { it.type == SafeHavenType.POLICE }
            val hospitalCount = mapState.safeHavens.count { it.type == SafeHavenType.HOSPITAL }
            Text(
                text = "Found: $policeCount Police Stations, $hospitalCount Hospitals (within 5km)",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Legend showing marker colors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(Color.Cyan)
                            .padding(6.dp)
                    )
                    Text(
                        text = "You",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(Color.Red)
                            .padding(6.dp)
                    )
                    Text(
                        text = "Police Station",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(Color.Green)
                            .padding(6.dp)
                    )
                    Text(
                        text = "Hospital",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = true,
                    mapStyleOptions = MapStyleOptions(DARK_MAP_STYLE_JSON)
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    mapToolbarEnabled = false,
                    myLocationButtonEnabled = true
                )
            ) {
                mapState.userLocation?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "Your Location",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)
                    )
                }

                mapState.safeHavens.forEach { haven ->
                    val markerColor = if (haven.type == SafeHavenType.POLICE) {
                        BitmapDescriptorFactory.HUE_RED
                    } else {
                        BitmapDescriptorFactory.HUE_GREEN
                    }

                    val markerTitle = if (haven.type == SafeHavenType.POLICE) {
                        "🚓 Police Station: ${haven.name}"
                    } else {
                        "🏥 Hospital: ${haven.name}"
                    }

                    Marker(
                        state = MarkerState(position = haven.location),
                        title = markerTitle,
                        snippet = haven.address,
                        icon = BitmapDescriptorFactory.defaultMarker(markerColor)
                    )
                }
            }

            if (mapState.isLoading) {
                CircularProgressIndicator(color = AccentBlue)
            }
        }
    }
}