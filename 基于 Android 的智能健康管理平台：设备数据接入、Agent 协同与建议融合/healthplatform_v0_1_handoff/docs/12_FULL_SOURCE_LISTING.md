# 12_FULL_SOURCE_LISTING.md
## 用途
本文件将当前 handoff 包中的关键源码与配置按路径串联展示，便于 Codex 一次性阅读。

## android-app/app/build.gradle.kts

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.healthplatform"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.healthplatform"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")

    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")

    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
}

```

## android-app/app/proguard-rules.pro

```
# No special ProGuard rules required for v0.1

```

## android-app/app/src/main/AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:allowBackup="true"
        android:label="HealthPlatform"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.Material.Light.NoActionBar">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>

```

## android-app/app/src/main/java/com/example/healthplatform/MainActivity.kt

```kotlin
package com.example.healthplatform

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.healthplatform.common.AppContainer
import com.example.healthplatform.ui.HealthPlatformApp
import com.example.healthplatform.ui.theme.HealthPlatformTheme

class MainActivity : ComponentActivity() {

    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appContainer = AppContainer(applicationContext)

        setContent {
            HealthPlatformTheme {
                HealthPlatformApp(appContainer = appContainer)
            }
        }
    }
}

```

## android-app/app/src/main/java/com/example/healthplatform/common/AppContainer.kt

```kotlin
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

```

## android-app/app/src/main/java/com/example/healthplatform/data/local/dao/FinalSuggestionDao.kt

```kotlin
package com.example.healthplatform.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.healthplatform.data.local.entity.FinalSuggestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FinalSuggestionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<FinalSuggestionEntity>)

    @Query("DELETE FROM final_suggestions")
    suspend fun clearAll()

    @Query(
        """
        SELECT * FROM final_suggestions
        ORDER BY priority DESC, createdAt DESC
        """
    )
    fun observeSuggestions(): Flow<List<FinalSuggestionEntity>>
}

```

## android-app/app/src/main/java/com/example/healthplatform/data/local/dao/HealthMetricDao.kt

```kotlin
package com.example.healthplatform.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.healthplatform.data.local.entity.HealthMetricEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthMetricDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(metric: HealthMetricEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(metrics: List<HealthMetricEntity>)

    @Query(
        """
        SELECT * FROM health_metrics
        WHERE userId = :userId
        AND timestamp BETWEEN :start AND :end
        ORDER BY timestamp ASC
        """
    )
    suspend fun getMetricsByRange(
        userId: String,
        start: Long,
        end: Long
    ): List<HealthMetricEntity>

    @Query(
        """
        SELECT * FROM health_metrics
        WHERE userId = :userId
        AND metricType = :metricType
        AND timestamp BETWEEN :start AND :end
        ORDER BY timestamp ASC
        """
    )
    suspend fun getMetricsByTypeAndRange(
        userId: String,
        metricType: String,
        start: Long,
        end: Long
    ): List<HealthMetricEntity>

    @Query(
        """
        SELECT * FROM health_metrics
        WHERE userId = :userId
        ORDER BY timestamp DESC
        LIMIT :limit
        """
    )
    fun observeRecentMetrics(userId: String, limit: Int = 20): Flow<List<HealthMetricEntity>>

    @Query("DELETE FROM health_metrics WHERE userId = :userId")
    suspend fun deleteByUser(userId: String)
}

```

## android-app/app/src/main/java/com/example/healthplatform/data/local/db/AppDatabase.kt

```kotlin
package com.example.healthplatform.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.healthplatform.data.local.dao.FinalSuggestionDao
import com.example.healthplatform.data.local.dao.HealthMetricDao
import com.example.healthplatform.data.local.entity.FinalSuggestionEntity
import com.example.healthplatform.data.local.entity.HealthMetricEntity

@Database(
    entities = [HealthMetricEntity::class, FinalSuggestionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun healthMetricDao(): HealthMetricDao
    abstract fun finalSuggestionDao(): FinalSuggestionDao
}

```

## android-app/app/src/main/java/com/example/healthplatform/data/local/entity/FinalSuggestionEntity.kt

```kotlin
package com.example.healthplatform.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "final_suggestions")
data class FinalSuggestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val category: String,
    val content: String,
    val explanation: String,
    val priority: Int,
    val sourceAgents: String,
    val createdAt: Long
)

```

## android-app/app/src/main/java/com/example/healthplatform/data/local/entity/HealthMetricEntity.kt

```kotlin
package com.example.healthplatform.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_metrics")
data class HealthMetricEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userId: String,
    val metricType: String,
    val value: Double,
    val unit: String,
    val timestamp: Long,
    val source: String
)

```

## android-app/app/src/main/java/com/example/healthplatform/data/mapper/FinalSuggestionMapper.kt

```kotlin
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

```

## android-app/app/src/main/java/com/example/healthplatform/data/mapper/HealthMetricMapper.kt

```kotlin
package com.example.healthplatform.data.mapper

import com.example.healthplatform.data.local.entity.HealthMetricEntity
import com.example.healthplatform.domain.model.HealthMetric
import com.example.healthplatform.domain.model.MetricType

fun HealthMetricEntity.toDomain(): HealthMetric {
    return HealthMetric(
        id = id,
        userId = userId,
        metricType = MetricType.valueOf(metricType),
        value = value,
        unit = unit,
        timestamp = timestamp,
        source = source
    )
}

fun HealthMetric.toEntity(): HealthMetricEntity {
    return HealthMetricEntity(
        id = id,
        userId = userId,
        metricType = metricType.name,
        value = value,
        unit = unit,
        timestamp = timestamp,
        source = source
    )
}

```

## android-app/app/src/main/java/com/example/healthplatform/data/repository/HealthDataRepositoryImpl.kt

```kotlin
package com.example.healthplatform.data.repository

import com.example.healthplatform.data.local.dao.HealthMetricDao
import com.example.healthplatform.data.mapper.toDomain
import com.example.healthplatform.data.mapper.toEntity
import com.example.healthplatform.domain.model.HealthMetric
import com.example.healthplatform.domain.model.HealthSummary
import com.example.healthplatform.domain.model.MetricType
import com.example.healthplatform.domain.model.TrendPoint
import com.example.healthplatform.domain.repository.HealthDataRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class HealthDataRepositoryImpl(
    private val dao: HealthMetricDao
) : HealthDataRepository {

    override suspend fun replaceMetricsForUser(userId: String, metrics: List<HealthMetric>) {
        dao.deleteByUser(userId)
        dao.insertAll(metrics.map { it.toEntity() })
    }

    override suspend fun saveMetrics(metrics: List<HealthMetric>) {
        dao.insertAll(metrics.map { it.toEntity() })
    }

    override suspend fun getMetrics(userId: String, start: Long, end: Long): List<HealthMetric> {
        return dao.getMetricsByRange(userId, start, end).map { it.toDomain() }
    }

    override suspend fun buildSummary(userId: String, start: Long, end: Long): HealthSummary {
        val all = dao.getMetricsByRange(userId, start, end).map { it.toDomain() }

        val heartRates = all.filter { it.metricType == MetricType.HEART_RATE }.map { it.value }
        val steps = all.filter { it.metricType == MetricType.STEPS }.sumOf { it.value }.toInt()
        val sleepDuration = all.filter { it.metricType == MetricType.SLEEP_DURATION }.sumOf { it.value }
        val deepSleep = all.filter { it.metricType == MetricType.SLEEP_DEEP }.sumOf { it.value }
        val lightSleep = all.filter { it.metricType == MetricType.SLEEP_LIGHT }.sumOf { it.value }

        return HealthSummary(
            avgHeartRate = heartRates.average().takeIf { heartRates.isNotEmpty() },
            restingHeartRate = heartRates.minOrNull(),
            todaySteps = steps,
            yesterdaySteps = 0,
            sleepDurationHours = sleepDuration.takeIf { sleepDuration > 0.0 },
            deepSleepHours = deepSleep.takeIf { deepSleep > 0.0 },
            lightSleepHours = lightSleep.takeIf { lightSleep > 0.0 },
            stepTrendRatio = null,
            sleepTrendRatio = null
        )
    }

    override suspend fun buildTrend(
        userId: String,
        metricType: MetricType,
        days: Int,
        endTime: Long
    ): List<TrendPoint> {
        val startTime = endTime - TimeUnit.DAYS.toMillis(days.toLong() - 1)
        val metrics = dao.getMetricsByTypeAndRange(
            userId = userId,
            metricType = metricType.name,
            start = startOfDay(startTime),
            end = endTime
        ).map { it.toDomain() }

        val formatter = SimpleDateFormat("MM-dd", Locale.getDefault())

        return metrics
            .groupBy { startOfDay(it.timestamp) }
            .toSortedMap()
            .map { (dayStart, items) ->
                val aggregated = when (metricType) {
                    MetricType.HEART_RATE -> items.map { it.value }.average()
                    MetricType.STEPS,
                    MetricType.SLEEP_DURATION,
                    MetricType.SLEEP_DEEP,
                    MetricType.SLEEP_LIGHT -> items.sumOf { it.value }
                }

                TrendPoint(
                    dayLabel = formatter.format(Date(dayStart)),
                    metricType = metricType,
                    value = aggregated
                )
            }
    }

    private fun startOfDay(time: Long): Long {
        val dayMillis = TimeUnit.DAYS.toMillis(1)
        return time / dayMillis * dayMillis
    }
}

```

## android-app/app/src/main/java/com/example/healthplatform/data/repository/SuggestionRepositoryImpl.kt

```kotlin
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

```

## android-app/app/src/main/java/com/example/healthplatform/domain/agent/AgentCoordinator.kt

```kotlin
package com.example.healthplatform.domain.agent

import com.example.healthplatform.domain.model.AgentResult
import com.example.healthplatform.domain.model.HealthContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class AgentCoordinator(
    private val agents: List<HealthAgent>
) {
    suspend fun execute(context: HealthContext): List<AgentResult> = coroutineScope {
        agents.map { agent ->
            async {
                runCatching { agent.analyze(context) }.getOrNull()
            }
        }.awaitAll().filterNotNull()
    }
}

```

## android-app/app/src/main/java/com/example/healthplatform/domain/agent/HealthAgent.kt

```kotlin
package com.example.healthplatform.domain.agent

import com.example.healthplatform.domain.model.AgentResult
import com.example.healthplatform.domain.model.HealthContext

interface HealthAgent {
    val name: String
    suspend fun analyze(context: HealthContext): AgentResult
}

```

## android-app/app/src/main/java/com/example/healthplatform/domain/agent/impl/MockDietAgent.kt

```kotlin
package com.example.healthplatform.domain.agent.impl

import com.example.healthplatform.domain.agent.HealthAgent
import com.example.healthplatform.domain.model.AdviceCategory
import com.example.healthplatform.domain.model.AgentResult
import com.example.healthplatform.domain.model.HealthContext

class MockDietAgent : HealthAgent {
    override val name: String = "diet-agent"

    override suspend fun analyze(context: HealthContext): AgentResult {
        val sleep = context.summary.sleepDurationHours ?: 0.0
        return if (sleep < 6.5) {
            AgentResult(
                agentName = name,
                category = AdviceCategory.DIET,
                riskLevel = 2,
                confidence = 0.76,
                suggestion = "Reduce late-day caffeine and avoid a heavy dinner.",
                explanation = "Recent sleep duration is limited, so dietary stimulation should be reduced."
            )
        } else {
            AgentResult(
                agentName = name,
                category = AdviceCategory.DIET,
                riskLevel = 1,
                confidence = 0.70,
                suggestion = "Maintain regular meals and adequate hydration.",
                explanation = "Current recovery is acceptable, so stable intake is recommended."
            )
        }
    }
}

```

## android-app/app/src/main/java/com/example/healthplatform/domain/agent/impl/MockExerciseAgent.kt

```kotlin
package com.example.healthplatform.domain.agent.impl

import com.example.healthplatform.domain.agent.HealthAgent
import com.example.healthplatform.domain.model.AdviceCategory
import com.example.healthplatform.domain.model.AgentResult
import com.example.healthplatform.domain.model.HealthContext

class MockExerciseAgent : HealthAgent {
    override val name: String = "exercise-agent"

    override suspend fun analyze(context: HealthContext): AgentResult {
        val steps = context.summary.todaySteps
        return if (steps < 6000) {
            AgentResult(
                agentName = name,
                category = AdviceCategory.EXERCISE,
                riskLevel = 3,
                confidence = 0.82,
                suggestion = "Today, add 20 to 30 minutes of low-to-moderate walking.",
                explanation = "Current step count is low and daily activity is insufficient."
            )
        } else {
            AgentResult(
                agentName = name,
                category = AdviceCategory.EXERCISE,
                riskLevel = 1,
                confidence = 0.78,
                suggestion = "Current activity is acceptable. Maintain today's pace.",
                explanation = "Current step count is near the daily target."
            )
        }
    }
}

```

## android-app/app/src/main/java/com/example/healthplatform/domain/agent/impl/MockSleepAgent.kt

```kotlin
package com.example.healthplatform.domain.agent.impl

import com.example.healthplatform.domain.agent.HealthAgent
import com.example.healthplatform.domain.model.AdviceCategory
import com.example.healthplatform.domain.model.AgentResult
import com.example.healthplatform.domain.model.HealthContext

class MockSleepAgent : HealthAgent {
    override val name: String = "sleep-agent"

    override suspend fun analyze(context: HealthContext): AgentResult {
        val sleep = context.summary.sleepDurationHours ?: 0.0
        return if (sleep < 7.0) {
            AgentResult(
                agentName = name,
                category = AdviceCategory.SLEEP,
                riskLevel = 4,
                confidence = 0.88,
                suggestion = "Aim to sleep before 23:00 and reduce late-night screen time.",
                explanation = "Sleep duration is below the recovery baseline."
            )
        } else {
            AgentResult(
                agentName = name,
                category = AdviceCategory.SLEEP,
                riskLevel = 1,
                confidence = 0.81,
                suggestion = "Keep the current sleep schedule tonight.",
                explanation = "Sleep duration is within a basic recovery range."
            )
        }
    }
}

```

## android-app/app/src/main/java/com/example/healthplatform/domain/fusion/SuggestionFusionEngine.kt

```kotlin
package com.example.healthplatform.domain.fusion

import com.example.healthplatform.domain.model.AdviceCategory
import com.example.healthplatform.domain.model.AgentResult
import com.example.healthplatform.domain.model.FinalSuggestion

class SuggestionFusionEngine {

    fun fuse(results: List<AgentResult>): List<FinalSuggestion> {
        if (results.isEmpty()) return emptyList()

        val sorted = results.sortedWith(
            compareByDescending<AgentResult> { it.riskLevel }
                .thenByDescending { it.confidence }
        )

        val filtered = resolveConflicts(sorted).take(3)

        return filtered.mapIndexed { index, item ->
            FinalSuggestion(
                title = when (item.category) {
                    AdviceCategory.EXERCISE -> "Exercise Advice"
                    AdviceCategory.DIET -> "Diet Advice"
                    AdviceCategory.SLEEP -> "Sleep Advice"
                    AdviceCategory.RISK -> "Risk Reminder"
                },
                category = item.category,
                content = item.suggestion,
                explanation = item.explanation,
                priority = 3 - index,
                sourceAgents = listOf(item.agentName)
            )
        }
    }

    private fun resolveConflicts(items: List<AgentResult>): List<AgentResult> {
        val sleepHighRisk = items.any {
            it.category == AdviceCategory.SLEEP && it.riskLevel >= 4
        }

        return if (sleepHighRisk) {
            items.filterNot {
                it.category == AdviceCategory.EXERCISE && it.riskLevel <= 2
            }
        } else {
            items
        }
    }
}

```

## android-app/app/src/main/java/com/example/healthplatform/domain/model/AdviceCategory.kt

```kotlin
package com.example.healthplatform.domain.model

enum class AdviceCategory {
    EXERCISE,
    DIET,
    SLEEP,
    RISK
}

```

## android-app/app/src/main/java/com/example/healthplatform/domain/model/AgentResult.kt

```kotlin
package com.example.healthplatform.domain.model

data class AgentResult(
    val agentName: String,
    val category: AdviceCategory,
    val riskLevel: Int,
    val confidence: Double,
    val suggestion: String,
    val explanation: String,
    val createdAt: Long = System.currentTimeMillis()
)

```

## android-app/app/src/main/java/com/example/healthplatform/domain/model/FinalSuggestion.kt

```kotlin
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

```

## android-app/app/src/main/java/com/example/healthplatform/domain/model/HealthContext.kt

```kotlin
package com.example.healthplatform.domain.model

data class HealthContext(
    val userId: String,
    val startTime: Long,
    val endTime: Long,
    val summary: HealthSummary
)

```

## android-app/app/src/main/java/com/example/healthplatform/domain/model/HealthMetric.kt

```kotlin
package com.example.healthplatform.domain.model

data class HealthMetric(
    val id: Long = 0L,
    val userId: String,
    val metricType: MetricType,
    val value: Double,
    val unit: String,
    val timestamp: Long,
    val source: String
)

```

## android-app/app/src/main/java/com/example/healthplatform/domain/model/HealthSummary.kt

```kotlin
package com.example.healthplatform.domain.model

data class HealthSummary(
    val avgHeartRate: Double?,
    val restingHeartRate: Double?,
    val todaySteps: Int,
    val yesterdaySteps: Int,
    val sleepDurationHours: Double?,
    val deepSleepHours: Double?,
    val lightSleepHours: Double?,
    val stepTrendRatio: Double?,
    val sleepTrendRatio: Double?
)

```

## android-app/app/src/main/java/com/example/healthplatform/domain/model/MetricType.kt

```kotlin
package com.example.healthplatform.domain.model

enum class MetricType {
    HEART_RATE,
    STEPS,
    SLEEP_DURATION,
    SLEEP_DEEP,
    SLEEP_LIGHT
}

```

## android-app/app/src/main/java/com/example/healthplatform/domain/model/TrendPoint.kt

```kotlin
package com.example.healthplatform.domain.model

data class TrendPoint(
    val dayLabel: String,
    val metricType: MetricType,
    val value: Double
)

```

## android-app/app/src/main/java/com/example/healthplatform/domain/repository/HealthDataRepository.kt

```kotlin
package com.example.healthplatform.domain.repository

import com.example.healthplatform.domain.model.HealthMetric
import com.example.healthplatform.domain.model.HealthSummary
import com.example.healthplatform.domain.model.MetricType
import com.example.healthplatform.domain.model.TrendPoint

interface HealthDataRepository {
    suspend fun replaceMetricsForUser(userId: String, metrics: List<HealthMetric>)
    suspend fun saveMetrics(metrics: List<HealthMetric>)
    suspend fun getMetrics(userId: String, start: Long, end: Long): List<HealthMetric>
    suspend fun buildSummary(userId: String, start: Long, end: Long): HealthSummary
    suspend fun buildTrend(
        userId: String,
        metricType: MetricType,
        days: Int,
        endTime: Long
    ): List<TrendPoint>
}

```

## android-app/app/src/main/java/com/example/healthplatform/domain/repository/SuggestionRepository.kt

```kotlin
package com.example.healthplatform.domain.repository

import com.example.healthplatform.domain.model.FinalSuggestion
import kotlinx.coroutines.flow.Flow

interface SuggestionRepository {
    suspend fun saveSuggestions(items: List<FinalSuggestion>)
    fun observeSuggestions(): Flow<List<FinalSuggestion>>
}

```

## android-app/app/src/main/java/com/example/healthplatform/domain/usecase/GenerateSuggestionsUseCase.kt

```kotlin
package com.example.healthplatform.domain.usecase

import com.example.healthplatform.domain.agent.AgentCoordinator
import com.example.healthplatform.domain.fusion.SuggestionFusionEngine
import com.example.healthplatform.domain.model.HealthContext
import com.example.healthplatform.domain.repository.HealthDataRepository
import com.example.healthplatform.domain.repository.SuggestionRepository

class GenerateSuggestionsUseCase(
    private val healthDataRepository: HealthDataRepository,
    private val suggestionRepository: SuggestionRepository,
    private val coordinator: AgentCoordinator,
    private val fusionEngine: SuggestionFusionEngine
) {
    suspend operator fun invoke(
        userId: String,
        start: Long,
        end: Long
    ) {
        val summary = healthDataRepository.buildSummary(userId, start, end)
        val context = HealthContext(
            userId = userId,
            startTime = start,
            endTime = end,
            summary = summary
        )
        val agentResults = coordinator.execute(context)
        val finalSuggestions = fusionEngine.fuse(agentResults)
        suggestionRepository.saveSuggestions(finalSuggestions)
    }
}

```

## android-app/app/src/main/java/com/example/healthplatform/domain/usecase/GetDashboardSummaryUseCase.kt

```kotlin
package com.example.healthplatform.domain.usecase

import com.example.healthplatform.domain.model.HealthSummary
import com.example.healthplatform.domain.repository.HealthDataRepository

class GetDashboardSummaryUseCase(
    private val repository: HealthDataRepository
) {
    suspend operator fun invoke(
        userId: String,
        start: Long,
        end: Long
    ): HealthSummary {
        return repository.buildSummary(userId, start, end)
    }
}

```

## android-app/app/src/main/java/com/example/healthplatform/domain/usecase/GetTrendDataUseCase.kt

```kotlin
package com.example.healthplatform.domain.usecase

import com.example.healthplatform.domain.model.MetricType
import com.example.healthplatform.domain.model.TrendPoint
import com.example.healthplatform.domain.repository.HealthDataRepository

class GetTrendDataUseCase(
    private val repository: HealthDataRepository
) {
    suspend operator fun invoke(
        userId: String,
        metricType: MetricType,
        days: Int,
        endTime: Long
    ): List<TrendPoint> {
        return repository.buildTrend(
            userId = userId,
            metricType = metricType,
            days = days,
            endTime = endTime
        )
    }
}

```

## android-app/app/src/main/java/com/example/healthplatform/domain/usecase/ImportMockDataUseCase.kt

```kotlin
package com.example.healthplatform.domain.usecase

import com.example.healthplatform.domain.model.HealthMetric
import com.example.healthplatform.domain.model.MetricType
import com.example.healthplatform.domain.repository.HealthDataRepository
import java.util.concurrent.TimeUnit

class ImportMockDataUseCase(
    private val repository: HealthDataRepository
) {
    suspend operator fun invoke(userId: String, now: Long) {
        val mockMetrics = mutableListOf<HealthMetric>()

        val dailySteps = listOf(4200.0, 5100.0, 6300.0, 7100.0, 5600.0, 8300.0, 6800.0)
        val dailyHeartRates = listOf(84.0, 82.0, 80.0, 78.0, 81.0, 77.0, 79.0)
        val dailySleep = listOf(6.1, 6.5, 7.2, 6.8, 5.9, 7.4, 7.0)

        for (i in 0 until 7) {
            val dayOffset = 6 - i
            val dayBase = now - TimeUnit.DAYS.toMillis(dayOffset.toLong())

            mockMetrics += HealthMetric(
                userId = userId,
                metricType = MetricType.STEPS,
                value = dailySteps[i],
                unit = "count",
                timestamp = dayBase - TimeUnit.HOURS.toMillis(2),
                source = "mock"
            )

            mockMetrics += HealthMetric(
                userId = userId,
                metricType = MetricType.HEART_RATE,
                value = dailyHeartRates[i],
                unit = "bpm",
                timestamp = dayBase - TimeUnit.HOURS.toMillis(1),
                source = "mock"
            )

            mockMetrics += HealthMetric(
                userId = userId,
                metricType = MetricType.SLEEP_DURATION,
                value = dailySleep[i],
                unit = "hour",
                timestamp = dayBase - TimeUnit.HOURS.toMillis(8),
                source = "mock"
            )
        }

        repository.replaceMetricsForUser(userId, mockMetrics)
    }
}

```

## android-app/app/src/main/java/com/example/healthplatform/presentation/advice/AdviceScreen.kt

```kotlin
package com.example.healthplatform.presentation.advice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Calendar

@Composable
fun AdviceScreen(
    contentPadding: PaddingValues,
    viewModel: AdviceViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val userId = "demo_user"

    LaunchedEffect(Unit) {
        viewModel.observeSuggestions()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Advice",
            style = MaterialTheme.typography.headlineSmall
        )

        Button(
            onClick = {
                viewModel.importMockData(
                    userId = userId,
                    now = System.currentTimeMillis()
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Import Mock Data")
        }

        Button(
            onClick = {
                val now = System.currentTimeMillis()
                val start = startOfToday()
                viewModel.refresh(
                    userId = userId,
                    start = start,
                    end = now
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Generate Today's Advice")
        }

        if (uiState.loading) {
            CircularProgressIndicator()
        }

        uiState.error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.suggestions) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = item.content,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = "Explanation: ${item.explanation}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = "Source: ${item.sourceAgents.joinToString()}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun startOfToday(): Long {
    return Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

```

## android-app/app/src/main/java/com/example/healthplatform/presentation/advice/AdviceUiState.kt

```kotlin
package com.example.healthplatform.presentation.advice

import com.example.healthplatform.domain.model.FinalSuggestion

data class AdviceUiState(
    val suggestions: List<FinalSuggestion> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

```

## android-app/app/src/main/java/com/example/healthplatform/presentation/advice/AdviceViewModel.kt

```kotlin
package com.example.healthplatform.presentation.advice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val suggestionRepository: SuggestionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdviceUiState())
    val uiState: StateFlow<AdviceUiState> = _uiState.asStateFlow()

    fun observeSuggestions() {
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
            }
        }
    }
}

```

## android-app/app/src/main/java/com/example/healthplatform/presentation/advice/AdviceViewModelFactory.kt

```kotlin
package com.example.healthplatform.presentation.advice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.healthplatform.domain.repository.SuggestionRepository
import com.example.healthplatform.domain.usecase.GenerateSuggestionsUseCase
import com.example.healthplatform.domain.usecase.ImportMockDataUseCase

class AdviceViewModelFactory(
    private val generateSuggestionsUseCase: GenerateSuggestionsUseCase,
    private val importMockDataUseCase: ImportMockDataUseCase,
    private val suggestionRepository: SuggestionRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AdviceViewModel(
            generateSuggestionsUseCase = generateSuggestionsUseCase,
            importMockDataUseCase = importMockDataUseCase,
            suggestionRepository = suggestionRepository
        ) as T
    }
}

```

## android-app/app/src/main/java/com/example/healthplatform/presentation/dashboard/DashboardScreen.kt

```kotlin
package com.example.healthplatform.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Calendar

@Composable
fun DashboardScreen(
    contentPadding: PaddingValues,
    viewModel: DashboardViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val userId = "demo_user"

    LaunchedEffect(Unit) {
        viewModel.load(
            userId = userId,
            start = startOfToday(),
            end = System.currentTimeMillis()
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.headlineSmall
        )

        if (uiState.loading) {
            CircularProgressIndicator()
        }

        uiState.error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }

        DashboardCard(title = "Today's Steps", value = uiState.todaySteps)
        DashboardCard(title = "Average Heart Rate", value = uiState.avgHeartRate)
        DashboardCard(title = "Sleep Duration", value = uiState.sleepDuration)
        DashboardCard(title = "Status Summary", value = uiState.statusSummary)
    }
}

@Composable
private fun DashboardCard(title: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

private fun startOfToday(): Long {
    return Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

```

## android-app/app/src/main/java/com/example/healthplatform/presentation/dashboard/DashboardUiState.kt

```kotlin
package com.example.healthplatform.presentation.dashboard

data class DashboardUiState(
    val avgHeartRate: String = "--",
    val todaySteps: String = "--",
    val sleepDuration: String = "--",
    val statusSummary: String = "Waiting for data import",
    val loading: Boolean = false,
    val error: String? = null
)

```

## android-app/app/src/main/java/com/example/healthplatform/presentation/dashboard/DashboardViewModel.kt

```kotlin
package com.example.healthplatform.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthplatform.domain.usecase.GetDashboardSummaryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val getDashboardSummaryUseCase: GetDashboardSummaryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun load(userId: String, start: Long, end: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)

            runCatching {
                getDashboardSummaryUseCase(userId, start, end)
            }.onSuccess { summary ->
                _uiState.value = DashboardUiState(
                    avgHeartRate = summary.avgHeartRate?.let { "${it.toInt()} bpm" } ?: "--",
                    todaySteps = summary.todaySteps.toString(),
                    sleepDuration = summary.sleepDurationHours?.let { String.format("%.1f h", it) } ?: "--",
                    statusSummary = buildStatusText(summary.todaySteps, summary.sleepDurationHours),
                    loading = false,
                    error = null
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = it.message ?: "Failed to load dashboard"
                )
            }
        }
    }

    private fun buildStatusText(steps: Int, sleepHours: Double?): String {
        return when {
            sleepHours != null && sleepHours < 6.5 -> "Recovery is slightly insufficient. Prioritize sleep."
            steps < 6000 -> "Daily activity is low. Add more walking if possible."
            else -> "Current status is generally stable."
        }
    }
}

```

## android-app/app/src/main/java/com/example/healthplatform/presentation/dashboard/DashboardViewModelFactory.kt

```kotlin
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

```

## android-app/app/src/main/java/com/example/healthplatform/presentation/navigation/Screen.kt

```kotlin
package com.example.healthplatform.presentation.navigation

sealed class Screen(val route: String, val title: String) {
    data object Dashboard : Screen("dashboard", "Home")
    data object Trends : Screen("trends", "Trends")
    data object Advice : Screen("advice", "Advice")
    data object Settings : Screen("settings", "Settings")
}

val bottomNavScreens = listOf(
    Screen.Dashboard,
    Screen.Trends,
    Screen.Advice,
    Screen.Settings
)

```

## android-app/app/src/main/java/com/example/healthplatform/presentation/settings/SettingsScreen.kt

```kotlin
package com.example.healthplatform.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(contentPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Future options: agent toggles, refresh interval, privacy info, data source management.",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

```

## android-app/app/src/main/java/com/example/healthplatform/presentation/trends/TrendsScreen.kt

```kotlin
package com.example.healthplatform.presentation.trends

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.healthplatform.domain.model.TrendPoint

@Composable
fun TrendsScreen(
    contentPadding: PaddingValues,
    viewModel: TrendsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val userId = "demo_user"

    LaunchedEffect(Unit) {
        viewModel.load(
            userId = userId,
            endTime = System.currentTimeMillis()
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Trends",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        item {
            if (uiState.loading) {
                CircularProgressIndicator()
            }
        }

        uiState.error?.let { message ->
            item {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        item {
            TrendSection(title = "Recent 7-Day Steps", points = uiState.stepTrend, unit = "steps")
        }

        item {
            TrendSection(title = "Recent 7-Day Average Heart Rate", points = uiState.heartRateTrend, unit = "bpm")
        }

        item {
            TrendSection(title = "Recent 7-Day Sleep Duration", points = uiState.sleepTrend, unit = "h")
        }
    }
}

@Composable
private fun TrendSection(
    title: String,
    points: List<TrendPoint>,
    unit: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            if (points.isEmpty()) {
                Text(
                    text = "No data available",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                points.forEach { point ->
                    Text(
                        text = "${point.dayLabel} : ${formatTrendValue(point.value, unit)}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

private fun formatTrendValue(value: Double, unit: String): String {
    return when (unit) {
        "steps" -> "${value.toInt()} $unit"
        "bpm" -> "${value.toInt()} $unit"
        else -> String.format("%.1f %s", value, unit)
    }
}

```

## android-app/app/src/main/java/com/example/healthplatform/presentation/trends/TrendsUiState.kt

```kotlin
package com.example.healthplatform.presentation.trends

import com.example.healthplatform.domain.model.TrendPoint

data class TrendsUiState(
    val stepTrend: List<TrendPoint> = emptyList(),
    val heartRateTrend: List<TrendPoint> = emptyList(),
    val sleepTrend: List<TrendPoint> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

```

## android-app/app/src/main/java/com/example/healthplatform/presentation/trends/TrendsViewModel.kt

```kotlin
package com.example.healthplatform.presentation.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthplatform.domain.model.MetricType
import com.example.healthplatform.domain.usecase.GetTrendDataUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TrendsViewModel(
    private val getTrendDataUseCase: GetTrendDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrendsUiState())
    val uiState: StateFlow<TrendsUiState> = _uiState.asStateFlow()

    fun load(userId: String, endTime: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)

            runCatching {
                val stepsDeferred = async {
                    getTrendDataUseCase(userId, MetricType.STEPS, 7, endTime)
                }
                val heartDeferred = async {
                    getTrendDataUseCase(userId, MetricType.HEART_RATE, 7, endTime)
                }
                val sleepDeferred = async {
                    getTrendDataUseCase(userId, MetricType.SLEEP_DURATION, 7, endTime)
                }

                Triple(
                    stepsDeferred.await(),
                    heartDeferred.await(),
                    sleepDeferred.await()
                )
            }.onSuccess { (steps, heart, sleep) ->
                _uiState.value = TrendsUiState(
                    stepTrend = steps,
                    heartRateTrend = heart,
                    sleepTrend = sleep,
                    loading = false,
                    error = null
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = it.message ?: "Failed to load trend data"
                )
            }
        }
    }
}

```

## android-app/app/src/main/java/com/example/healthplatform/presentation/trends/TrendsViewModelFactory.kt

```kotlin
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

```

## android-app/app/src/main/java/com/example/healthplatform/ui/HealthPlatformApp.kt

```kotlin
package com.example.healthplatform.ui

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.healthplatform.common.AppContainer
import com.example.healthplatform.presentation.advice.AdviceScreen
import com.example.healthplatform.presentation.advice.AdviceViewModel
import com.example.healthplatform.presentation.advice.AdviceViewModelFactory
import com.example.healthplatform.presentation.dashboard.DashboardScreen
import com.example.healthplatform.presentation.dashboard.DashboardViewModel
import com.example.healthplatform.presentation.dashboard.DashboardViewModelFactory
import com.example.healthplatform.presentation.navigation.Screen
import com.example.healthplatform.presentation.navigation.bottomNavScreens
import com.example.healthplatform.presentation.settings.SettingsScreen
import com.example.healthplatform.presentation.trends.TrendsScreen
import com.example.healthplatform.presentation.trends.TrendsViewModel
import com.example.healthplatform.presentation.trends.TrendsViewModelFactory

@Composable
fun HealthPlatformApp(appContainer: AppContainer) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavScreens.forEach { screen ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(Screen.Dashboard.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Text(screen.title.take(1)) },
                        label = {
                            Text(
                                text = screen.title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route
        ) {
            composable(Screen.Dashboard.route) {
                val dashboardViewModel: DashboardViewModel = viewModel(
                    factory = DashboardViewModelFactory(
                        getDashboardSummaryUseCase = appContainer.getDashboardSummaryUseCase
                    )
                )
                DashboardScreen(
                    contentPadding = innerPadding,
                    viewModel = dashboardViewModel
                )
            }
            composable(Screen.Trends.route) {
                val trendsViewModel: TrendsViewModel = viewModel(
                    factory = TrendsViewModelFactory(
                        getTrendDataUseCase = appContainer.getTrendDataUseCase
                    )
                )
                TrendsScreen(
                    contentPadding = innerPadding,
                    viewModel = trendsViewModel
                )
            }
            composable(Screen.Advice.route) {
                val adviceViewModel: AdviceViewModel = viewModel(
                    factory = AdviceViewModelFactory(
                        generateSuggestionsUseCase = appContainer.generateSuggestionsUseCase,
                        importMockDataUseCase = appContainer.importMockDataUseCase,
                        suggestionRepository = appContainer.suggestionRepository
                    )
                )
                AdviceScreen(
                    contentPadding = innerPadding,
                    viewModel = adviceViewModel
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(contentPadding = innerPadding)
            }
        }
    }
}

```

## android-app/app/src/main/java/com/example/healthplatform/ui/theme/Theme.kt

```kotlin
package com.example.healthplatform.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme()
private val DarkColors = darkColorScheme()

@Composable
fun HealthPlatformTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}

```

## android-app/app/src/main/java/com/example/healthplatform/ui/theme/Type.kt

```kotlin
package com.example.healthplatform.ui.theme

import androidx.compose.material3.Typography

val Typography = Typography()

```

## android-app/build.gradle.kts

```kotlin
plugins {
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("com.google.devtools.ksp") version "1.9.24-1.0.20" apply false
}

```

## android-app/gradle.properties

```
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true

```

## android-app/settings.gradle.kts

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "HealthPlatform"
include(":app")

```

