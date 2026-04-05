package com.example.healthplatform.domain.agent.impl

import com.example.healthplatform.domain.agent.HealthAgent
import com.example.healthplatform.domain.model.AdviceCategory
import com.example.healthplatform.domain.model.AgentResult
import com.example.healthplatform.domain.model.HealthContext

class MockExerciseAgent : HealthAgent {
    override val name: String = "exercise-agent"

    override suspend fun analyze(context: HealthContext): AgentResult {
        val steps = context.summary.todaySteps
        return if (steps < 6000) {
            AgentResult(
                agentName = name,
                category = AdviceCategory.EXERCISE,
                riskLevel = 3,
                confidence = 0.82,
                suggestion = "Today, add 20 to 30 minutes of low-to-moderate walking.",
                explanation = "Current step count is low and daily activity is insufficient."
            )
        } else {
            AgentResult(
                agentName = name,
                category = AdviceCategory.EXERCISE,
                riskLevel = 1,
                confidence = 0.78,
                suggestion = "Current activity is acceptable. Maintain today's pace.",
                explanation = "Current step count is near the daily target."
            )
        }
    }
}
