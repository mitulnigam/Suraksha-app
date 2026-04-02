package com.suraksha.app.screens

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
// Replace Google LatLng with OSMDroid GeoPoint
import org.osmdroid.util.GeoPoint
import com.suraksha.app.utils.LocationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

data class MapState(
    val userLocation: GeoPoint? = null,
    val safeHavens: List<SafeHaven> = emptyList(),
    val isLoading: Boolean = true
)

data class SafeHaven(
    val id: String,
    val name: String,
    val address: String, // Keeping address to maintain UI compatibility
    val location: GeoPoint,
    val type: SafeHavenType
)

enum class SafeHavenType {
    POLICE,
    HOSPITAL
}

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val locationManager = LocationManager()
    private val httpClient = OkHttpClient()

    private val _mapState = MutableStateFlow(MapState())
    val mapState: StateFlow<MapState> = _mapState.asStateFlow()

    private var hasSearched = false
    private var locationCollectionJob: kotlinx.coroutines.Job? = null

    init {
        startLocationUpdates()
    }

    fun refreshMap() {
        Log.d("MapViewModel", "🔄 Refreshing map data...")
        _mapState.value = MapState(isLoading = true)
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
                    Log.d("MapViewModel", "📍 Location update: $newGeoPoint")
                    _mapState.value = _mapState.value.copy(userLocation = newGeoPoint, isLoading = false)

                    if (!hasSearched) {
                        hasSearched = true
                        Log.d("MapViewModel", "🔍 Starting search for safe havens at location: $newGeoPoint")
                        searchOverpassSafeHavens(newGeoPoint)
                    }
                }
        }
    }

    private fun searchOverpassSafeHavens(location: GeoPoint) {
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

        viewModelScope.launch(Dispatchers.IO) {
            _mapState.value = _mapState.value.copy(isLoading = true)
            
            // Query for police and hospitals within 5000m
            val query = """
                [out:json][timeout:25];
                (
                  node["amenity"="police"](around:5000,${location.latitude},${location.longitude});
                  way["amenity"="police"](around:5000,${location.latitude},${location.longitude});
                  node["amenity"="hospital"](around:5000,${location.latitude},${location.longitude});
                  way["amenity"="hospital"](around:5000,${location.latitude},${location.longitude});
                  node["healthcare"="hospital"](around:5000,${location.latitude},${location.longitude});
                  way["healthcare"="hospital"](around:5000,${location.latitude},${location.longitude});
                );
                out center;
            """.trimIndent()

            val mediaType = "application/x-www-form-urlencoded".toMediaType()
            val requestBody = "data=${java.net.URLEncoder.encode(query, "UTF-8")}".toRequestBody(mediaType)

            val request = Request.Builder()
                .url("https://overpass-api.de/api/interpreter")
                .post(requestBody)
                .build()

            try {
                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        val parsedHavens = parseOverpassResponse(responseBody)
                        withContext(Dispatchers.Main) {
                            _mapState.value = _mapState.value.copy(
                                safeHavens = parsedHavens,
                                isLoading = false
                            )
                        }
                    } else {
                        Log.e("MapViewModel", "Overpass response body is null")
                        withContext(Dispatchers.Main) { _mapState.value = _mapState.value.copy(isLoading = false) }
                    }
                } else {
                    Log.e("MapViewModel", "Overpass search failed with code: ${response.code}")
                    withContext(Dispatchers.Main) { _mapState.value = _mapState.value.copy(isLoading = false) }
                }
            } catch (e: IOException) {
                Log.e("MapViewModel", "Exception connecting to Overpass API", e)
                withContext(Dispatchers.Main) { _mapState.value = _mapState.value.copy(isLoading = false) }
            } catch (e: Exception) {
                 Log.e("MapViewModel", "Exception parsing Overpass data", e)
                 withContext(Dispatchers.Main) { _mapState.value = _mapState.value.copy(isLoading = false) }
            }
        }
    }

    private fun parseOverpassResponse(jsonBody: String): List<SafeHaven> {
        val havens = mutableListOf<SafeHaven>()
        val jsonObject = JSONObject(jsonBody)
        val elements = jsonObject.optJSONArray("elements") ?: return havens

        for (i in 0 until elements.length()) {
            val element = elements.optJSONObject(i) ?: continue
            val id = element.optLong("id").toString()
            val tags = element.optJSONObject("tags") ?: continue
            
            val name = tags.optString("name", "Unknown Amenity")
            val amenity = tags.optString("amenity", "")
            val healthcare = tags.optString("healthcare", "")

            val type = if (amenity == "police") {
                SafeHavenType.POLICE
            } else if (amenity == "hospital" || healthcare == "hospital" || healthcare == "clinic") {
                SafeHavenType.HOSPITAL
            } else {
                continue // Skip unknown types
            }

            var lat = element.optDouble("lat", Double.NaN)
            var lon = element.optDouble("lon", Double.NaN)
            
            // "way" elements might have center instead of lat/lon
            if (lat.isNaN() || lon.isNaN()) {
                val center = element.optJSONObject("center")
                if (center != null) {
                    lat = center.optDouble("lat", Double.NaN)
                    lon = center.optDouble("lon", Double.NaN)
                }
            }

            val address = tags.optString("addr:street", "Address not available") + 
                          if (tags.has("addr:housenumber")) " " + tags.optString("addr:housenumber") else ""

            if (!lat.isNaN() && !lon.isNaN()) {
                havens.add(
                    SafeHaven(
                        id = id,
                        name = name,
                        address = address.trim().ifEmpty { "Address not available" },
                        location = GeoPoint(lat, lon),
                        type = type
                    )
                )
            }
        }
        return havens
    }
}