package com.example.healthplatform.presentation.advice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.healthplatform.common.AppRefreshTicker
import com.example.healthplatform.domain.repository.SuggestionRepository
import com.example.healthplatform.domain.usecase.GenerateSuggestionsUseCase
import com.example.healthplatform.domain.usecase.ImportMockDataUseCase

class AdviceViewModelFactory(
    private val generateSuggestionsUseCase: GenerateSuggestionsUseCase,
    private val importMockDataUseCase: ImportMockDataUseCase,
    private val suggestionRepository: SuggestionRepository,
    private val refreshTicker: AppRefreshTicker
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AdviceViewModel(
            generateSuggestionsUseCase = generateSuggestionsUseCase,
            importMockDataUseCase = importMockDataUseCase,
            suggestionRepository = suggestionRepository,
            refreshTicker = refreshTicker
        ) as T
    }
}
