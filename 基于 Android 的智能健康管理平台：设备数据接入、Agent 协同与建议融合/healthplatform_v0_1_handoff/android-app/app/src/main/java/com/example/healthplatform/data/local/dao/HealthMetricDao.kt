package com.example.healthplatform.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.healthplatform.data.local.entity.HealthMetricEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthMetricDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(metric: HealthMetricEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(metrics: List<HealthMetricEntity>)

    @Query(
        """
        SELECT * FROM health_metrics
        WHERE userId = :userId
        AND timestamp BETWEEN :start AND :end
        ORDER BY timestamp ASC
        """
    )
    suspend fun getMetricsByRange(
        userId: String,
        start: Long,
        end: Long
    ): List<HealthMetricEntity>

    @Query(
        """
        SELECT * FROM health_metrics
        WHERE userId = :userId
        AND metricType = :metricType
        AND timestamp BETWEEN :start AND :end
        ORDER BY timestamp ASC
        """
    )
    suspend fun getMetricsByTypeAndRange(
        userId: String,
        metricType: String,
        start: Long,
        end: Long
    ): List<HealthMetricEntity>

    @Query(
        """
        SELECT * FROM health_metrics
        WHERE userId = :userId
        ORDER BY timestamp DESC
        LIMIT :limit
        """
    )
    fun observeRecentMetrics(userId: String, limit: Int = 20): Flow<List<HealthMetricEntity>>

    @Query("DELETE FROM health_metrics WHERE userId = :userId")
    suspend fun deleteByUser(userId: String)
}
