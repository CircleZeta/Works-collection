package com.example.healthplatform.domain.agent

import com.example.healthplatform.domain.model.AgentResult
import com.example.healthplatform.domain.model.HealthContext

interface HealthAgent {
    val name: String
    suspend fun analyze(context: HealthContext): AgentResult
}
