package com.suraksha.app

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.suraksha.app.screens.*
import com.suraksha.app.services.SurakshaService
import com.suraksha.app.services.FallDetectorService
import com.suraksha.app.ui.theme.SurakshaTheme
import com.suraksha.app.ui.theme.ThemeStore

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ThemeStore.init(this)


        try {
            val intent = Intent(this, FallDetectorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            Log.d("MainActivity", "FallDetectorService started")
        } catch (e: Exception) {
            Log.w("MainActivity", "Failed to start FallDetectorService: ${e.message}")

        }

        setContent {
            val permissionsToRequest = remember {
                val list = mutableListOf(
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.RECORD_AUDIO
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    list.add(Manifest.permission.ANSWER_PHONE_CALLS)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    list.add(Manifest.permission.POST_NOTIFICATIONS)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    list.add(Manifest.permission.FOREGROUND_SERVICE_LOCATION)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    list.add(Manifest.permission.FOREGROUND_SERVICE_MICROPHONE)
                }
                list.toTypedArray()
            }

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                val allGranted = permissions.values.all { it }
                if (allGranted) {
                    Log.d("Permissions", "All permissions granted")
                } else {
                    Log.w("Permissions", "One or more permissions denied")
                }
            }
            LaunchedEffect(key1 = true) {
                permissionLauncher.launch(permissionsToRequest)

                Intent(this@MainActivity, SurakshaService::class.java).also { intent ->
                    intent.action = SurakshaService.ACTION_SYNC_LISTENERS
                    startService(intent)
                }
            }

            SurakshaTheme(darkTheme = ThemeStore.isDark.value) {

                if (com.suraksha.app.utils.PinManager.isAppDisguised(this@MainActivity)) {
                    com.suraksha.app.screens.PinVerificationScreen(
                        onSuccess = {

                            recreate()
                        }
                    )
                } else {
                    RootNavigationGraph(
                        navController = rememberNavController()
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        Intent(this, SurakshaService::class.java).also { intent ->
            intent.action = SurakshaService.ACTION_APP_FOREGROUND
            startService(intent)
        }
        Intent(this, SurakshaService::class.java).also { intent ->
            intent.action = SurakshaService.ACTION_SYNC_LISTENERS
            startService(intent)
        }
        Log.d("MainActivity", "App resumed - notified service & synced listeners")
    }

    override fun onPause() {
        super.onPause()

        Intent(this, SurakshaService::class.java).also { intent ->
            intent.action = SurakshaService.ACTION_APP_BACKGROUND
            startService(intent)
        }
        Log.d("MainActivity", "App paused - notified service")
    }
}

@Composable
fun RootNavigationGraph(navController: NavHostController) {
    val authViewModel: AuthViewModel = viewModel()

    val startDestination = if (authViewModel.getCurrentUser() != null) {
        Graph.MAIN
    } else {
        Graph.AUTH
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        route = Graph.ROOT
    ) {
        navigation(
            startDestination = AuthScreen.Login.route,
            route = Graph.AUTH
        ) {
            composable(route = AuthScreen.Login.route) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onLoginSuccess = {
                        navController.navigate(Graph.MAIN) {
                            popUpTo(Graph.AUTH) { inclusive = true }
                        }
                    },
                    onNavigateToSignUp = {
                        navController.navigate(AuthScreen.SignUp.route)
                    }
                )
            }
            composable(route = AuthScreen.SignUp.route) {
                SignUpScreen(
                    authViewModel = authViewModel,
                    onSignUpSuccess = {
                        navController.navigate(Graph.MAIN) {
                            popUpTo(Graph.AUTH) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(route = Graph.MAIN) {
            AppMainNavigation(rootNavController = navController)
        }
    }
}