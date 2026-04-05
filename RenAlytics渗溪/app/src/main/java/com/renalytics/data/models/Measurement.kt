package com.renalytics.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "measurements")
data class Measurement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val userId: Long,
    
    val timestamp: Long,
    
    val eGFR: Float,
    
    val creatinine: Float,
    
    val bun: Float,
    
    val cystatinC: Float,
    
    val uricAcid: Float,
    
    val proteinuria: Float,
    
    val albuminuria: Float,
    
    val urineVolume: Float,
    
    val ckdStage: Int,
    
    val deviceId: String
)
