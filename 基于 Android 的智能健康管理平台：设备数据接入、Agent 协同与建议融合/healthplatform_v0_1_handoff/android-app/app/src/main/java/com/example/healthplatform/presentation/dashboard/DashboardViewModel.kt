package com.example.healthplatform.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthplatform.domain.usecase.GetDashboardSummaryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val getDashboardSummaryUseCase: GetDashboardSummaryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun load(userId: String, start: Long, end: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)

            runCatching {
                getDashboardSummaryUseCase(userId, start, end)
            }.onSuccess { summary ->
                _uiState.value = DashboardUiState(
                    avgHeartRate = summary.avgHeartRate?.let { "${it.toInt()} bpm" } ?: "--",
                    todaySteps = summary.todaySteps.toString(),
                    sleepDuration = summary.sleepDurationHours?.let { String.format("%.1f h", it) } ?: "--",
                    statusSummary = buildStatusText(summary.todaySteps, summary.sleepDurationHours),
                    loading = false,
                    error = null
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = it.message ?: "Failed to load dashboard"
                )
            }
        }
    }

    private fun buildStatusText(steps: Int, sleepHours: Double?): String {
        return when {
            sleepHours != null && sleepHours < 6.5 -> "Recovery is slightly insufficient. Prioritize sleep."
            steps < 6000 -> "Daily activity is low. Add more walking if possible."
            else -> "Current status is generally stable."
        }
    }
}
