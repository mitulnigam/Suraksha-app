package com.suraksha.app.screens

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.suraksha.app.utils.LocationManager
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
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import kotlin.math.*

data class PlaceResult(
    val name: String,
    val address: String,
    val latLng: LatLng
)

data class RouteState(
    val startLocation: LatLng? = null,
    val destination: LatLng? = null,
    val currentLocation: LatLng? = null,
    val routePoints: List<LatLng> = emptyList(),
    val isSafest: Boolean = true,
    val isLoading: Boolean = false,
    val isNavigating: Boolean = false,
    val totalRisk: Double = 0.0,
    val currentRisk: Double = 0.0,
    val errorMessage: String? = null,
    val highRiskZones: List<LatLng> = emptyList(),
    val placeSuggestions: List<PlaceResult> = emptyList()
)

class RouteViewModel(application: Application) : AndroidViewModel(application) {

    private val _routeState = MutableStateFlow(RouteState())
    val routeState: StateFlow<RouteState> = _routeState.asStateFlow()

    private val riskMap = mutableMapOf<Pair<Double, Double>, Double>()
    private val gridStep = 0.01
    private val httpClient = OkHttpClient()
    private val locationManager = LocationManager()
    private val API_KEY = "AIzaSyD2Pd89IeVzh_5a_fAFLHHaJjxYxPBphU0"
    
    private var locationJob: kotlinx.coroutines.Job? = null

    init {
        loadRiskData()
    }

    private fun loadRiskData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = getApplication<Application>().assets.open("ai_training_dataset.csv")
                val reader = BufferedReader(InputStreamReader(inputStream))
                reader.readLine()

                val tempRiskMap = mutableMapOf<Pair<Double, Double>, MutableList<Double>>()
                reader.forEachLine { line ->
                    val parts = line.split(",")
                    if (parts.size >= 11) {
                        val lat = parts[0].toDoubleOrNull() ?: return@forEachLine
                        val lon = parts[1].toDoubleOrNull() ?: return@forEachLine
                        val risk = parts[10].toDoubleOrNull() ?: 0.0
                        val key = Pair(round(lat / gridStep) * gridStep, round(lon / gridStep) * gridStep)
                        tempRiskMap.getOrPut(key) { mutableListOf() }.add(risk)
                    }
                }
                tempRiskMap.forEach { (key, risks) -> riskMap[key] = risks.average() }
            } catch (e: Exception) {
                Log.e("RouteViewModel", "Risk data load error", e)
            }
        }
    }

    fun startNavigation() {
        if (_routeState.value.routePoints.isEmpty()) return
        
        _routeState.value = _routeState.value.copy(isNavigating = true)
        
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            locationManager.getLocationUpdates(getApplication())
                .catch { e -> Log.e("RouteViewModel", "Nav location error", e) }
                .collect { location ->
                    val userPos = LatLng(location.latitude, location.longitude)
                    val risk = getRiskAt(userPos)
                    
                    _routeState.value = _routeState.value.copy(
                        currentLocation = userPos,
                        currentRisk = risk
                    )
                    
                    // Check if off-path (more than 50 meters)
                    if (!PolyUtil.isLocationOnPath(userPos, _routeState.value.routePoints, true, 50.0)) {
                        Log.d("RouteViewModel", "User off-path, re-calculating...")
                        recalculateFromCurrentLocation(userPos)
                    }
                }
        }
    }

    fun stopNavigation() {
        _routeState.value = _routeState.value.copy(isNavigating = false)
        locationJob?.cancel()
    }

    private fun recalculateFromCurrentLocation(pos: LatLng) {
        val dest = _routeState.value.destination ?: return
        viewModelScope.launch(Dispatchers.IO) {
            fetchDirections(pos, dest)
        }
    }

    fun setStartLocation(location: LatLng) {
        _routeState.value = _routeState.value.copy(startLocation = location, placeSuggestions = emptyList())
    }

    fun setDestination(location: LatLng) {
        _routeState.value = _routeState.value.copy(destination = location, placeSuggestions = emptyList())
    }

    fun searchPlaces(query: String) {
        if (query.length < 3) return
        viewModelScope.launch(Dispatchers.IO) {
            val url = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=$query&key=$API_KEY"
            val request = Request.Builder().url(url).build()
            try {
                val response = httpClient.newCall(request).execute()
                val body = response.body?.string()
                if (body != null) {
                    val json = JSONObject(body)
                    val results = json.getJSONArray("results")
                    val suggestions = mutableListOf<PlaceResult>()
                    for (i in 0 until min(5, results.length())) {
                        val obj = results.getJSONObject(i)
                        suggestions.add(PlaceResult(
                            obj.getString("name"),
                            obj.optString("formatted_address", ""),
                            LatLng(obj.getJSONObject("geometry").getJSONObject("location").getDouble("lat"), obj.getJSONObject("geometry").getJSONObject("location").getDouble("lng"))
                        ))
                    }
                    _routeState.value = _routeState.value.copy(placeSuggestions = suggestions)
                }
            } catch (e: Exception) { Log.e("RouteViewModel", "Places search error", e) }
        }
    }

    fun calculateRoute() {
        val start = _routeState.value.startLocation ?: return
        val dest = _routeState.value.destination ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _routeState.value = _routeState.value.copy(isLoading = true, errorMessage = null)
            fetchDirections(start, dest)
        }
    }

    private suspend fun fetchDirections(origin: LatLng, dest: LatLng) {
        val url = "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&alternatives=true&key=$API_KEY"
        val request = Request.Builder().url(url).build()
        try {
            val response = httpClient.newCall(request).execute()
            val body = response.body?.string()
            if (body != null) {
                val json = JSONObject(body)
                if (json.getString("status") == "OK") {
                    val routes = json.getJSONArray("routes")
                    var bestPoints = emptyList<LatLng>()
                    var minRisk = Double.MAX_VALUE
                    var bestRisk = 0.0

                    for (i in 0 until routes.length()) {
                        val route = routes.getJSONObject(i)
                        val points = PolyUtil.decode(route.getJSONObject("overview_polyline").getString("points"))
                        val risk = calculatePathRisk(points)
                        if (_routeState.value.isSafest) {
                            if (risk < minRisk) { minRisk = risk; bestPoints = points; bestRisk = risk }
                        } else if (i == 0) { bestPoints = points; bestRisk = risk; break }
                    }

                    withContext(Dispatchers.Main) {
                        _routeState.value = _routeState.value.copy(
                            routePoints = bestPoints,
                            totalRisk = bestRisk,
                            isLoading = false,
                            highRiskZones = bestPoints.filter { getRiskAt(it) > 0.7 }
                        )
                    }
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) { _routeState.value = _routeState.value.copy(isLoading = false, errorMessage = e.message) }
        }
    }

    private fun calculatePathRisk(points: List<LatLng>): Double {
        if (points.isEmpty()) return 0.0
        return points.map { getRiskAt(it) }.average()
    }

    private fun getRiskAt(latLng: LatLng): Double {
        val key = Pair(round(latLng.latitude / gridStep) * gridStep, round(latLng.longitude / gridStep) * gridStep)
        return riskMap[key] ?: 0.45
    }

    fun toggleRouteType() {
        _routeState.value = _routeState.value.copy(isSafest = !_routeState.value.isSafest)
        if (_routeState.value.startLocation != null && _routeState.value.destination != null) calculateRoute()
    }
}
