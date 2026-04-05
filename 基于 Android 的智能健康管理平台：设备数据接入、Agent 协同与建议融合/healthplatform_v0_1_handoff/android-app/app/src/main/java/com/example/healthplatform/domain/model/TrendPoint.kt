package com.example.healthplatform.domain.model

data class TrendPoint(
    val dayLabel: String,
    val metricType: MetricType,
    val value: Double
)
