package com.suraksha.app

import androidx.annotation.DrawableRes
import com.suraksha.app.R

sealed class BottomNavItem(
    val route: String,
    val title: String,
    @DrawableRes val icon: Int
) {
    object Home : BottomNavItem(Screen.Home.route, "Home", R.drawable.ic_home)
    object Contacts : BottomNavItem(Screen.Contacts.route, "Contacts", R.drawable.ic_contacts)
    object Map : BottomNavItem(Screen.Map.route, "Map", R.drawable.ic_map)
    object Settings : BottomNavItem(Screen.Settings.route, "Settings", R.drawable.ic_settings)

}
