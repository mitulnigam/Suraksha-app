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
    private var locationCollectionJob: kotlinx.coroutines.Job? = null

    init {
        startLocationUpdates()
    }

    
    fun refreshMap() {
        Log.d("MapViewModel", "ðŸ”„ Refreshing map data...")

        _mapState.value = MapState(isLoading = true)
        hasSearched = false

        locationCollectionJob?.cancel()

        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        locationCollectionJob = viewModelScope.launch {
            locationManager.getLocationUpdates(getApplication())
                .catch { e ->
                    Log.e("MapViewModel", "âŒ Error getting location updates: ${e.message}")
                    _mapState.value = _mapState.value.copy(isLoading = false)
                }
                .collect { location ->
                    val newLatLng = LatLng(location.latitude, location.longitude)
                    Log.d("MapViewModel", "ðŸ“ Location update: $newLatLng")
                    _mapState.value = _mapState.value.copy(userLocation = newLatLng, isLoading = false)

                    if (!hasSearched) {
                        hasSearched = true
                        Log.d("MapViewModel", "ðŸ” Starting search for safe havens at location: $newLatLng")

                        searchForSafeHavens(newLatLng, "police station near me", SafeHavenType.POLICE)
                        searchForSafeHavens(newLatLng, "hospital near me", SafeHavenType.HOSPITAL)
                    }
                }
        }
    }

    private fun searchForSafeHavens(location: LatLng, typeQuery: String, havenType: SafeHavenType) {
        val context = getApplication<Application>().applicationContext

        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w("MapViewModel", "Location permission denied, cannot find safe havens.")
            return
        }

        val client = placesClient
        if (client == null) {
            Log.e("MapViewModel", "âš ï¸ PlacesClient is null. Safe havens search disabled. Map will still show your location.")

            return
        }

        try {
            val placeFields = listOf(Field.ID, Field.NAME, Field.LAT_LNG, Field.ADDRESS)
            val locationRestriction = CircularBounds.newInstance(location, 5000.0)

            Log.d("MapViewModel", "ðŸ” Searching for '$typeQuery' near location: $location with 5km radius")

            val searchByTextRequest = SearchByTextRequest.builder(typeQuery, placeFields)
                .setLocationRestriction(locationRestriction)
                .setMaxResultCount(20)
                .setRankPreference(SearchByTextRequest.RankPreference.DISTANCE)
                .build()

            client.searchByText(searchByTextRequest).addOnSuccessListener { response ->
                Log.d("MapViewModel", "âœ… Search response received for $typeQuery. Total places: ${response.places.size}")

                val newHavens = response.places.mapNotNull { place ->
                    try {
                        if (place.latLng != null && place.id != null && place.name != null) {
                            val address = place.address ?: "Address not available"
                            Log.d("MapViewModel", "ðŸ“ Place: ${place.name} at ${place.latLng}")
                            SafeHaven(
                                id = place.id!!,
                                name = place.name!!,
                                address = address,
                                location = place.latLng!!,
                                type = havenType
                            )
                        } else {
                            Log.w("MapViewModel", "âš ï¸ Place missing required fields: id=${place.id}, name=${place.name}, latLng=${place.latLng}")
                            null
                        }
                    } catch (e: Exception) {
                        Log.e("MapViewModel", "âŒ Error processing place: ${e.message}", e)
                        null
                    }
                }

                Log.d("MapViewModel", "âœ… Successfully processed ${newHavens.size} havens of type: $typeQuery")

                if (newHavens.isNotEmpty()) {
                    newHavens.forEach { haven ->
                        Log.d("MapViewModel", "  - ${haven.name} at ${haven.location}")
                    }
                }

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
                Log.e("MapViewModel", "âŒ Failed to search by text for $typeQuery: ${exception.message}", exception)
                Log.e("MapViewModel", "Map will still show your location without safe havens")

                _mapState.value = _mapState.value.copy(isLoading = false)
            }
        } catch (e: Exception) {
            Log.e("MapViewModel", "âŒ Exception while searching for safe havens: ${e.message}", e)
            Log.e("MapViewModel", "Map will still show your location without safe havens")

            _mapState.value = _mapState.value.copy(isLoading = false)
        }
    }
}