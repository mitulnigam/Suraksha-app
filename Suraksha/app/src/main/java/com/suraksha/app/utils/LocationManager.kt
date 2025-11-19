package com.suraksha.app.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Manages fetching the user's current location.
 *
 * This new version supports:
 * 1. `getCurrentLocation()`: A one-time request for a fresh location (for SOS alerts).
 * 2. `getLocationUpdates()`: A continuous flow of location updates (for the Map screen).
 */
class LocationManager {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // --- This is the new function for Phase 3 ---
    /**
     * A continuous flow of location updates for the map.
     */
    @SuppressLint("MissingPermission")
    fun getLocationUpdates(context: Context): Flow<Location> = callbackFlow {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        // 1. Check for permissions (same as before)
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("LocationManager", "Location permission not granted for flow. Closing flow.")
            close() // Close the flow if permissions are missing
            return@callbackFlow
        }

        // 2. Define the location request
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000L // 10 seconds
        ).build()

        // 3. Define the callback
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let {
                    Log.d("LocationManager", "Flow: New location update: $it")
                    trySend(it) // Send the new location to the flow
                }
            }
        }

        // 4. Start listening for updates
        Log.d("LocationManager", "Starting location updates for map...")
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        // 5. This block is called when the flow is 'closed' or 'cancelled'
        awaitClose {
            Log.d("LocationManager", "Stopping location updates for map.")
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }


    // --- This is your existing function from Phase 2 (for SOS/Shake) ---
    /**
     * Gets a single, fresh, one-time location for alerts.
     */
    @SuppressLint("MissingPermission")
    fun getCurrentLocation(
        context: Context,
        onLocationFetched: (location: Location) -> Unit
    ) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("LocationManager", "Location permission not granted. Cannot get location.")
            onLocationFetched(Location("NoPermissionProvider").apply {
                latitude = 0.0
                longitude = 0.0
            })
            return
        }

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener { location: Location? ->
            if (location != null) {
                Log.d("LocationManager", "Successfully fetched fresh location: $location")
                onLocationFetched(location)
            } else {
                Log.w("LocationManager", "Fresh location is null. Trying last known location.")
                getLastKnownLocation(context, onLocationFetched)
            }
        }.addOnFailureListener { e ->
            Log.e("LocationManager", "Failed to get fresh location: ${e.message}. Trying last known location.")
            getLastKnownLocation(context, onLocationFetched)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation(
        context: Context,
        onLocationFetched: (location: Location) -> Unit
    ) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                Log.d("LocationManager", "Using last known location: $location")
                onLocationFetched(location)
            } else {
                Log.e("LocationManager", "CRITICAL: Both fresh and last known location are null.")
                onLocationFetched(Location("FallbackProvider").apply {
                    latitude = 0.0
                    longitude = 0.0
                })
            }
        }.addOnFailureListener { e ->
            Log.e("LocationManager", "Failed to get last known location: ${e.message}")
            onLocationFetched(Location("FallbackProvider").apply {
                latitude = 0.0
                longitude = 0.0
            })
        }
    }
}