package com.suraksha.app.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.suraksha.app.ui.theme.AccentBlue
import com.suraksha.app.ui.theme.DarkBackground
import com.suraksha.app.ui.theme.Gray

@Composable
fun SignUpScreen(
    authViewModel: AuthViewModel = viewModel(),
    onSignUpSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "SURAKSHA",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineMedium,
            color = AccentBlue,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Get started with Suraksha",
            style = MaterialTheme.typography.bodyMedium,
            color = Gray,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email", color = Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A1A), RoundedCornerShape(12.dp))
                .border(1.dp, Gray.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1A1A1A),
                unfocusedContainerColor = Color(0xFF1A1A1A),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = AccentBlue,
                unfocusedLabelColor = Gray,
                focusedIndicatorColor = AccentBlue,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = AccentBlue
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password", color = Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A1A), RoundedCornerShape(12.dp))
                .border(1.dp, Gray.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1A1A1A),
                unfocusedContainerColor = Color(0xFF1A1A1A),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = AccentBlue,
                unfocusedLabelColor = Gray,
                focusedIndicatorColor = AccentBlue,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = AccentBlue
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Password Field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password", color = Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A1A), RoundedCornerShape(12.dp))
                .border(1.dp, Gray.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1A1A1A),
                unfocusedContainerColor = Color(0xFF1A1A1A),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = AccentBlue,
                unfocusedLabelColor = Gray,
                focusedIndicatorColor = AccentBlue,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = AccentBlue
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Sign Up Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(
                    brush = Brush.linearGradient(colors = listOf(AccentBlue, Color(0xFF0099CC))),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable {
                    when {
                        email.isBlank() || password.isBlank() -> {
                            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                        }
                        password != confirmPassword -> {
                            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            isLoading = true
                            authViewModel.signUp(email, password,
                                onSuccess = {
                                    isLoading = false
                                    Log.d("SignUpScreen", "Sign Up Success")
                                    onSignUpSuccess()
                                },
                                onError = { errorMsg ->
                                    isLoading = false
                                    Log.w("SignUpScreen", "Sign Up Failure: $errorMsg")
                                    Toast.makeText(context, "Sign Up Failed: $errorMsg", Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text(text = "SIGN UP", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Login Text
        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Already have an account? ",
                color = Gray,
                fontSize = 14.sp
            )
            Text(
                text = "Log In",
                color = AccentBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onNavigateToLogin() }
            )
        }
    }
}