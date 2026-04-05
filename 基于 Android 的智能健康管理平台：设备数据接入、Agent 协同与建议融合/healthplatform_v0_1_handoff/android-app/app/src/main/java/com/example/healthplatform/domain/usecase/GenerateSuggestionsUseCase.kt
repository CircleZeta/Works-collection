package com.example.healthplatform.domain.usecase

import com.example.healthplatform.domain.agent.AgentCoordinator
import com.example.healthplatform.domain.fusion.SuggestionFusionEngine
import com.example.healthplatform.domain.model.HealthContext
import com.example.healthplatform.domain.repository.HealthDataRepository
import com.example.healthplatform.domain.repository.SuggestionRepository

class GenerateSuggestionsUseCase(
    private val healthDataRepository: HealthDataRepository,
    private val suggestionRepository: SuggestionRepository,
    private val coordinator: AgentCoordinator,
    private val fusionEngine: SuggestionFusionEngine
) {
    suspend operator fun invoke(
        userId: String,
        start: Long,
        end: Long
    ) {
        val summary = healthDataRepository.buildSummary(userId, start, end)
        val context = HealthContext(
            userId = userId,
            startTime = start,
            endTime = end,
            summary = summary
        )
        val agentResults = coordinator.execute(context)
        val finalSuggestions = fusionEngine.fuse(agentResults)
        suggestionRepository.saveSuggestions(finalSuggestions)
    }
}
