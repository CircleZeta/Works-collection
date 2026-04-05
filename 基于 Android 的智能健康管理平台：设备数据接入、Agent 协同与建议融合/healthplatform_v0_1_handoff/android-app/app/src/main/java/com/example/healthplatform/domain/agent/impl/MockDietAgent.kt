package com.example.healthplatform.domain.agent.impl

import com.example.healthplatform.domain.agent.HealthAgent
import com.example.healthplatform.domain.model.AdviceCategory
import com.example.healthplatform.domain.model.AgentResult
import com.example.healthplatform.domain.model.HealthContext

class MockDietAgent : HealthAgent {
    override val name: String = "diet-agent"

    override suspend fun analyze(context: HealthContext): AgentResult {
        val sleep = context.summary.sleepDurationHours ?: 0.0
        return if (sleep < 6.5) {
            AgentResult(
                agentName = name,
                category = AdviceCategory.DIET,
                riskLevel = 2,
                confidence = 0.76,
                suggestion = "Reduce late-day caffeine and avoid a heavy dinner.",
                explanation = "Recent sleep duration is limited, so dietary stimulation should be reduced."
            )
        } else {
            AgentResult(
                agentName = name,
                category = AdviceCategory.DIET,
                riskLevel = 1,
                confidence = 0.70,
                suggestion = "Maintain regular meals and adequate hydration.",
                explanation = "Current recovery is acceptable, so stable intake is recommended."
            )
        }
    }
}
