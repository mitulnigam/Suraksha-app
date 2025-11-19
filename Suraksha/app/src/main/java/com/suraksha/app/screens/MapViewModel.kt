package com.suraksha.app.screens

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place.Field
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextRequest
import com.suraksha.app.utils.LocationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class MapState(
    val userLocation: LatLng? = null,
    val safeHavens: List<SafeHaven> = emptyList(),
    val isLoading: Boolean = true
)

data class SafeHaven(
    val id: String,
    val name: String,
    val address: String,
    val location: LatLng,
    val type: SafeHavenType
)

enum class SafeHavenType {
    POLICE,
    HOSPITAL
}

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val locationManager = LocationManager()
    private val placesClient: PlacesClient? by lazy {
        try {
            if (Places.isInitialized()) {
                Places.createClient(application)
            } else {
                Log.e("MapViewModel", "Places SDK is not initialized. Cannot create PlacesClient.")
                null
            }
        } catch (e: Exception) {
            Log.e("MapViewModel", "Failed to create PlacesClient: ${e.message}", e)
            null
        }
    }

    private val _mapState = MutableStateFlow(MapState())
    val mapState: StateFlow<MapState> = _mapState.asStateFlow()

    private var hasSearched = false

    init {
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        viewModelScope.launch {
            locationManager.getLocationUpdates(getApplication())
                .catch { e ->
                    Log.e("MapViewModel", "Error getting location updates: ${e.message}")
                    _mapState.value = _mapState.value.copy(isLoading = false)
                }
                .collect { location ->
                    val newLatLng = LatLng(location.latitude, location.longitude)
                    _mapState.value = _mapState.value.copy(userLocation = newLatLng, isLoading = false)

                    if (!hasSearched) {
                        hasSearched = true
                        Log.d("MapViewModel", "Starting search for safe havens at location: $newLatLng")
                        // Search for police stations and hospitals
                        searchForSafeHavens(newLatLng, "police station near me", SafeHavenType.POLICE)
                        searchForSafeHavens(newLatLng, "hospital near me", SafeHavenType.HOSPITAL)
                    }
                }
        }
    }

    private fun searchForSafeHavens(location: LatLng, typeQuery: String, havenType: SafeHavenType) {
        val context = getApplication<Application>().applicationContext
        
        // Check for location permissions
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w("MapViewModel", "Location permission denied, cannot find safe havens.")
            return
        }

        // Check if PlacesClient is available
        val client = placesClient
        if (client == null) {
            Log.e("MapViewModel", "PlacesClient is null. Places SDK may not be initialized.")
            _mapState.value = _mapState.value.copy(isLoading = false)
            return
        }

        try {
            val placeFields = listOf(Field.ID, Field.NAME, Field.LAT_LNG, Field.ADDRESS)
            val locationRestriction = CircularBounds.newInstance(location, 5000.0) // 5km radius

            Log.d("MapViewModel", "Searching for '$typeQuery' near location: $location with 5km radius")

            val searchByTextRequest = SearchByTextRequest.builder(typeQuery, placeFields)
                .setLocationRestriction(locationRestriction)
                .setMaxResultCount(20)
                .setRankPreference(SearchByTextRequest.RankPreference.DISTANCE) // Prioritize by distance
                .build()

            client.searchByText(searchByTextRequest).addOnSuccessListener { response ->
                Log.d("MapViewModel", "Search response received for $typeQuery. Total places: ${response.places.size}")
                
                val newHavens = response.places.mapNotNull { place ->
                    try {
                        if (place.latLng != null && place.id != null && place.name != null) {
                            val address = place.address ?: "Address not available"
                            Log.d("MapViewModel", "Place: ${place.name} at ${place.latLng}")
                            SafeHaven(
                                id = place.id!!,
                                name = place.name!!,
                                address = address,
                                location = place.latLng!!,
                                type = havenType
                            )
                        } else {
                            Log.w("MapViewModel", "Place missing required fields: id=${place.id}, name=${place.name}, latLng=${place.latLng}")
                            null
                        }
                    } catch (e: Exception) {
                        Log.e("MapViewModel", "Error processing place: ${e.message}", e)
                        null
                    }
                }

                Log.d("MapViewModel", "Successfully processed ${newHavens.size} havens of type: $typeQuery")
                
                if (newHavens.isNotEmpty()) {
                    newHavens.forEach { haven ->
                        Log.d("MapViewModel", "  - ${haven.name} at ${haven.location}")
                    }
                }

                // Update state with new havens, avoiding duplicates by ID
                val currentHavens = _mapState.value.safeHavens
                val existingIds = currentHavens.map { it.id }.toSet()
                val uniqueNewHavens = newHavens.filter { it.id !in existingIds }
                
                if (uniqueNewHavens.isNotEmpty()) {
                    _mapState.value = _mapState.value.copy(
                        safeHavens = currentHavens + uniqueNewHavens,
                        isLoading = false
                    )
                    Log.d("MapViewModel", "Added ${uniqueNewHavens.size} new unique havens (${newHavens.size - uniqueNewHavens.size} duplicates filtered)")
                } else {
                    _mapState.value = _mapState.value.copy(isLoading = false)
                    Log.d("MapViewModel", "No new unique havens to add (all were duplicates)")
                }

            }.addOnFailureListener { exception: Exception ->
                Log.e("MapViewModel", "Failed to search by text for $typeQuery: ${exception.message}", exception)
                Log.e("MapViewModel", "Exception details: ${exception.stackTraceToString()}")
                _mapState.value = _mapState.value.copy(isLoading = false)
            }
        } catch (e: Exception) {
            Log.e("MapViewModel", "Exception while searching for safe havens: ${e.message}", e)
            Log.e("MapViewModel", "Exception details: ${e.stackTraceToString()}")
            _mapState.value = _mapState.value.copy(isLoading = false)
        }
    }
}