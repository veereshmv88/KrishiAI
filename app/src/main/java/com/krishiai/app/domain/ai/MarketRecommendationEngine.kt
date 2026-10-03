package com.krishiai.app.domain.ai

import com.krishiai.app.data.local.entity.MarketPriceEntity
import javax.inject.Inject

data class MarketRecommendation(
    val action: String, // "Sell Today", "Wait 2-3 Days"
    val reasoning: String,
    val bestNearbyMarket: String? = null,
    val estimatedAdditionalProfit: Double? = null,
    val confidence: Int
)

class MarketRecommendationEngine @Inject constructor() {
    
    fun generateRecommendation(currentPrices: List<MarketPriceEntity>, targetCommodity: String): MarketRecommendation {
        if (currentPrices.isEmpty()) {
            return MarketRecommendation(
                action = "No Data",
                reasoning = "Not enough data available to make a recommendation.",
                confidence = 0
            )
        }
        
        // Filter by commodity
        val cropPrices = currentPrices.filter { it.commodity.equals(targetCommodity, ignoreCase = true) }
        
        if (cropPrices.isEmpty()) {
            return MarketRecommendation(
                action = "No Data",
                reasoning = "No market prices found for $targetCommodity recently.",
                confidence = 0
            )
        }

        // Find highest paying market
        val bestMarket = cropPrices.maxByOrNull { it.modalPrice }
        val averagePrice = cropPrices.map { it.modalPrice }.average()
        
        if (bestMarket != null && bestMarket.modalPrice > averagePrice * 1.1) {
            // Price is 10% above average in this market
            return MarketRecommendation(
                action = "High Demand",
                reasoning = "Prices for $targetCommodity are unusually high at ${bestMarket.market}.",
                bestNearbyMarket = bestMarket.market,
                estimatedAdditionalProfit = bestMarket.modalPrice - averagePrice,
                confidence = 85
            )
        }
        
        return MarketRecommendation(
            action = "Stable",
            reasoning = "Prices are stable across markets. Monitor for upcoming fluctuations.",
            confidence = 70
        )
    }
}
