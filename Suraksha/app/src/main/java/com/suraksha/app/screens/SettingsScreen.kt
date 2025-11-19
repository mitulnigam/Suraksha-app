package com.suraksha.app.screens

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.suraksha.app.ui.theme.ThemeStore
import com.suraksha.app.ui.theme.UrgentRed
import androidx.compose.foundation.shape.CornerSize
import com.suraksha.app.sensors.SensorLoggerActivity

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

            item { SettingHeader(title = "OTHER") }
            item {
                SettingRow(title = "Notifications", iconRes = R.drawable.ic_notification, onClick = {})
            }
            item {
                SettingRow(title = "Privacy Policy", iconRes = R.drawable.ic_lock, onClick = {})
            }
            item {
                SettingRow(title = "About Us", iconRes = R.drawable.ic_info, onClick = {})
            }
            item {
                SettingRow(title = "Sensor Logger", iconRes = R.drawable.ic_info, onClick = {
                    context.startActivity(Intent(context, SensorLoggerActivity::class.java))
                })
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
    }
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