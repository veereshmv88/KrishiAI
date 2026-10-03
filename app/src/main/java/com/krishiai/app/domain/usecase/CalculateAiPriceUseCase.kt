package com.krishiai.app.domain.usecase

import com.krishiai.app.data.model.AiPriceExplanation
import com.krishiai.app.domain.ai.IAIPricePredictor
import javax.inject.Inject

class CalculateAiPriceUseCase @Inject constructor(
    private val aiPricePredictor: IAIPricePredictor
) {

    suspend operator fun invoke(
        cropName: String,
        district: String,
        quality: String,
        quantityKg: Double,
        harvestDate: Long,
        historicalApmcPrice: Double,
        weatherCondition: String
    ): AiPriceExplanation {
        val prediction = aiPricePredictor.predictPrice(
            cropName = cropName,
            district = district,
            quality = quality,
            quantityKg = quantityKg,
            harvestDate = harvestDate,
            historicalApmcPrice = historicalApmcPrice,
            weatherCondition = weatherCondition
        )
        
        return AiPriceExplanation(
            basePrice = historicalApmcPrice,
            finalPrice = prediction.suggestedPricePerKg,
            confidencePercentage = (prediction.confidenceScore * 100).toInt(),
            recommendation = prediction.explanation
        )
    }
}

