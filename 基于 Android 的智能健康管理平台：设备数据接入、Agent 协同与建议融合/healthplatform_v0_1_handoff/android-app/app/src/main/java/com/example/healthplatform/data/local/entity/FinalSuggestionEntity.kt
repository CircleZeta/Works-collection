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
