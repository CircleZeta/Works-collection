package com.renalytics.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val name: String,
    
    val age: Int,
    
    val gender: String,
    
    val height: Float, // 单位：cm
    
    val weight: Float, // 单位：kg
    
    val medicalHistory: String,
    
    val isDefault: Boolean = false
)
