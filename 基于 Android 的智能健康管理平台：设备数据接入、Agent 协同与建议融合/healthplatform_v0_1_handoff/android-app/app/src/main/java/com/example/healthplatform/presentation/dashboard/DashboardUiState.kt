package com.example.healthplatform.presentation.dashboard

data class DashboardUiState(
    val avgHeartRate: String = "--",
    val todaySteps: String = "--",
    val sleepDuration: String = "--",
    val statusSummary: String = "Waiting for data import",
    val loading: Boolean = false,
    val error: String? = null
)
