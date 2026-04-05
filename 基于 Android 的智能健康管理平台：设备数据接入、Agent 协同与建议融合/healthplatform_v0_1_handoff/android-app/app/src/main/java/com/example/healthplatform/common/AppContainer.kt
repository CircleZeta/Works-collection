package com.example.healthplatform.common

import android.content.Context
import androidx.room.Room
import com.example.healthplatform.data.local.db.AppDatabase
import com.example.healthplatform.data.repository.HealthDataRepositoryImpl
import com.example.healthplatform.data.repository.SuggestionRepositoryImpl
import com.example.healthplatform.domain.agent.AgentCoordinator
import com.example.healthplatform.domain.agent.impl.MockDietAgent
import com.example.healthplatform.domain.agent.impl.MockExerciseAgent
import com.example.healthplatform.domain.agent.impl.MockSleepAgent
import com.example.healthplatform.domain.fusion.SuggestionFusionEngine
import com.example.healthplatform.domain.repository.HealthDataRepository
import com.example.healthplatform.domain.repository.SuggestionRepository
import com.example.healthplatform.domain.usecase.GenerateSuggestionsUseCase
import com.example.healthplatform.domain.usecase.GetDashboardSummaryUseCase
import com.example.healthplatform.domain.usecase.GetTrendDataUseCase
import com.example.healthplatform.domain.usecase.ImportMockDataUseCase

class AppContainer(context: Context) {

    val refreshTicker = AppRefreshTicker()

    private val database: AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "health_platform.db"
    ).build()

    val healthDataRepository: HealthDataRepository =
        HealthDataRepositoryImpl(database.healthMetricDao())

    val suggestionRepository: SuggestionRepository =
        SuggestionRepositoryImpl(database.finalSuggestionDao())

    private val agentCoordinator = AgentCoordinator(
        agents = listOf(
            MockExerciseAgent(),
            MockDietAgent(),
            MockSleepAgent()
        )
    )

    private val fusionEngine = SuggestionFusionEngine()

    val generateSuggestionsUseCase = GenerateSuggestionsUseCase(
        healthDataRepository = healthDataRepository,
        suggestionRepository = suggestionRepository,
        coordinator = agentCoordinator,
        fusionEngine = fusionEngine
    )

    val importMockDataUseCase = ImportMockDataUseCase(
        repository = healthDataRepository
    )

    val getDashboardSummaryUseCase = GetDashboardSummaryUseCase(
        repository = healthDataRepository
    )

    val getTrendDataUseCase = GetTrendDataUseCase(
        repository = healthDataRepository
    )
}
