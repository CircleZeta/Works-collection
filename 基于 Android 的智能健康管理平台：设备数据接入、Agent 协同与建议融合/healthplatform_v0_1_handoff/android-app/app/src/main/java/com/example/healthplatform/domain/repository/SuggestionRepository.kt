package com.example.healthplatform.domain.repository

import com.example.healthplatform.domain.model.FinalSuggestion
import kotlinx.coroutines.flow.Flow

interface SuggestionRepository {
    suspend fun saveSuggestions(items: List<FinalSuggestion>)
    fun observeSuggestions(): Flow<List<FinalSuggestion>>
}
