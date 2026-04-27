package com.suraksha.app.screens

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import org.osmdroid.util.GeoPoint
import com.suraksha.app.utils.LocationManager
import com.suraksha.app.BuildConfig
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.suraksha.app.data.CommunityAlert
import com.suraksha.app.data.CommunityAlertType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

data class MapState(
    val userLocation: GeoPoint? = null,
    val safeHavens: List<SafeHaven> = emptyList(),
    val communityAlerts: List<CommunityAlert> = emptyList(),
    val isLoading: Boolean = true
)

data class SafeHaven(
    val id: String,
    val name: String,
    val address: String,
    val location: GeoPoint,
    val type: SafeHavenType
)

enum class SafeHavenType {
    POLICE,
    HOSPITAL
}

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val locationManager = LocationManager()
    private val db = FirebaseFirestore.getInstance()
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val _mapState = MutableStateFlow(MapState())
    val mapState: StateFlow<MapState> = _mapState.asStateFlow()

    private var hasSearched = false
    private var locationCollectionJob: kotlinx.coroutines.Job? = null

    init {
        startLocationUpdates()
        listenForCommunityAlerts()
    }

    private fun listenForCommunityAlerts() {
        db.collection("community_alerts")
            .whereGreaterThan("expiresAt", Timestamp.now())
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("MapViewModel", "Firestore listen failed", e)
                    return@addSnapshotListener
                }
                
                val alerts = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val typeStr = doc.getString("type") ?: "SUSPICIOUS"
                        val loc = doc.getGeoPoint("location") ?: return@mapNotNull null
                        CommunityAlert(
                            id = doc.id,
                            type = CommunityAlertType.valueOf(typeStr),
                            description = doc.getString("description") ?: "",
                            location = loc,
                            userName = doc.getString("userName") ?: "Anonymous",
                            timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now()
                        )
                    } catch (ex: Exception) { null }
                } ?: emptyList()
                
                _mapState.value = _mapState.value.copy(communityAlerts = alerts)
                Log.d("MapViewModel", "🔔 Received ${alerts.size} active community alerts")
            }
    }

    fun refreshMap() {
        Log.d("MapViewModel", "🔄 Refreshing map data...")
        _mapState.value = _mapState.value.copy(isLoading = true)
        hasSearched = false
        locationCollectionJob?.cancel()
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        locationCollectionJob = viewModelScope.launch {
            locationManager.getLocationUpdates(getApplication())
                .catch { e ->
                    Log.e("MapViewModel", "❌ Error getting location updates: ${e.message}")
                    _mapState.value = _mapState.value.copy(isLoading = false)
                }
                .collect { location ->
                    val newGeoPoint = GeoPoint(location.latitude, location.longitude)
                    _mapState.value = _mapState.value.copy(userLocation = newGeoPoint, isLoading = false)

                    if (!hasSearched) {
                        hasSearched = true
                        searchGooglePlacesSafeHavens(newGeoPoint)
                    }
                }
        }
    }

    private fun searchGooglePlacesSafeHavens(location: GeoPoint) {
        val apiKey = BuildConfig.MAPS_API_KEY
        if (apiKey.isEmpty()) {
            _mapState.value = _mapState.value.copy(isLoading = false)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _mapState.value = _mapState.value.copy(isLoading = true)
                
                val hospitals = fetchPlacesFromGoogle(location, "hospital", apiKey)
                val police = fetchPlacesFromGoogle(location, "police", apiKey)
                
                val allHavens = hospitals + police
                
                withContext(Dispatchers.Main) {
                    _mapState.value = _mapState.value.copy(
                        safeHavens = allHavens,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _mapState.value = _mapState.value.copy(isLoading = false)
                }
            }
        }
    }

    private suspend fun fetchPlacesFromGoogle(location: GeoPoint, type: String, apiKey: String): List<SafeHaven> {
        val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=${location.latitude},${location.longitude}" +
                "&radius=5000" +
                "&type=$type" +
                "&key=$apiKey"

        val request = Request.Builder().url(url).build()
        val response = httpClient.newCall(request).execute()
        
        if (!response.isSuccessful) return emptyList()

        val body = response.body?.string() ?: return emptyList()
        return parseGooglePlacesResponse(body, type)
    }

    private fun parseGooglePlacesResponse(jsonBody: String, typeStr: String): List<SafeHaven> {
        val havens = mutableListOf<SafeHaven>()
        val jsonObject = JSONObject(jsonBody)
        val results = jsonObject.optJSONArray("results") ?: return havens
        val havenType = if (typeStr == "police") SafeHavenType.POLICE else SafeHavenType.HOSPITAL

        for (i in 0 until results.length()) {
            val result = results.optJSONObject(i) ?: continue
            val id = result.optString("place_id")
            val name = result.optString("name")
            val vicinity = result.optString("vicinity", "Address not available")
            val geometry = result.optJSONObject("geometry") ?: continue
            val loc = geometry.optJSONObject("location") ?: continue
            
            havens.add(
                SafeHaven(
                    id = id,
                    name = name,
                    address = vicinity,
                    location = GeoPoint(loc.optDouble("lat"), loc.optDouble("lng")),
                    type = havenType
                )
            )
        }
        return havens
    }
}