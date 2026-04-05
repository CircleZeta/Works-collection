package com.example.healthplatform.data.mapper

import com.example.healthplatform.data.local.entity.FinalSuggestionEntity
import com.example.healthplatform.domain.model.AdviceCategory
import com.example.healthplatform.domain.model.FinalSuggestion

fun FinalSuggestionEntity.toDomain(): FinalSuggestion {
    return FinalSuggestion(
        title = title,
        category = AdviceCategory.valueOf(category),
        content = content,
        explanation = explanation,
        priority = priority,
        sourceAgents = sourceAgents.split(",").filter { it.isNotBlank() },
        createdAt = createdAt
    )
}

fun FinalSuggestion.toEntity(): FinalSuggestionEntity {
    return FinalSuggestionEntity(
        title = title,
        category = category.name,
        content = content,
        explanation = explanation,
        priority = priority,
        sourceAgents = sourceAgents.joinToString(","),
        createdAt = createdAt
    )
}
