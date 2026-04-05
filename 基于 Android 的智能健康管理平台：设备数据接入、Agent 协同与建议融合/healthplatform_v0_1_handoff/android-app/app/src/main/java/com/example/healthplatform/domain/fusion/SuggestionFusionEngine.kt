package com.example.healthplatform.domain.fusion

import com.example.healthplatform.domain.model.AdviceCategory
import com.example.healthplatform.domain.model.AgentResult
import com.example.healthplatform.domain.model.FinalSuggestion

class SuggestionFusionEngine {

    fun fuse(results: List<AgentResult>): List<FinalSuggestion> {
        if (results.isEmpty()) return emptyList()

        val sorted = results.sortedWith(
            compareByDescending<AgentResult> { it.riskLevel }
                .thenByDescending { it.confidence }
        )

        val filtered = resolveConflicts(sorted).take(3)

        return filtered.mapIndexed { index, item ->
            FinalSuggestion(
                title = when (item.category) {
                    AdviceCategory.EXERCISE -> "Exercise Advice"
                    AdviceCategory.DIET -> "Diet Advice"
                    AdviceCategory.SLEEP -> "Sleep Advice"
                    AdviceCategory.RISK -> "Risk Reminder"
                },
                category = item.category,
                content = item.suggestion,
                explanation = item.explanation,
                priority = 3 - index,
                sourceAgents = listOf(item.agentName)
            )
        }
    }

    private fun resolveConflicts(items: List<AgentResult>): List<AgentResult> {
        val sleepHighRisk = items.any {
            it.category == AdviceCategory.SLEEP && it.riskLevel >= 4
        }

        return if (sleepHighRisk) {
            items.filterNot {
                it.category == AdviceCategory.EXERCISE && it.riskLevel <= 2
            }
        } else {
            items
        }
    }
}
