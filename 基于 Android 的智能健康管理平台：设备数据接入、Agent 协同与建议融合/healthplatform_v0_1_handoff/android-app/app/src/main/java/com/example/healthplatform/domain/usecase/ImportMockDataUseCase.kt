package com.example.healthplatform.domain.usecase

import com.example.healthplatform.domain.model.HealthMetric
import com.example.healthplatform.domain.model.MetricType
import com.example.healthplatform.domain.repository.HealthDataRepository
import java.util.concurrent.TimeUnit

class ImportMockDataUseCase(
    private val repository: HealthDataRepository
) {
    suspend operator fun invoke(userId: String, now: Long) {
        val mockMetrics = mutableListOf<HealthMetric>()

        val dailySteps = listOf(4200.0, 5100.0, 6300.0, 7100.0, 5600.0, 8300.0, 6800.0)
        val dailyHeartRates = listOf(84.0, 82.0, 80.0, 78.0, 81.0, 77.0, 79.0)
        val dailySleep = listOf(6.1, 6.5, 7.2, 6.8, 5.9, 7.4, 7.0)

        for (i in 0 until 7) {
            val dayOffset = 6 - i
            val dayBase = now - TimeUnit.DAYS.toMillis(dayOffset.toLong())

            mockMetrics += HealthMetric(
                userId = userId,
                metricType = MetricType.STEPS,
                value = dailySteps[i],
                unit = "count",
                timestamp = dayBase - TimeUnit.HOURS.toMillis(2),
                source = "mock"
            )

            mockMetrics += HealthMetric(
                userId = userId,
                metricType = MetricType.HEART_RATE,
                value = dailyHeartRates[i],
                unit = "bpm",
                timestamp = dayBase - TimeUnit.HOURS.toMillis(1),
                source = "mock"
            )

            mockMetrics += HealthMetric(
                userId = userId,
                metricType = MetricType.SLEEP_DURATION,
                value = dailySleep[i],
                unit = "hour",
                timestamp = dayBase - TimeUnit.HOURS.toMillis(8),
                source = "mock"
            )
        }

        repository.replaceMetricsForUser(userId, mockMetrics)
    }
}
