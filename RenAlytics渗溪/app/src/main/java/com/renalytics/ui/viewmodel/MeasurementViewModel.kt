package com.renalytics.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renalytics.data.repository.MeasurementRepository
import com.renalytics.data.models.Measurement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MeasurementViewModel(private val measurementRepository: MeasurementRepository) : ViewModel() {
    fun getMeasurementsByUserId(userId: Long): Flow<List<Measurement>> {
        return measurementRepository.getMeasurementsByUserId(userId)
    }

    fun getMeasurementsByUserIdAndTimeRange(
        userId: Long,
        startTime: Long,
        endTime: Long
    ): Flow<List<Measurement>> {
        return measurementRepository.getMeasurementsByUserIdAndTimeRange(userId, startTime, endTime)
    }

    fun addMeasurement(measurement: Measurement) {
        viewModelScope.launch {
            measurementRepository.insertMeasurement(measurement)
        }
    }

    suspend fun getLatestMeasurementByUserId(userId: Long): Measurement? {
        return measurementRepository.getLatestMeasurementByUserId(userId)
    }

    suspend fun getMeasurementById(id: Long): Measurement? {
        return measurementRepository.getMeasurementById(id)
    }

    fun deleteMeasurementsByUserId(userId: Long) {
        viewModelScope.launch {
            measurementRepository.deleteMeasurementsByUserId(userId)
        }
    }
}
