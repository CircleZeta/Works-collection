package com.example.healthplatform.data.mapper

import com.example.healthplatform.data.local.entity.HealthMetricEntity
import com.example.healthplatform.domain.model.HealthMetric
import com.example.healthplatform.domain.model.MetricType

fun HealthMetricEntity.toDomain(): HealthMetric {
    return HealthMetric(
        id = id,
        userId = userId,
        metricType = MetricType.valueOf(metricType),
        value = value,
        unit = unit,
        timestamp = timestamp,
        source = source
    )
}

fun HealthMetric.toEntity(): HealthMetricEntity {
    return HealthMetricEntity(
        id = id,
        userId = userId,
        metricType = metricType.name,
        value = value,
        unit = unit,
        timestamp = timestamp,
        source = source
    )
}
