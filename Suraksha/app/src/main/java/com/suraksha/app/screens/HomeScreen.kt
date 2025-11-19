package com.suraksha.app.screens

import android.content.Context
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.suraksha.app.R
import com.suraksha.app.ui.theme.AccentBlue
import com.suraksha.app.ui.theme.UrgentRed
import com.suraksha.app.utils.AlertManager
import com.suraksha.app.utils.LocationManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Error Fix: Made enum public to be accessible by other composables
enum class AlertState {
    IDLE,
    COUNTDOWN,
    SENT
}

@Composable
fun HomeScreen() {
    var alertState by remember { mutableStateOf(AlertState.IDLE) }
    // Error Fix: Used more performant mutableIntStateOf for Int
    var countdownSeconds by remember { mutableIntStateOf(10) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    // Error Fix: LocationManager requires a context to be initialized
    val locationManager = remember { LocationManager() }

    val sharedPrefs = remember { context.getSharedPreferences("SurakshaSettings", Context.MODE_PRIVATE) }

    var isShakeEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("SHAKE_ENABLED", true)) }
    var isVoiceEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("VOICE_ENABLED", true)) }
    var isFallEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("FALL_ENABLED", false)) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isShakeEnabled = sharedPrefs.getBoolean("SHAKE_ENABLED", true)
                isVoiceEnabled = sharedPrefs.getBoolean("VOICE_ENABLED", true)
                isFallEnabled = sharedPrefs.getBoolean("FALL_ENABLED", false)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(key1 = alertState) {
        if (alertState == AlertState.COUNTDOWN) {
            countdownSeconds = 10
            var timerActive = true

            while (countdownSeconds > 0 && timerActive) {
                delay(1000L)
                countdownSeconds--

                if (alertState != AlertState.COUNTDOWN) {
                    timerActive = false
                }
            }

            if (timerActive && alertState == AlertState.COUNTDOWN) {
                Log.d("HomeScreen", "Countdown finished. Getting location...")

                locationManager.getCurrentLocation(context.applicationContext) { location ->
                    coroutineScope.launch {
                        Log.d("HomeScreen", "Got location: $location. Sending alert...")

                        if (alertState == AlertState.COUNTDOWN) {
                            AlertManager.sendAlert(context.applicationContext, location)
                            alertState = AlertState.SENT
                        }
                    }
                }
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SURAKSHA",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        val (statusText, statusColor, statusIcon) = when (alertState) {
            AlertState.IDLE -> Triple("All Systems Normal", Color(0xFF34D399) /* Green */, R.drawable.ic_shield)
            AlertState.COUNTDOWN -> Triple("Sending Alert in $countdownSeconds...", AccentBlue, R.drawable.ic_alert)
            AlertState.SENT -> Triple("ALERT ACTIVE!", UrgentRed, R.drawable.ic_alert)
        }

        StatusChip(
            text = statusText,
            color = statusColor,
            iconRes = statusIcon
        )

        Spacer(modifier = Modifier.weight(1f))
        SOSButton(
            alertState = alertState,
            countdownSeconds = countdownSeconds,
            onClick = {
                alertState = when (alertState) {
                    AlertState.IDLE -> AlertState.COUNTDOWN
                    AlertState.COUNTDOWN -> AlertState.IDLE
                    AlertState.SENT -> AlertState.SENT
                }
            }
        )
        Spacer(modifier = Modifier.weight(1f))

        FeatureCardsRow(
            isShakeEnabled = isShakeEnabled,
            isVoiceEnabled = isVoiceEnabled,
            isFallEnabled = isFallEnabled
        )
    }
}

@Composable
fun StatusChip(text: String, color: Color, iconRes: Int) {
    Surface(
        shape = CircleShape,
        color = color.copy(alpha = 0.2f),
        contentColor = color,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .animateContentSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun SOSButton(
    alertState: AlertState,
    countdownSeconds: Int,
    onClick: () -> Unit
) {
    val buttonSize = 200.dp
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (alertState == AlertState.IDLE) 1.5f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ), label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = if (alertState == AlertState.IDLE) 1f else 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ), label = "pulseAlpha"
    )

    val progressAngle by animateFloatAsState(
        targetValue = if (alertState == AlertState.COUNTDOWN) (countdownSeconds / 10f) * 360f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing), label = "progress"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 50),
        label = "pressScale"
    )

    // Breathing animation for the button
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (alertState == AlertState.IDLE) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breatheScale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(buttonSize * 1.5f)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                    alpha = pulseAlpha
                }
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(UrgentRed.copy(alpha = 0.5f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        if (alertState == AlertState.COUNTDOWN) {
            Canvas(modifier = Modifier.size(buttonSize + 24.dp)) {
                drawArc(
                    color = AccentBlue,
                    startAngle = -90f,
                    sweepAngle = progressAngle,
                    useCenter = false,
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round),
                    size = Size(size.width, size.height)
                )
            }
        }

        val (buttonText, buttonColor) = when (alertState) {
            AlertState.IDLE ->
                "SOS" to Brush.linearGradient(colors = listOf(Color(0xFFFF416C), UrgentRed))
            AlertState.COUNTDOWN -> "CANCEL\n$countdownSeconds" to Brush.linearGradient(colors = listOf(AccentBlue, Color(0xFF007BFF)))
            AlertState.SENT -> "ALERT\nSENT" to Brush.linearGradient(colors = listOf(Color(0xFF444444), Color.Black))
        }

        Button(
            onClick = onClick,
            shape = CircleShape,
            modifier = Modifier
                .size(buttonSize)
                .graphicsLayer {
                    scaleX = breatheScale * pressScale
                    scaleY = breatheScale * pressScale
                }
                .background(brush = buttonColor, shape = CircleShape)
                .then(
                    if (alertState == AlertState.SENT) {
                        Modifier.border(width = 2.dp, color = UrgentRed, shape = CircleShape)
                    } else {
                        Modifier
                    }
                )
                .clip(CircleShape),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White,
                disabledContainerColor = Color.Transparent
            ),
            interactionSource = interactionSource,
            enabled = (alertState != AlertState.SENT),
            elevation = null
        ) {
            Text(
                text = buttonText,
                fontSize = when (alertState) {
                    AlertState.IDLE -> 56.sp
                    AlertState.COUNTDOWN -> 36.sp
                    AlertState.SENT -> 36.sp
                },
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 40.sp
            )
        }
    }
}

@Composable
fun FeatureCardsRow(
    isShakeEnabled: Boolean,
    isVoiceEnabled: Boolean,
    isFallEnabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FeatureCard(
            title = "Hotword",
            description = if (isVoiceEnabled) "ON" else "OFF",
            iconRes = R.drawable.ic_voice
        )
        FeatureCard(
            title = "Shake Alert",
            description = if (isShakeEnabled) "ON" else "OFF",
            iconRes = R.drawable.ic_shake
        )
        FeatureCard(
            title = "Fall Detection",
            description = if (isFallEnabled) "ON" else "OFF",
            iconRes = R.drawable.ic_fall
        )
    }
}

@Composable
fun RowScope.FeatureCard(
    title: String,
    description: String,
    iconRes: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .weight(1f)
            .clickable { /* TODO: Link to settings */ },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                tint = AccentBlue,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
