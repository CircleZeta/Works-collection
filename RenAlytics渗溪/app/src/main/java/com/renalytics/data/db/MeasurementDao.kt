package com.renalytics.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.renalytics.data.models.Measurement
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {
    @Insert
    suspend fun insert(measurement: Measurement)

    @Query("SELECT * FROM measurements WHERE userId = :userId ORDER BY timestamp DESC")
    fun getMeasurementsByUserId(userId: Long): Flow<List<Measurement>>

    @Query("SELECT * FROM measurements WHERE userId = :userId AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    fun getMeasurementsByUserIdAndTimeRange(
        userId: Long,
        startTime: Long,
        endTime: Long
    ): Flow<List<Measurement>>

    @Query("SELECT * FROM measurements WHERE id = :id")
    suspend fun getMeasurementById(id: Long): Measurement?

    @Query("DELETE FROM measurements WHERE userId = :userId")
    suspend fun deleteMeasurementsByUserId(userId: Long)

    @Query("SELECT * FROM measurements WHERE userId = :userId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestMeasurementByUserId(userId: Long): Measurement?
}
