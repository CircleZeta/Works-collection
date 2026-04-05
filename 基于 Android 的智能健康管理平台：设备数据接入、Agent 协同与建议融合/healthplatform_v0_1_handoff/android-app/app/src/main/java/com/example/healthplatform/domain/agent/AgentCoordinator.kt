package com.example.healthplatform.domain.agent

import com.example.healthplatform.domain.model.AgentResult
import com.example.healthplatform.domain.model.HealthContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class AgentCoordinator(
    private val agents: List<HealthAgent>
) {
    suspend fun execute(context: HealthContext): List<AgentResult> = coroutineScope {
        agents.map { agent ->
            async {
                runCatching { agent.analyze(context) }.getOrNull()
            }
        }.awaitAll().filterNotNull()
    }
}
