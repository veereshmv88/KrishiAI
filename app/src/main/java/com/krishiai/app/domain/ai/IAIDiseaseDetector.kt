package com.krishiai.app.domain.ai

import android.graphics.Bitmap
import java.io.File

interface IAIDiseaseDetector {
    suspend fun detectDisease(imageFile: File): DiseaseDetectionResult
    suspend fun detectDisease(bitmap: Bitmap): DiseaseDetectionResult
}

data class DiseaseDetectionResult(
    val diseaseName: String,
    val confidenceScore: Float, // 0.0 to 1.0
    val isHealthy: Boolean,
    val symptoms: List<String>,
    val preventiveMeasures: List<String>,
    val suggestedTreatments: List<String>,
    val severityLevel: String
)
