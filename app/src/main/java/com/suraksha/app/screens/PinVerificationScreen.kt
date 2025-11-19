package com.suraksha.app.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suraksha.app.ui.theme.AccentBlue
import com.suraksha.app.utils.IconManager
import com.suraksha.app.utils.PinManager

@Composable
fun PinVerificationScreen(
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    var pin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(AccentBlue.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Calculator",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter PIN to access",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (index < pin.length) AccentBlue
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    NumberButton("1", pin.length < 4) { pin += "1"; errorMessage = "" }
                    NumberButton("2", pin.length < 4) { pin += "2"; errorMessage = "" }
                    NumberButton("3", pin.length < 4) { pin += "3"; errorMessage = "" }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    NumberButton("4", pin.length < 4) { pin += "4"; errorMessage = "" }
                    NumberButton("5", pin.length < 4) { pin += "5"; errorMessage = "" }
                    NumberButton("6", pin.length < 4) { pin += "6"; errorMessage = "" }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    NumberButton("7", pin.length < 4) { pin += "7"; errorMessage = "" }
                    NumberButton("8", pin.length < 4) { pin += "8"; errorMessage = "" }
                    NumberButton("9", pin.length < 4) { pin += "9"; errorMessage = "" }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Button(
                        onClick = {
                            pin = ""
                            errorMessage = ""
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(70.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        enabled = pin.isNotEmpty()
                    ) {
                        Text("C", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }

                    NumberButton("0", pin.length < 4) { pin += "0"; errorMessage = "" }

                    Button(
                        onClick = {
                            if (pin.length == 4) {
                                isVerifying = true
                                if (PinManager.verifyPin(context, pin)) {

                                    IconManager.revealIcon(context)
                                    onSuccess()
                                } else {
                                    errorMessage = "Incorrect PIN"
                                    pin = ""
                                    isVerifying = false
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(70.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        enabled = pin.length == 4 && !isVerifying
                    ) {
                        if (isVerifying) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text("âœ“", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = {
                    (context as? Activity)?.finishAffinity()
                }
            ) {
                Text(
                    text = "Exit App",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun RowScope.NumberButton(
    number: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .weight(1f)
            .height(70.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        enabled = enabled
    ) {
        Text(
            text = number,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

