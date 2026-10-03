package com.krishiai.app.domain.ai

/**
 * Predicts the fair market price of a crop based on multiple parameters.
 */
interface IAIPricePredictor {
    suspend fun predictPrice(
        cropName: String,
        district: String,
        quality: String,
        quantityKg: Double,
        harvestDate: Long,
        historicalApmcPrice: Double,
        weatherCondition: String
    ): PricePredictionResult
}

data class PricePredictionResult(
    val suggestedPricePerKg: Double,
    val confidenceScore: Float, // 0.0 to 1.0
    val explanation: String
)
