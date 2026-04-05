package com.example.healthplatform.presentation.navigation

sealed class Screen(val route: String, val title: String) {
    data object Dashboard : Screen("dashboard", "Dashboard")
    data object Trends : Screen("trends", "Trends")
    data object Advice : Screen("advice", "Advice")
    data object Settings : Screen("settings", "Settings")
}

val bottomNavScreens = listOf(
    Screen.Dashboard,
    Screen.Trends,
    Screen.Advice,
    Screen.Settings
)
