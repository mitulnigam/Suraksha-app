package com.suraksha.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.suraksha.app.data.CommunityAlert
import com.suraksha.app.data.CommunityAlertType
import com.suraksha.app.ui.theme.AccentBlue
import com.suraksha.app.ui.theme.UrgentRed
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityAlertsScreen(
    viewModel: CommunityAlertViewModel = viewModel(),
    mapViewModel: MapViewModel = viewModel()
) {
    val alerts by viewModel.alerts.collectAsState()
    val mapState by mapViewModel.mapState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Community Alerts", fontWeight = FontWeight.Bold) },
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
                Icon(Icons.Default.Add, contentDescription = "Create Alert")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
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
                    items(alerts) { alert ->
                        AlertCard(alert)
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateAlertDialog(
            onDismiss = { showCreateDialog = false },
            onSubmit = { type, desc ->
                mapState.userLocation?.let { location ->
                    viewModel.postAlert(type, desc, location.latitude, location.longitude)
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
        CommunityAlertType.ACCIDENT -> Color(0xFFFF9800) // Orange
        CommunityAlertType.SUSPICIOUS -> Color(0xFFFBC02D) // Yellow
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
            
            // Distance Text (If user location is available)
            // Note: In a real app, use Location.distanceBetween
            Text(
                text = "📍 Nearby Area",
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
fun CreateAlertDialog(onDismiss: () -> Unit, onSubmit: (CommunityAlertType, String) -> Unit) {
    var selectedType by remember { mutableStateOf(CommunityAlertType.SUSPICIOUS) }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report Safety Alert") },
        text = {
            Column {
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
                onClick = { if (description.isNotBlank()) onSubmit(selectedType, description) },
                enabled = description.isNotBlank(),
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
