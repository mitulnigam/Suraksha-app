package com.suraksha.app

// This file defines all the "addresses" (routes) in your app

// Routes for the main app (after login)
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Contacts : Screen("contacts")
    object Map : Screen("map")
    object Settings : Screen("settings")
    object Profile : Screen("profile")
}

// Routes for the authentication flow (before login)
sealed class AuthScreen(val route: String) {
    object Login : AuthScreen("login")
    object SignUp : AuthScreen("signup")
}

// Parent routes for the two main sections of your app
object Graph {
    const val ROOT = "root_graph"
    const val AUTH = "auth_graph"
    const val MAIN = "main_graph"
}