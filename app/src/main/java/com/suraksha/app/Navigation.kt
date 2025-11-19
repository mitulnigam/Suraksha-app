package com.suraksha.app


sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Contacts : Screen("contacts")
    object Map : Screen("map")
    object Settings : Screen("settings")
    object Profile : Screen("profile")
    object AboutUs : Screen("about_us")
}

sealed class AuthScreen(val route: String) {
    object Login : AuthScreen("login")
    object SignUp : AuthScreen("signup")
}

object Graph {
    const val ROOT = "root_graph"
    const val AUTH = "auth_graph"
    const val MAIN = "main_graph"
}