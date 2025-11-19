package com.suraksha.app.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.suraksha.app.R
import com.suraksha.app.ui.theme.AccentBlue
import com.suraksha.app.ui.theme.UrgentRed

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()

    var name by remember { mutableStateOf(currentUser?.displayName ?: "") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var medicalInfo by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { uid ->
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        name = document.getString("name") ?: currentUser.displayName ?: ""
                        phone = document.getString("phone") ?: ""
                        address = document.getString("address") ?: ""
                        bloodGroup = document.getString("bloodGroup") ?: ""
                        age = document.getString("age") ?: ""
                        medicalInfo = document.getString("medicalInfo") ?: ""
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                    Toast.makeText(context, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = AccentBlue)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(
                    onClick = { isEditing = !isEditing }
                ) {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.Edit else Icons.Default.Edit,
                        contentDescription = if (isEditing) "Cancel" else "Edit",
                        tint = if (isEditing) UrgentRed else AccentBlue
                    )
                }
            }
        }

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

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Email (Cannot be changed)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentUser?.email ?: "Not set",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        item {
            EditableProfileField(
                label = "Full Name",
                value = name,
                onValueChange = { name = it },
                icon = R.drawable.ic_person,
                isEditing = isEditing,
                placeholder = "Enter your full name"
            )
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        item {
            EditableProfileField(
                label = "Phone Number",
                value = phone,
                onValueChange = { phone = it },
                icon = R.drawable.ic_alert,
                isEditing = isEditing,
                placeholder = "Enter your phone number",
                keyboardType = KeyboardType.Phone
            )
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        item {
            EditableProfileField(
                label = "Age",
                value = age,
                onValueChange = { age = it },
                icon = R.drawable.ic_info,
                isEditing = isEditing,
                placeholder = "Enter your age",
                keyboardType = KeyboardType.Number
            )
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        item {
            EditableProfileField(
                label = "Blood Group",
                value = bloodGroup,
                onValueChange = { bloodGroup = it },
                icon = R.drawable.ic_fall,
                isEditing = isEditing,
                placeholder = "e.g., O+, A-, B+, AB+"
            )
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        item {
            EditableProfileField(
                label = "Address",
                value = address,
                onValueChange = { address = it },
                icon = R.drawable.ic_map,
                isEditing = isEditing,
                placeholder = "Enter your address",
                singleLine = false,
                maxLines = 3
            )
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        item {
            EditableProfileField(
                label = "Medical Information (Optional)",
                value = medicalInfo,
                onValueChange = { medicalInfo = it },
                icon = R.drawable.ic_shield,
                isEditing = isEditing,
                placeholder = "Allergies, medications, conditions, etc.",
                singleLine = false,
                maxLines = 4
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        if (isEditing) {
            item {
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            Toast.makeText(context, "Name is required", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isSaving = true

                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()

                        currentUser?.updateProfile(profileUpdates)
                            ?.addOnSuccessListener {

                                val userData = hashMapOf(
                                    "name" to name,
                                    "phone" to phone,
                                    "age" to age,
                                    "bloodGroup" to bloodGroup,
                                    "address" to address,
                                    "medicalInfo" to medicalInfo,
                                    "email" to (currentUser.email ?: ""),
                                    "updatedAt" to System.currentTimeMillis()
                                )

                                currentUser.uid.let { uid ->
                                    firestore.collection("users").document(uid)
                                        .set(userData)
                                        .addOnSuccessListener {
                                            isSaving = false
                                            isEditing = false
                                            Toast.makeText(context, "Profile saved successfully!", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { e ->
                                            isSaving = false
                                            Toast.makeText(context, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                            ?.addOnFailureListener { e ->
                                isSaving = false
                                Toast.makeText(context, "Failed to update: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_shield),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save Profile",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AccentBlue.copy(alpha = 0.1f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_info),
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Why we need this information",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "â€¢ Your details help emergency responders assist you better\n" +
                                "â€¢ Medical info is crucial in emergencies\n" +
                                "â€¢ Blood group can save critical time",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
fun EditableProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: Int,
    isEditing: Boolean,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    maxLines: Int = 1
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEditing)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isEditing) 4.dp else 2.dp
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (isEditing) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    singleLine = singleLine,
                    maxLines = maxLines,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            } else {
                Text(
                    text = if (value.isNotBlank()) value else "Not set",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (value.isNotBlank())
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    fontWeight = if (value.isNotBlank()) FontWeight.Medium else FontWeight.Normal
                )
            }
        }
    }
}
