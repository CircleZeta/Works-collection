package com.example.healthplatform.presentation.trends

import com.example.healthplatform.domain.model.TrendPoint

data class TrendsUiState(
    val stepTrend: List<TrendPoint> = emptyList(),
    val heartRateTrend: List<TrendPoint> = emptyList(),
    val sleepTrend: List<TrendPoint> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)
