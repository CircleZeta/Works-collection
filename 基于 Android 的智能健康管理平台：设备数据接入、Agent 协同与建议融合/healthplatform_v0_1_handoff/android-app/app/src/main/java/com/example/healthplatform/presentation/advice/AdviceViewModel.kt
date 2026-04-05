package com.example.healthplatform.presentation.advice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthplatform.common.AppRefreshTicker
import com.example.healthplatform.domain.repository.SuggestionRepository
import com.example.healthplatform.domain.usecase.GenerateSuggestionsUseCase
import com.example.healthplatform.domain.usecase.ImportMockDataUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdviceViewModel(
    private val generateSuggestionsUseCase: GenerateSuggestionsUseCase,
    private val importMockDataUseCase: ImportMockDataUseCase,
    private val suggestionRepository: SuggestionRepository,
    private val refreshTicker: AppRefreshTicker
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdviceUiState())
    val uiState: StateFlow<AdviceUiState> = _uiState.asStateFlow()

    init {
        observeSuggestions()
    }

    private fun observeSuggestions() {
        viewModelScope.launch {
            suggestionRepository.observeSuggestions().collect { items ->
                _uiState.value = _uiState.value.copy(
                    suggestions = items,
                    loading = false,
                    error = null
                )
            }
        }
    }

    fun importMockData(userId: String, now: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            runCatching {
                importMockDataUseCase(userId, now)
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = it.message ?: "Failed to import mock data"
                )
            }.onSuccess {
                refreshTicker.bump()
                _uiState.value = _uiState.value.copy(loading = false)
            }
        }
    }

    fun refresh(userId: String, start: Long, end: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            runCatching {
                generateSuggestionsUseCase(userId, start, end)
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = it.message ?: "Failed to generate suggestions"
                )
            }.onSuccess {
                refreshTicker.bump()
            }
        }
    }
}
