package com.krishiai.app.core.ai.buyer.engines

import com.krishiai.app.core.ai.buyer.AIEngine
import com.krishiai.app.core.ai.buyer.AIResult
import com.krishiai.app.data.local.entity.MarketPriceEntity
import javax.inject.Inject
import javax.inject.Singleton

data class MarketIntelligenceInput(
    val commodity: String,
    val marketPrices: List<MarketPriceEntity>
)

data class MarketIntelligenceOutput(
    val averagePrice: Double,
    val minPrice: Double,
    val maxPrice: Double,
    val trend: String, // "UP", "DOWN", "STABLE"
    val supplyDemandScore: Float, // 0-100
    val analysis: String
)

@Singleton
class MarketIntelligenceEngine @Inject constructor() : AIEngine<MarketIntelligenceInput, MarketIntelligenceOutput> {
    override suspend fun process(input: MarketIntelligenceInput): AIResult<MarketIntelligenceOutput> {
        if (input.marketPrices.isEmpty()) {
            return AIResult.Error("No market data available for ${input.commodity}")
        }

        var totalModal = 0.0
        var absoluteMin = Double.MAX_VALUE
        var absoluteMax = Double.MIN_VALUE

        for (price in input.marketPrices) {
            totalModal += price.modalPrice
            if (price.minPrice < absoluteMin) absoluteMin = price.minPrice
            if (price.maxPrice > absoluteMax) absoluteMax = price.maxPrice
        }

        val averagePrice = totalModal / input.marketPrices.size

        // Simple trend analysis based on absolute min/max vs avg
        val volatility = absoluteMax - absoluteMin
        val trend = if (averagePrice > absoluteMin + (volatility * 0.6)) {
            "UP"
        } else if (averagePrice < absoluteMin + (volatility * 0.4)) {
            "DOWN"
        } else {
            "STABLE"
        }

        val supplyDemandScore = if (trend == "UP") 80f else if (trend == "DOWN") 30f else 50f
        
        val analysis = "Based on ${input.marketPrices.size} markets, ${input.commodity} is currently trending $trend with an average modal price of ₹${"%.2f".format(averagePrice)} per Quintal."

        return AIResult.Success(
            MarketIntelligenceOutput(
                averagePrice = averagePrice,
                minPrice = absoluteMin,
                maxPrice = absoluteMax,
                trend = trend,
                supplyDemandScore = supplyDemandScore,
                analysis = analysis
            )
        )
    }
}
