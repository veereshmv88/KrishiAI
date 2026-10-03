package com.krishiai.app.disease.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "disease_scans")
data class DiseaseScanEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val originalImagePath: String,
    val cropName: String,
    val diseaseName: String,
    val confidence: Float,
    val healthScore: Int,
    val severity: String,
    val timestamp: Long,
    val latitude: Double?,
    val longitude: Double?,
    val deviceModel: String,
    val imageResolution: String,
    val scanDurationMs: Long,
    val aiModelVersion: String,
    val applicationVersion: String
)
