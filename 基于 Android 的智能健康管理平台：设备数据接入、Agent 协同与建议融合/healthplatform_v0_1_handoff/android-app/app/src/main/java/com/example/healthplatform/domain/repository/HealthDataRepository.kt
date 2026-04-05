package com.example.healthplatform.domain.repository

import com.example.healthplatform.domain.model.HealthMetric
import com.example.healthplatform.domain.model.HealthSummary
import com.example.healthplatform.domain.model.MetricType
import com.example.healthplatform.domain.model.TrendPoint

interface HealthDataRepository {
    suspend fun replaceMetricsForUser(userId: String, metrics: List<HealthMetric>)
    suspend fun saveMetrics(metrics: List<HealthMetric>)
    suspend fun getMetrics(userId: String, start: Long, end: Long): List<HealthMetric>
    suspend fun buildSummary(userId: String, start: Long, end: Long): HealthSummary
    suspend fun buildTrend(
        userId: String,
        metricType: MetricType,
        days: Int,
        endTime: Long
    ): List<TrendPoint>
}
