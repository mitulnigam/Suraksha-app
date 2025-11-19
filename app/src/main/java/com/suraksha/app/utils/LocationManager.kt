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


class LocationManager {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    
    @SuppressLint("MissingPermission")
    fun getLocationUpdates(context: Context): Flow<Location> = callbackFlow {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("LocationManager", "Location permission not granted for flow. Closing flow.")
            close()
            return@callbackFlow
        }

        // Try to get last known location immediately as a fallback
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    Log.d("LocationManager", "Flow: Got last known location immediately: $it")
                    trySend(it)
                }
            }
        } catch (e: Exception) {
            Log.w("LocationManager", "Could not get last known location: ${e.message}")
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L  // Reduced interval for faster updates
        )
            .setMinUpdateIntervalMillis(2000L)  // Minimum update interval
            .setMaxUpdateDelayMillis(10000L)    // Maximum delay
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let {
                    Log.d("LocationManager", "Flow: New location update: $it")
                    trySend(it)
                }
            }
        }

        Log.d("LocationManager", "Starting location updates for map...")
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        awaitClose {
            Log.d("LocationManager", "Stopping location updates for map.")
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    
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