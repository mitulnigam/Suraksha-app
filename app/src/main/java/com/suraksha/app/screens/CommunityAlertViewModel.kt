package com.suraksha.app.screens

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.suraksha.app.data.CommunityAlert
import com.suraksha.app.data.CommunityAlertType
import com.suraksha.app.utils.LocationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class CommunityAlertViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val locationManager = LocationManager()

    private val _alerts = MutableStateFlow<List<CommunityAlert>>(emptyList())
    val alerts: StateFlow<List<CommunityAlert>> = _alerts.asStateFlow()

    private val _isPosting = MutableStateFlow(false)
    val isPosting: StateFlow<Boolean> = _isPosting.asStateFlow()

    // Holds the last known lat/lon fetched independently of MapViewModel
    private val _currentLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val currentLocation: StateFlow<Pair<Double, Double>?> = _currentLocation.asStateFlow()

    init {
        listenForAlerts()
        fetchLocation()
    }

    /** Fetch live location independently so posting always works. */
    private fun fetchLocation() {
        viewModelScope.launch {
            try {
                locationManager.getLocationUpdates(getApplication())
                    .collect { location ->
                        _currentLocation.value = Pair(location.latitude, location.longitude)
                    }
            } catch (e: Exception) {
                Log.e("CommunityAlertVM", "Location fetch error", e)
            }
        }
    }

    private fun listenForAlerts() {
        db.collection("community_alerts")
            .whereGreaterThan("expiresAt", Timestamp.now())
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("CommunityAlertVM", "Listen failed.", e)
                    return@addSnapshotListener
                }

                val alertList = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val typeStr = doc.getString("type") ?: "SUSPICIOUS"
                        CommunityAlert(
                            id = doc.id,
                            userId = doc.getString("userId") ?: "",
                            userName = doc.getString("userName") ?: "Anonymous",
                            type = CommunityAlertType.valueOf(typeStr),
                            description = doc.getString("description") ?: "",
                            location = doc.getGeoPoint("location") ?: GeoPoint(0.0, 0.0),
                            timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now(),
                            expiresAt = doc.getTimestamp("expiresAt") ?: Timestamp.now()
                        )
                    } catch (ex: Exception) {
                        Log.e("CommunityAlertVM", "Error parsing doc ${doc.id}", ex)
                        null
                    }
                } ?: emptyList()

                _alerts.value = alertList.sortedByDescending { it.timestamp }
            }
    }

    fun postAlert(type: CommunityAlertType, description: String, lat: Double, lon: Double) {
        val user = auth.currentUser
        val expiresAt = Calendar.getInstance().apply {
            add(Calendar.HOUR, 4)
        }.time

        val alertData = hashMapOf(
            "userId" to (user?.uid ?: "anonymous"),
            "userName" to (user?.displayName ?: "Anonymous"),
            "type" to type.name,
            "description" to description,
            "location" to GeoPoint(lat, lon),
            "timestamp" to Timestamp.now(),
            "expiresAt" to Timestamp(expiresAt)
        )

        _isPosting.value = true
        viewModelScope.launch {
            try {
                db.collection("community_alerts").add(alertData).await()
                Log.d("CommunityAlertVM", "Alert posted successfully at ($lat, $lon)")
                _isPosting.value = false
            } catch (e: Exception) {
                Log.e("CommunityAlertVM", "Error posting alert", e)
                _isPosting.value = false
            }
        }
    }
}
