package com.example.healthplatform.presentation.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthplatform.domain.model.MetricType
import com.example.healthplatform.domain.usecase.GetTrendDataUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TrendsViewModel(
    private val getTrendDataUseCase: GetTrendDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrendsUiState())
    val uiState: StateFlow<TrendsUiState> = _uiState.asStateFlow()

    fun load(userId: String, endTime: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)

            runCatching {
                val stepsDeferred = async {
                    getTrendDataUseCase(userId, MetricType.STEPS, 7, endTime)
                }
                val heartDeferred = async {
                    getTrendDataUseCase(userId, MetricType.HEART_RATE, 7, endTime)
                }
                val sleepDeferred = async {
                    getTrendDataUseCase(userId, MetricType.SLEEP_DURATION, 7, endTime)
                }

                Triple(
                    stepsDeferred.await(),
                    heartDeferred.await(),
                    sleepDeferred.await()
                )
            }.onSuccess { (steps, heart, sleep) ->
                _uiState.value = TrendsUiState(
                    stepTrend = steps,
                    heartRateTrend = heart,
                    sleepTrend = sleep,
                    loading = false,
                    error = null
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = it.message ?: "Failed to load trend data"
                )
            }
        }
    }
}
