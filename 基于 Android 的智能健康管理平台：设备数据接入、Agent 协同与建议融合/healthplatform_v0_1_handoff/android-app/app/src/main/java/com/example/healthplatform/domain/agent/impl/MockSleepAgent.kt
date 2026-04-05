package com.example.healthplatform.domain.agent.impl

import com.example.healthplatform.domain.agent.HealthAgent
import com.example.healthplatform.domain.model.AdviceCategory
import com.example.healthplatform.domain.model.AgentResult
import com.example.healthplatform.domain.model.HealthContext

class MockSleepAgent : HealthAgent {
    override val name: String = "sleep-agent"

    override suspend fun analyze(context: HealthContext): AgentResult {
        val sleep = context.summary.sleepDurationHours ?: 0.0
        return if (sleep < 7.0) {
            AgentResult(
                agentName = name,
                category = AdviceCategory.SLEEP,
                riskLevel = 4,
                confidence = 0.88,
                suggestion = "Aim to sleep before 23:00 and reduce late-night screen time.",
                explanation = "Sleep duration is below the recovery baseline."
            )
        } else {
            AgentResult(
                agentName = name,
                category = AdviceCategory.SLEEP,
                riskLevel = 1,
                confidence = 0.81,
                suggestion = "Keep the current sleep schedule tonight.",
                explanation = "Sleep duration is within a basic recovery range."
            )
        }
    }
}
