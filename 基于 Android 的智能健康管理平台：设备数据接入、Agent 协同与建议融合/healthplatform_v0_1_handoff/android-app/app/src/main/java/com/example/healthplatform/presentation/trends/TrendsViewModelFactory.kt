package com.example.healthplatform.presentation.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.healthplatform.domain.usecase.GetTrendDataUseCase

class TrendsViewModelFactory(
    private val getTrendDataUseCase: GetTrendDataUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TrendsViewModel(
            getTrendDataUseCase = getTrendDataUseCase
        ) as T
    }
}
