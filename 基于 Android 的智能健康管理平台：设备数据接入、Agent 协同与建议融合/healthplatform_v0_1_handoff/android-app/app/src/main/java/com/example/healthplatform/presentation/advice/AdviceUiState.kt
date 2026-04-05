package com.example.healthplatform.presentation.advice

import com.example.healthplatform.domain.model.FinalSuggestion

data class AdviceUiState(
    val suggestions: List<FinalSuggestion> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)
