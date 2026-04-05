package com.renalytics.data.repository

import com.renalytics.data.db.MeasurementDao
import com.renalytics.data.models.Measurement
import kotlinx.coroutines.flow.Flow

class MeasurementRepository(private val measurementDao: MeasurementDao) {
    suspend fun insertMeasurement(measurement: Measurement) {
        measurementDao.insert(measurement)
    }

    fun getMeasurementsByUserId(userId: Long): Flow<List<Measurement>> {
        return measurementDao.getMeasurementsByUserId(userId)
    }

    fun getMeasurementsByUserIdAndTimeRange(
        userId: Long,
        startTime: Long,
        endTime: Long
    ): Flow<List<Measurement>> {
        return measurementDao.getMeasurementsByUserIdAndTimeRange(userId, startTime, endTime)
    }

    suspend fun getMeasurementById(id: Long): Measurement? {
        return measurementDao.getMeasurementById(id)
    }

    suspend fun deleteMeasurementsByUserId(userId: Long) {
        measurementDao.deleteMeasurementsByUserId(userId)
    }

    suspend fun getLatestMeasurementByUserId(userId: Long): Measurement? {
        return measurementDao.getLatestMeasurementByUserId(userId)
    }
}
