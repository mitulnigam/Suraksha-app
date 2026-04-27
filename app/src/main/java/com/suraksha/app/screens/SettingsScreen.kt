package com.suraksha.app.screens

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.core.content.edit
import com.suraksha.app.Graph
import com.suraksha.app.R
import com.suraksha.app.Screen
import com.suraksha.app.services.SurakshaService
import com.suraksha.app.ui.theme.AccentBlue
import com.suraksha.app.utils.PinManager
import com.suraksha.app.sensors.SensorLoggerActivity
import com.suraksha.app.ui.theme.ThemeStore
import com.suraksha.app.ui.theme.UrgentRed
import androidx.compose.foundation.shape.CornerSize

@Composable
fun SettingsScreen(
    navController: NavController,
    rootNavController: NavHostController,
    authViewModel: AuthViewModel = viewModel()
) {

    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("SurakshaSettings", Context.MODE_PRIVATE) }

    var shakeEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("SHAKE_ENABLED", true)) }
    var voiceEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("VOICE_ENABLED", true)) }
    var fallEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("FALL_ENABLED", false)) }
    var showPinDialog by remember { mutableStateOf(false) }


    val serviceIntent = remember { Intent(context, SurakshaService::class.java) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        item { SettingHeader(title = "ACCOUNT") }
            item {
                SettingRow(
                    title = "Edit Profile",
                    iconRes = R.drawable.ic_profile,
                    onClick = { navController.navigate(Screen.Profile.route) }
                )
            }
            item {
                SettingRow(
                    title = "Logout",
                    iconRes = R.drawable.ic_logout,
                    color = UrgentRed,
                    onClick = {
                        authViewModel.signOut()
                        rootNavController.navigate(Graph.AUTH) {
                            popUpTo(Graph.ROOT) { inclusive = true }
                        }
                    }
                )
            }

            item { SettingHeader(title = "TRIGGERS") }
            item {
                SettingToggleRow(
                    title = "Shake Detection",
                    iconRes = R.drawable.ic_shake,
                    checked = shakeEnabled,
                    onCheckedChange = { isChecked ->
                        shakeEnabled = isChecked
                        sharedPrefs.edit { putBoolean("SHAKE_ENABLED", isChecked) }
                        if (isChecked) {
                            Log.d("SettingsScreen", "Sending START_SHAKE command...")
                            serviceIntent.action = SurakshaService.ACTION_START_SHAKE_LISTENER
                            context.startService(serviceIntent)
                        } else {
                            Log.d("SettingsScreen", "Sending STOP_SHAKE command...")
                            serviceIntent.action = SurakshaService.ACTION_STOP_SHAKE_LISTENER
                            context.startService(serviceIntent)
                        }
                        serviceIntent.action = SurakshaService.ACTION_SYNC_LISTENERS
                        context.startService(serviceIntent)
                    }
                )
            }
            item {
                SettingToggleRow(
                    title = "Hotword Detection",
                    iconRes = R.drawable.ic_voice,
                    checked = voiceEnabled,
                    onCheckedChange = { isChecked ->
                        voiceEnabled = isChecked
                        sharedPrefs.edit { putBoolean("VOICE_ENABLED", isChecked) }
                        if (isChecked) {
                            Log.d("SettingsScreen", "Sending START_VOICE (hotword) command...")
                            serviceIntent.action = SurakshaService.ACTION_START_VOICE_LISTENER
                            context.startService(serviceIntent)
                        } else {
                            Log.d("SettingsScreen", "Sending STOP_VOICE (hotword) command...")
                            serviceIntent.action = SurakshaService.ACTION_STOP_VOICE_LISTENER
                            context.startService(serviceIntent)
                        }
                        serviceIntent.action = SurakshaService.ACTION_SYNC_LISTENERS
                        context.startService(serviceIntent)
                    }
                )
            }


            item {
                SettingToggleRow(
                    title = "AI Fall Detection",
                    iconRes = R.drawable.ic_fall,
                    checked = fallEnabled,
                    onCheckedChange = { isChecked ->
                        fallEnabled = isChecked
                        sharedPrefs.edit { putBoolean("FALL_ENABLED", isChecked) }
                        if (isChecked) {
                            Log.d("SettingsScreen", "Sending START_FALL command...")
                            serviceIntent.action = SurakshaService.ACTION_START_FALL_LISTENER
                            context.startService(serviceIntent)
                        } else {
                            Log.d("SettingsScreen", "Sending STOP_FALL command...")
                            serviceIntent.action = SurakshaService.ACTION_STOP_FALL_LISTENER
                            context.startService(serviceIntent)
                        }
                        serviceIntent.action = SurakshaService.ACTION_SYNC_LISTENERS
                        context.startService(serviceIntent)
                    }
                )
            }

            item { SettingHeader(title = "APPEARANCE") }
            item {
                SettingToggleRow(
                    title = "Light Mode",
                    iconRes = if (!ThemeStore.isDark.value) R.drawable.ic_sun else R.drawable.ic_moon,
                    checked = !ThemeStore.isDark.value,
                    onCheckedChange = { isChecked ->
                        ThemeStore.setDark(context, !isChecked)
                    }
                )
            }

            item { SettingHeader(title = "SECURITY") }
            item {
                SettingRow(
                    title = if (PinManager.isPinSet(context)) "Change PIN" else "Setup PIN",
                    iconRes = R.drawable.ic_lock,
                    onClick = { showPinDialog = true }
                )
            }
            item {
                if (PinManager.isPinSet(context)) {
                    SettingRow(
                        title = "App Disguise: ${if (PinManager.isAppDisguised(context)) "Active" else "Inactive"}",
                        iconRes = R.drawable.ic_info,
                        onClick = { }
                    )
                }
            }

            item { SettingHeader(title = "OTHER") }
            item {
                SettingRow(
                    title = "Community Alerts",
                    iconRes = R.drawable.ic_alert,
                    onClick = { navController.navigate(Screen.CommunityAlerts.route) }
                )
            }
            item {
                SettingRow(title = "Notifications", iconRes = R.drawable.ic_notification, onClick = {})
            }
            item {
                SettingRow(
                    title = "About Us",
                    iconRes = R.drawable.ic_info,
                    onClick = { navController.navigate(Screen.AboutUs.route) }
                )
            }
            item {
                SettingRow(title = "Sensor Logger", iconRes = R.drawable.ic_info, onClick = {
                    context.startActivity(Intent(context, SensorLoggerActivity::class.java))
                })
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    if (showPinDialog) {
        PinDialog(
            isPinSet = PinManager.isPinSet(context),
            onDismiss = { showPinDialog = false },
            onSuccess = {
                showPinDialog = false
                Toast.makeText(context, "PIN updated successfully", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun PinDialog(
    isPinSet: Boolean,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(if (isPinSet) 0 else 1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isPinSet) "Change PIN" else "Setup PIN") },
        text = {
            Column {
                when (step) {
                    0 -> {
                        Text("Enter current PIN", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = oldPin,
                            onValueChange = {
                                if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                    oldPin = it
                                    errorMessage = ""
                                }
                            },
                            label = { Text("Current PIN") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = PasswordVisualTransformation(),
                            isError = errorMessage.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    1 -> {
                        Text("Enter new 4-digit PIN", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newPin,
                            onValueChange = {
                                if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                    newPin = it
                                    errorMessage = ""
                                }
                            },
                            label = { Text("New PIN") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = PasswordVisualTransformation(),
                            isError = errorMessage.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    2 -> {
                        Text("Confirm new PIN", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = confirmPin,
                            onValueChange = {
                                if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                    confirmPin = it
                                    errorMessage = ""
                                }
                            },
                            label = { Text("Confirm PIN") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = PasswordVisualTransformation(),
                            isError = errorMessage.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when (step) {
                        0 -> {
                            if (oldPin.length == 4) {
                                if (PinManager.verifyPin(context, oldPin)) {
                                    step = 1
                                } else {
                                    errorMessage = "Incorrect PIN"
                                }
                            } else {
                                errorMessage = "PIN must be 4 digits"
                            }
                        }
                        1 -> {
                            if (newPin.length == 4) {
                                step = 2
                            } else {
                                errorMessage = "PIN must be 4 digits"
                            }
                        }
                        2 -> {
                            if (confirmPin.length == 4) {
                                if (newPin == confirmPin) {
                                    val success = if (isPinSet) {
                                        PinManager.changePin(context, oldPin, newPin)
                                    } else {
                                        PinManager.setupPin(context, newPin)
                                    }

                                    if (success) {
                                        onSuccess()
                                    } else {
                                        errorMessage = "Failed to save PIN"
                                    }
                                } else {
                                    errorMessage = "PINs do not match"
                                }
                            } else {
                                errorMessage = "PIN must be 4 digits"
                            }
                        }
                    }
                }
            ) {
                Text("Continue")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun SettingHeader(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingRow(
    title: String,
    iconRes: Int,
    color: Color = Color.Unspecified,
    onClick: () -> Unit
) {
    val contentColor = if (color == Color.Unspecified) MaterialTheme.colorScheme.onSurface else color
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                color = contentColor,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    iconRes: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onCheckedChange(!checked) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = AccentBlue,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                    uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

