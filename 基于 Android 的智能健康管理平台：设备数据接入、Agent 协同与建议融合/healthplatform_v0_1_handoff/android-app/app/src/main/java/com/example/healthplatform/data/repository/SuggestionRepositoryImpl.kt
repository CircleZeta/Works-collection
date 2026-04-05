package com.example.healthplatform.data.repository

import com.example.healthplatform.data.local.dao.FinalSuggestionDao
import com.example.healthplatform.data.mapper.toDomain
import com.example.healthplatform.data.mapper.toEntity
import com.example.healthplatform.domain.model.FinalSuggestion
import com.example.healthplatform.domain.repository.SuggestionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SuggestionRepositoryImpl(
    private val dao: FinalSuggestionDao
) : SuggestionRepository {

    override suspend fun saveSuggestions(items: List<FinalSuggestion>) {
        dao.clearAll()
        dao.insertAll(items.map { it.toEntity() })
    }

    override fun observeSuggestions(): Flow<List<FinalSuggestion>> {
        return dao.observeSuggestions().map { list -> list.map { it.toDomain() } }
    }
}
