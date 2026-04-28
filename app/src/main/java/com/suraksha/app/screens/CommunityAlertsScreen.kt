package com.suraksha.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.suraksha.app.data.CommunityAlert
import com.suraksha.app.data.CommunityAlertType
import com.suraksha.app.ui.theme.AccentBlue
import com.suraksha.app.ui.theme.UrgentRed
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityAlertsScreen(
    navController: NavController,
    viewModel: CommunityAlertViewModel = viewModel()
) {
    val alerts by viewModel.alerts.collectAsState()
    val isPosting by viewModel.isPosting.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Community Alerts", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = AccentBlue,
                contentColor = Color.White
            ) {
                if (isPosting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Add, contentDescription = "Create Alert")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Location status banner
            if (currentLocation == null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = AccentBlue)
                    Text(
                        "Fetching your location…",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                }
            } else {
                val (lat, lon) = currentLocation!!
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AccentBlue.copy(alpha = 0.1f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("📍", fontSize = 14.sp)
                    Text(
                        "Your location: ${"%,.4f".format(lat)}, ${"%,.4f".format(lon)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = AccentBlue
                    )
                }
            }

            if (alerts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No active alerts in your community.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(alerts, key = { it.id }) { alert ->
                        AlertCard(alert)
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateAlertDialog(
            currentLocation = currentLocation,
            onDismiss = { showCreateDialog = false },
            onSubmit = { type, desc ->
                val loc = currentLocation
                if (loc != null) {
                    viewModel.postAlert(type, desc, loc.first, loc.second)
                }
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun AlertCard(alert: CommunityAlert) {
    val color = when (alert.type) {
        CommunityAlertType.CRIME -> UrgentRed
        CommunityAlertType.ACCIDENT -> Color(0xFFFF9800)
        CommunityAlertType.SUSPICIOUS -> Color(0xFFFBC02D)
    }

    val typeLabel = when (alert.type) {
        CommunityAlertType.CRIME -> "⚠️ CRIME"
        CommunityAlertType.ACCIDENT -> "🚗 ACCIDENT"
        CommunityAlertType.SUSPICIOUS -> "👤 SUSPICIOUS"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = typeLabel,
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(alert.timestamp.toDate()),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(4.dp))

            // Show actual coordinates from Firestore
            val lat = alert.location.latitude
            val lon = alert.location.longitude
            Text(
                text = "📍 ${"%,.4f".format(lat)}, ${"%,.4f".format(lon)}",
                style = MaterialTheme.typography.labelMedium,
                color = AccentBlue
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = alert.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "By: ${alert.userName}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Expires: ${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(alert.expiresAt.toDate())}",
                    style = MaterialTheme.typography.labelSmall,
                    color = UrgentRed.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAlertDialog(
    currentLocation: Pair<Double, Double>?,
    onDismiss: () -> Unit,
    onSubmit: (CommunityAlertType, String) -> Unit
) {
    var selectedType by remember { mutableStateOf(CommunityAlertType.SUSPICIOUS) }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report Safety Alert") },
        text = {
            Column {
                // Location preview
                if (currentLocation != null) {
                    val (lat, lon) = currentLocation
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AccentBlue.copy(alpha = 0.1f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "📍 Will post at: ${"%,.4f".format(lat)}, ${"%,.4f".format(lon)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = AccentBlue,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = AccentBlue)
                        Text("Waiting for GPS location…", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Select Type", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CommunityAlertType.values().forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("What's happening?") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Text(
                    "This alert will be visible to nearby users for 4 hours.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (description.isNotBlank() && currentLocation != null) onSubmit(selectedType, description) },
                enabled = description.isNotBlank() && currentLocation != null,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("Post Alert", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
