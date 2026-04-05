package com.example.healthplatform.domain.model

data class HealthSummary(
    val avgHeartRate: Double?,
    val restingHeartRate: Double?,
    val todaySteps: Int,
    val yesterdaySteps: Int,
    val sleepDurationHours: Double?,
    val deepSleepHours: Double?,
    val lightSleepHours: Double?,
    val stepTrendRatio: Double?,
    val sleepTrendRatio: Double?
)
