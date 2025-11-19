package com.suraksha.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.suraksha.app.R
import com.suraksha.app.ui.theme.AccentBlue

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Profile Avatar
        item {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = AccentBlue.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_person),
                        contentDescription = "Profile Picture",
                        tint = AccentBlue,
                        modifier = Modifier.size(60.dp)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Name Field
        item {
            ProfileInfoCard(
                label = "Name",
                value = currentUser?.displayName ?: "User",
                iconRes = R.drawable.ic_person
            )
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Email Field
        item {
            ProfileInfoCard(
                label = "Email",
                value = currentUser?.email ?: "Not set",
                iconRes = R.drawable.ic_notification
            )
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Phone Field
        item {
            ProfileInfoCard(
                label = "Phone",
                value = currentUser?.phoneNumber ?: "Not set",
                iconRes = R.drawable.ic_alert
            )
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Emergency Contacts Count
        item {
            ProfileInfoCard(
                label = "Emergency Contacts",
                value = "Manage in Contacts",
                iconRes = R.drawable.ic_person_add
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Account Status
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Account Status",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_shield),
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentUser?.isEmailVerified == true) "Verified Account" else "Unverified Account",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProfileInfoCard(
    label: String,
    value: String,
    iconRes: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = AccentBlue,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
