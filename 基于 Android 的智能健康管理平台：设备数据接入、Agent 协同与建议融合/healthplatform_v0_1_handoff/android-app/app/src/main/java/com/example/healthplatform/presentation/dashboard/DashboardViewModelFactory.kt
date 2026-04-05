package com.example.healthplatform.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.healthplatform.domain.usecase.GetDashboardSummaryUseCase

class DashboardViewModelFactory(
    private val getDashboardSummaryUseCase: GetDashboardSummaryUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DashboardViewModel(
            getDashboardSummaryUseCase = getDashboardSummaryUseCase
        ) as T
    }
}
