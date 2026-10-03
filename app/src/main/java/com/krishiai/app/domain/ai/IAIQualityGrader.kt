package com.krishiai.app.domain.ai

import java.io.File

data class QualityGradingResult(
    val grade: String, // Premium, Good, Average, Poor
    val confidenceScore: Float,
    val remarks: List<String>
)

interface IAIQualityGrader {
    suspend fun gradeQuality(imageFile: File, cropName: String): QualityGradingResult
}
