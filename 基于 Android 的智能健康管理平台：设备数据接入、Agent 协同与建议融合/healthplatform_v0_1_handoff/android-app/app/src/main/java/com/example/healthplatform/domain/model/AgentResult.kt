package com.example.healthplatform.domain.model

data class AgentResult(
    val agentName: String,
    val category: AdviceCategory,
    val riskLevel: Int,
    val confidence: Double,
    val suggestion: String,
    val explanation: String,
    val createdAt: Long = System.currentTimeMillis()
)
