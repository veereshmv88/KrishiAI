package com.krishiai.app.disease.ai

object HealthScoreCalculator {

    fun calculate(
        severity: String,
        confidence: Float,
        isHealthy: Boolean
    ): Int {
        val baseScore = when {
            isHealthy -> 100
            severity.equals("Mild", ignoreCase = true) -> 75
            severity.equals("Moderate", ignoreCase = true) -> 50
            severity.equals("Severe", ignoreCase = true) -> 25
            else -> 0
        }

        // Adjust slightly based on confidence. 
        // If it's healthy but we are only 60% sure, it might be an 80 instead of 100.
        // If it's severe and we are 99% sure, it stays low.
        val adjustedScore = if (isHealthy) {
            baseScore - ((1f - confidence) * 50).toInt()
        } else {
            baseScore + ((1f - confidence) * 20).toInt()
        }

        return adjustedScore.coerceIn(0, 100)
    }
}
