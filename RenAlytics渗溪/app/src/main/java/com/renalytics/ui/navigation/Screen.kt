package com.renalytics.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object MeasurementDetail : Screen("measurement_detail")
    object DeviceConnection : Screen("device_connection")
    object UserManagement : Screen("user_management")
}
