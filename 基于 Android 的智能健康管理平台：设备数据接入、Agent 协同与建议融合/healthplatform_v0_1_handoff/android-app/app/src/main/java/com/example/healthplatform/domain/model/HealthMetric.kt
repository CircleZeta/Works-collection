package com.example.healthplatform.domain.model

data class HealthMetric(
    val id: Long = 0L,
    val userId: String,
    val metricType: MetricType,
    val value: Double,
    val unit: String,
    val timestamp: Long,
    val source: String
)
