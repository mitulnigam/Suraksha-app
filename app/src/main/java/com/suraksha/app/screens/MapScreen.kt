package com.suraksha.app.screens

import android.Manifest
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.suraksha.app.ui.theme.AccentBlue
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus
import org.osmdroid.views.overlay.OverlayItem

@Composable
fun MapScreen(viewModel: MapViewModel = viewModel()) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapState by viewModel.mapState.collectAsState()

    var hasRequestedPermissions by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            android.util.Log.d("MapScreen", "✅ Location permission granted - triggering refresh")
            viewModel.refreshMap()
        } else {
            android.util.Log.w("MapScreen", "⚠️ Location permission denied")
        }
    }

    LaunchedEffect(Unit) {
        android.util.Log.d("MapScreen", "🔄 MapScreen entered")
        if (!hasRequestedPermissions) {
            hasRequestedPermissions = true
            android.util.Log.d("MapScreen", "📋 Requesting location permissions...")
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            android.util.Log.d("MapScreen", "🔄 Refreshing map data...")
            viewModel.refreshMap()
        }
    }

    // Configure OSMDroid user agent
    Configuration.getInstance().userAgentValue = context.packageName

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(15.0)
            controller.setCenter(GeoPoint(20.5937, 78.9629)) // Default India
            
            // Apply dark mode filter to match app theme
            val inverseMatrix = ColorMatrix(
                floatArrayOf(
                    -1.0f, 0.0f, 0.0f, 0.0f, 255f,
                    0.0f, -1.0f, 0.0f, 0.0f, 255f,
                    0.0f, 0.0f, -1.0f, 0.0f, 255f,
                    0.0f, 0.0f, 0.0f, 1.0f, 0.0f
                )
            )
            val destinationColor = Color.parseColor("#FF2A2A2A")
            val lr = (255.0f - Color.red(destinationColor))
            val lg = (255.0f - Color.green(destinationColor))
            val lb = (255.0f - Color.blue(destinationColor))
            val modifyMatrix = ColorMatrix(
                floatArrayOf(
                    lr / 255.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                    0.0f, lg / 255.0f, 0.0f, 0.0f, 0.0f,
                    0.0f, 0.0f, lb / 255.0f, 0.0f, 0.0f,
                    0.0f, 0.0f, 0.0f, 1.0f, 0.0f
                )
            )
            modifyMatrix.preConcat(inverseMatrix)
            overlayManager.tilesOverlay.setColorFilter(ColorMatrixColorFilter(modifyMatrix))
        }
    }

    // Handle MapView lifecycle
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    // Update markers and camera
    LaunchedEffect(mapState) {
        mapView.overlays.clear()
        
        val items = ArrayList<OverlayItem>()

        // User Location Marker
        mapState.userLocation?.let { userLocation ->
            val userMarker = OverlayItem("You", "Current Location", userLocation)
            val userIcon = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_mylocation)?.mutate()
            userIcon?.setTint(Color.CYAN)
            userMarker.setMarker(userIcon)
            items.add(userMarker)

            // Center camera on user
            mapView.controller.animateTo(userLocation)
        }

        // Safe Haven Markers
        mapState.safeHavens.forEach { haven ->
            val title = if (haven.type == SafeHavenType.POLICE) "🚓 Police: ${haven.name}" else "🏥 Hospital: ${haven.name}"
            val marker = OverlayItem(title, haven.address, haven.location)
            
            val icon = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_myplaces)?.mutate()
            if (haven.type == SafeHavenType.POLICE) {
                icon?.setTint(Color.RED)
            } else {
                icon?.setTint(Color.GREEN)
            }
            marker.setMarker(icon)
            items.add(marker)
        }

        if (items.isNotEmpty()) {
            val mOverlay = ItemizedOverlayWithFocus(
                items,
                object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
                    override fun onItemSingleTapUp(index: Int, item: OverlayItem): Boolean {
                        return true
                    }
                    override fun onItemLongPress(index: Int, item: OverlayItem): Boolean {
                        return false
                    }
                }, context
            )
            mOverlay.setFocusItemsOnTap(true)
            mapView.overlays.add(mOverlay)
        }
        
        mapView.invalidate()
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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f))
                .padding(8.dp)
        ) {
            val policeCount = mapState.safeHavens.count { it.type == SafeHavenType.POLICE }
            val hospitalCount = mapState.safeHavens.count { it.type == SafeHavenType.HOSPITAL }

            val summaryText = if (mapState.safeHavens.isEmpty() && !mapState.isLoading) {
                "Showing your location • Safe havens search unavailable"
            } else if (mapState.isLoading) {
                "Loading safe havens..."
            } else {
                "Found: $policeCount Police Stations, $hospitalCount Hospitals (within 5km)"
            }

            Text(
                text = summaryText,
                style = MaterialTheme.typography.bodySmall,
                color = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(androidx.compose.ui.graphics.Color.Cyan)
                            .padding(6.dp)
                    )
                    Text(
                        text = "You",
                        style = MaterialTheme.typography.bodySmall,
                        color = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(androidx.compose.ui.graphics.Color.Red)
                            .padding(6.dp)
                    )
                    Text(
                        text = "Police Station",
                        style = MaterialTheme.typography.bodySmall,
                        color = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(androidx.compose.ui.graphics.Color.Green)
                            .padding(6.dp)
                    )
                    Text(
                        text = "Hospital",
                        style = MaterialTheme.typography.bodySmall,
                        color = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(androidx.compose.ui.graphics.Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { mapView },
                modifier = Modifier.fillMaxSize()
            )

            if (mapState.isLoading) {
                CircularProgressIndicator(color = AccentBlue)
            }
        }
    }
}