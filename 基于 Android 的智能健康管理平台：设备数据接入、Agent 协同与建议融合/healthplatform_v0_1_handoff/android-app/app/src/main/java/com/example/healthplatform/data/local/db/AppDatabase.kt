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
