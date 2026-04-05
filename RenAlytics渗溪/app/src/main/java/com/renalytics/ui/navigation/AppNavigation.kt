package com.renalytics.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.renalytics.ui.screens.DashboardScreen
import com.renalytics.ui.screens.MeasurementDetailScreen
import com.renalytics.ui.screens.DeviceConnectionScreen
import com.renalytics.ui.screens.UserManagementScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }
        composable(Screen.MeasurementDetail.route) {
            MeasurementDetailScreen(navController = navController)
        }
        composable(Screen.DeviceConnection.route) {
            DeviceConnectionScreen(navController = navController)
        }
        composable(Screen.UserManagement.route) {
            UserManagementScreen(navController = navController)
        }
    }
}
