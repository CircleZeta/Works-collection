package com.example.healthplatform.domain.usecase

import com.example.healthplatform.domain.model.HealthSummary
import com.example.healthplatform.domain.repository.HealthDataRepository

class GetDashboardSummaryUseCase(
    private val repository: HealthDataRepository
) {
    suspend operator fun invoke(
        userId: String,
        start: Long,
        end: Long
    ): HealthSummary {
        return repository.buildSummary(userId, start, end)
    }
}
