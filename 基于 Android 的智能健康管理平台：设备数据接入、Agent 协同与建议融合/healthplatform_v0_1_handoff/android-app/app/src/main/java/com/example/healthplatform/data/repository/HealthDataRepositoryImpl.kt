package com.example.healthplatform.data.repository

import com.example.healthplatform.data.local.dao.HealthMetricDao
import com.example.healthplatform.data.mapper.toDomain
import com.example.healthplatform.data.mapper.toEntity
import com.example.healthplatform.domain.model.HealthMetric
import com.example.healthplatform.domain.model.HealthSummary
import com.example.healthplatform.domain.model.MetricType
import com.example.healthplatform.domain.model.TrendPoint
import com.example.healthplatform.domain.repository.HealthDataRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class HealthDataRepositoryImpl(
    private val dao: HealthMetricDao
) : HealthDataRepository {

    override suspend fun replaceMetricsForUser(userId: String, metrics: List<HealthMetric>) {
        dao.deleteByUser(userId)
        dao.insertAll(metrics.map { it.toEntity() })
    }

    override suspend fun saveMetrics(metrics: List<HealthMetric>) {
        dao.insertAll(metrics.map { it.toEntity() })
    }

    override suspend fun getMetrics(userId: String, start: Long, end: Long): List<HealthMetric> {
        return dao.getMetricsByRange(userId, start, end).map { it.toDomain() }
    }

    override suspend fun buildSummary(userId: String, start: Long, end: Long): HealthSummary {
        val all = dao.getMetricsByRange(userId, start, end).map { it.toDomain() }

        val heartRates = all.filter { it.metricType == MetricType.HEART_RATE }.map { it.value }
        val steps = all.filter { it.metricType == MetricType.STEPS }.sumOf { it.value }.toInt()
        val sleepDuration = all.filter { it.metricType == MetricType.SLEEP_DURATION }.sumOf { it.value }
        val deepSleep = all.filter { it.metricType == MetricType.SLEEP_DEEP }.sumOf { it.value }
        val lightSleep = all.filter { it.metricType == MetricType.SLEEP_LIGHT }.sumOf { it.value }

        return HealthSummary(
            avgHeartRate = heartRates.average().takeIf { heartRates.isNotEmpty() },
            restingHeartRate = heartRates.minOrNull(),
            todaySteps = steps,
            yesterdaySteps = 0,
            sleepDurationHours = sleepDuration.takeIf { sleepDuration > 0.0 },
            deepSleepHours = deepSleep.takeIf { deepSleep > 0.0 },
            lightSleepHours = lightSleep.takeIf { lightSleep > 0.0 },
            stepTrendRatio = null,
            sleepTrendRatio = null
        )
    }

    override suspend fun buildTrend(
        userId: String,
        metricType: MetricType,
        days: Int,
        endTime: Long
    ): List<TrendPoint> {
        val startTime = endTime - TimeUnit.DAYS.toMillis(days.toLong() - 1)
        val metrics = dao.getMetricsByTypeAndRange(
            userId = userId,
            metricType = metricType.name,
            start = startOfDay(startTime),
            end = endTime
        ).map { it.toDomain() }

        val formatter = SimpleDateFormat("MM-dd", Locale.getDefault())

        return metrics
            .groupBy { startOfDay(it.timestamp) }
            .toSortedMap()
            .map { (dayStart, items) ->
                val aggregated = when (metricType) {
                    MetricType.HEART_RATE -> items.map { it.value }.average()
                    MetricType.STEPS,
                    MetricType.SLEEP_DURATION,
                    MetricType.SLEEP_DEEP,
                    MetricType.SLEEP_LIGHT -> items.sumOf { it.value }
                }

                TrendPoint(
                    dayLabel = formatter.format(Date(dayStart)),
                    metricType = metricType,
                    value = aggregated
                )
            }
    }

    private fun startOfDay(time: Long): Long {
        val dayMillis = TimeUnit.DAYS.toMillis(1)
        return time / dayMillis * dayMillis
    }
}
