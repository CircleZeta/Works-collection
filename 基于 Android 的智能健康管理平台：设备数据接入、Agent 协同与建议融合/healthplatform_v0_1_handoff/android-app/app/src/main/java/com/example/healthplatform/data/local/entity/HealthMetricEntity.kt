package com.example.healthplatform.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_metrics")
data class HealthMetricEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userId: String,
    val metricType: String,
    val value: Double,
    val unit: String,
    val timestamp: Long,
    val source: String
)
