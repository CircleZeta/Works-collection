package com.example.healthplatform.domain.usecase

import com.example.healthplatform.domain.model.MetricType
import com.example.healthplatform.domain.model.TrendPoint
import com.example.healthplatform.domain.repository.HealthDataRepository

class GetTrendDataUseCase(
    private val repository: HealthDataRepository
) {
    suspend operator fun invoke(
        userId: String,
        metricType: MetricType,
        days: Int,
        endTime: Long
    ): List<TrendPoint> {
        return repository.buildTrend(
            userId = userId,
            metricType = metricType,
            days = days,
            endTime = endTime
        )
    }
}
