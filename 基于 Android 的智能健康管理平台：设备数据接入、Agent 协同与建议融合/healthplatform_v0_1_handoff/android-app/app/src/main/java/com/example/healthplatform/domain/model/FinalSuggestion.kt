package com.example.healthplatform.domain.model

data class FinalSuggestion(
    val title: String,
    val category: AdviceCategory,
    val content: String,
    val explanation: String,
    val priority: Int,
    val sourceAgents: List<String>,
    val createdAt: Long = System.currentTimeMillis()
)
