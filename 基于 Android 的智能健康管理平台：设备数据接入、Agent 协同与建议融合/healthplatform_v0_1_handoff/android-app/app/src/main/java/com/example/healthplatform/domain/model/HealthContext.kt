package com.example.healthplatform.domain.model

data class HealthContext(
    val userId: String,
    val startTime: Long,
    val endTime: Long,
    val summary: HealthSummary
)
