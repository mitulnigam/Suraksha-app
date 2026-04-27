package com.suraksha.app.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class CommunityAlert(
    val id: String = "",
    val userId: String = "",
    val userName: String = "Anonymous",
    val type: CommunityAlertType = CommunityAlertType.SUSPICIOUS,
    val description: String = "",
    val location: GeoPoint = GeoPoint(0.0, 0.0),
    val timestamp: Timestamp = Timestamp.now(),
    val expiresAt: Timestamp = Timestamp.now()
)

enum class CommunityAlertType {
    CRIME,
    ACCIDENT,
    SUSPICIOUS
}
