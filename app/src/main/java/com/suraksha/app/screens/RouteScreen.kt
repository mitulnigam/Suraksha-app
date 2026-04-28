package com.suraksha.app.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import com.suraksha.app.ui.theme.AccentBlue
import com.suraksha.app.ui.theme.UrgentRed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import com.google.android.gms.maps.CameraUpdateFactory
import com.suraksha.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteScreen(viewModel: RouteViewModel = viewModel()) {
    val context = LocalContext.current
    val routeState by viewModel.routeState.collectAsState()

    var startInput by remember { mutableStateOf("") }
    var destInput by remember { mutableStateOf("") }
    var isStartSearching by remember { mutableStateOf(false) }
    var isDestSearching by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(20.5937, 78.9629), 5f)
    }

    // Auto-center camera on user during navigation
    LaunchedEffect(routeState.currentLocation) {
        if (routeState.isNavigating && routeState.currentLocation != null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.builder()
                        .target(routeState.currentLocation!!)
                        .zoom(17f)
                        .bearing(0f)
                        .tilt(45f)
                        .build()
                )
            )
        }
    }

    // Initial zoom to fit route
    LaunchedEffect(routeState.routePoints) {
        if (!routeState.isNavigating && routeState.routePoints.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.builder()
            routeState.routePoints.forEach { boundsBuilder.include(it) }
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 150))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header Card (Hide during navigation to maximize map)
            AnimatedVisibility(
                visible = !routeState.isNavigating,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Safety Navigator", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Start Location
                        Box {
                            OutlinedTextField(
                                value = startInput,
                                onValueChange = { startInput = it; isStartSearching = true; viewModel.searchPlaces(it) },
                                label = { Text("Start") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color.Cyan) }
                            )
                            if (isStartSearching && routeState.placeSuggestions.isNotEmpty()) {
                                DropdownMenu(expanded = true, onDismissRequest = { isStartSearching = false }, properties = PopupProperties(focusable = false)) {
                            routeState.placeSuggestions.forEachIndexed { index, suggestion ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Place,
                                                contentDescription = null,
                                                tint = AccentBlue,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = suggestion.name,
                                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                                    color = Color.White
                                                )
                                                if (suggestion.address.isNotEmpty()) {
                                                    Text(
                                                        text = suggestion.address,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color.Gray,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    onClick = {
                                        startInput = suggestion.name
                                        viewModel.setStartLocation(suggestion.latLng)
                                        isStartSearching = false
                                    }
                                )
                                if (index < routeState.placeSuggestions.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        thickness = 0.5.dp,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Destination
                        Box {
                            OutlinedTextField(
                                value = destInput,
                                onValueChange = { destInput = it; isDestSearching = true; viewModel.searchPlaces(it) },
                                label = { Text("Destination") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null, tint = UrgentRed) }
                            )
                            if (isDestSearching && routeState.placeSuggestions.isNotEmpty()) {
                                DropdownMenu(expanded = true, onDismissRequest = { isDestSearching = false }, properties = PopupProperties(focusable = false)) {
                            routeState.placeSuggestions.forEachIndexed { index, suggestion ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Place,
                                                contentDescription = null,
                                                tint = UrgentRed,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = suggestion.name,
                                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                                    color = Color.White
                                                )
                                                if (suggestion.address.isNotEmpty()) {
                                                    Text(
                                                        text = suggestion.address,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color.Gray,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    onClick = {
                                        destInput = suggestion.name
                                        viewModel.setDestination(suggestion.latLng)
                                        isDestSearching = false
                                    }
                                )
                                if (index < routeState.placeSuggestions.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        thickness = 0.5.dp,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Spacer(modifier = Modifier.height(4.dp))

                        // Route mode selector — full width, card style
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            horizontalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            // Safest button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp, topEnd = 0.dp, bottomEnd = 0.dp))
                                    .background(
                                        if (routeState.isSafest)
                                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                                listOf(Color(0xFF1B5E20), Color(0xFF2E7D32))
                                            )
                                        else
                                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                                listOf(Color.Transparent, Color.Transparent)
                                            )
                                    )
                                    .clickable { if (!routeState.isSafest) viewModel.toggleRouteType() },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = if (routeState.isSafest) Color(0xFF69F0AE) else Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "Safest",
                                            fontSize = 13.sp,
                                            fontWeight = if (routeState.isSafest) FontWeight.Bold else FontWeight.Normal,
                                            color = if (routeState.isSafest) Color(0xFF69F0AE) else Color.Gray
                                        )
                                        if (routeState.isSafest) {
                                            Text(
                                                "Avoids risk zones",
                                                fontSize = 9.sp,
                                                color = Color(0xFF69F0AE).copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }

                            // Divider
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            )

                            // Shortest button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 16.dp, bottomEnd = 16.dp))
                                    .background(
                                        if (!routeState.isSafest)
                                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                                listOf(Color(0xFF0D2A3D), Color(0xFF01579B))
                                            )
                                        else
                                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                                listOf(Color.Transparent, Color.Transparent)
                                            )
                                    )
                                    .clickable { if (routeState.isSafest) viewModel.toggleRouteType() },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = if (!routeState.isSafest) Color.Cyan else Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "Shortest",
                                            fontSize = 13.sp,
                                            fontWeight = if (!routeState.isSafest) FontWeight.Bold else FontWeight.Normal,
                                            color = if (!routeState.isSafest) Color.Cyan else Color.Gray
                                        )
                                        if (!routeState.isSafest) {
                                            Text(
                                                "Fastest path",
                                                fontSize = 9.sp,
                                                color = Color.Cyan.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Find Route button — full width below the selector
                        Button(
                            onClick = { viewModel.calculateRoute() },
                            enabled = !routeState.isLoading && routeState.startLocation != null && routeState.destination != null,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                        ) {
                            if (routeState.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                                Spacer(Modifier.width(8.dp))
                                Text("Finding Route…", color = Color.White, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Find Route", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }
            }

            // Map Area
            Box(modifier = Modifier.weight(1f).padding(if (routeState.isNavigating) 0.dp else 16.dp).clip(if (routeState.isNavigating) RoundedCornerShape(0.dp) else RoundedCornerShape(24.dp))) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false),
                    properties = MapProperties(mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style))
                ) {
                    if (routeState.routePoints.isNotEmpty()) {
                        Polyline(points = routeState.routePoints, color = if (routeState.isSafest) Color.Green else Color.Cyan, width = 20f, jointType = JointType.ROUND)
                        Marker(state = MarkerState(position = routeState.routePoints.first()), icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE), title = "Start")
                        Marker(state = MarkerState(position = routeState.routePoints.last()), icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED), title = "End")
                        
                        routeState.highRiskZones.forEach { zone ->
                            Circle(center = zone, radius = 300.0, fillColor = Color.Red.copy(alpha = 0.2f), strokeColor = Color.Red, strokeWidth = 2f)
                        }
                    }
                    
                    // User Live Location Marker
                    routeState.currentLocation?.let {
                        Marker(
                            state = MarkerState(position = it),
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE),
                            title = "You",
                            rotation = 0f,
                            flat = true
                        )
                    }
                }

                // Overlay Controls
                if (routeState.routePoints.isNotEmpty() && !routeState.isNavigating) {
                    Box(modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp)) {
                        Button(
                            onClick = { viewModel.startNavigation() },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                        ) {
                            Icon(Icons.Default.Navigation, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Start Navigation", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            }
        }

        // Navigation Mode Overlay
        AnimatedVisibility(
            visible = routeState.isNavigating,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            Column(modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.8f)).padding(16.dp).statusBarsPadding()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = if (routeState.currentRisk > 0.7) UrgentRed else Color.Green)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Live Safety Monitor", color = Color.White, fontSize = 14.sp)
                        Text(
                            if (routeState.currentRisk > 0.7) "DANGER: HIGH RISK AREA" else "SAFETY SECURED",
                            color = if (routeState.currentRisk > 0.7) UrgentRed else Color.Green,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { viewModel.stopNavigation() }) {
                        Icon(Icons.Default.Close, contentDescription = "Exit", tint = Color.White)
                    }
                }
                
                if (routeState.currentRisk > 0.7) {
                    Text(
                        "⚠️ This area has high crime/accident rates. Stay alert!",
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                LinearProgressIndicator(
                    progress = routeState.currentRisk.toFloat(),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = if (routeState.currentRisk > 0.7) UrgentRed else Color.Green,
                    trackColor = Color.DarkGray
                )
            }
        }
    }
}
