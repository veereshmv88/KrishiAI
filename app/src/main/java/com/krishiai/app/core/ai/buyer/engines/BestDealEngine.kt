package com.krishiai.app.core.ai.buyer.engines

import com.krishiai.app.core.ai.buyer.AIEngine
import com.krishiai.app.core.ai.buyer.AIResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BestDealEngine @Inject constructor() : AIEngine<RecommendationInput, PurchaseRecommendation?> {
    override suspend fun process(input: RecommendationInput): AIResult<PurchaseRecommendation?> {
        if (input.activeCropListings.isEmpty()) return AIResult.Success(null)

        var bestDeal: PurchaseRecommendation? = null
        var highestScore = 0f

        for (listing in input.activeCropListings) {
            if (listing.isSold) continue

            // 1. Calculate value score (price vs market average)
            var valueScore = 0f
            if (listing.pricePerKg > 0 && listing.aiRecommendedPrice > 0) {
                valueScore = ((listing.aiRecommendedPrice - listing.pricePerKg) / listing.aiRecommendedPrice * 100f).toFloat()
            }

            // 2. Factor in quality and freshness
            var qualityMultiplier = 1.0f
            if (listing.quality.equals("Grade A", ignoreCase = true)) qualityMultiplier = 1.2f
            if (listing.quality.equals("Grade B", ignoreCase = true)) qualityMultiplier = 1.0f
            if (listing.quality.equals("Grade C", ignoreCase = true)) qualityMultiplier = 0.8f

            val finalScore = (valueScore + 50f) * qualityMultiplier

            if (finalScore > highestScore) {
                highestScore = finalScore
                val discountInt = valueScore.toInt().coerceAtLeast(0)
                val reason = if (discountInt > 0) {
                    "$discountInt% below market price, excellent value"
                } else {
                    "Top rated value for ${listing.quality}"
                }

                bestDeal = PurchaseRecommendation(
                    cropId = listing.listingId,
                    cropName = listing.cropName,
                    sellerId = listing.farmerId,
                    sellerName = listing.farmerName.ifBlank { "Premium Seller" },
                    price = listing.pricePerKg,
                    quantityAvailable = listing.quantityKg,
                    reason = reason,
                    aiScore = finalScore.coerceIn(0f, 100f)
                )
            }
        }

        return AIResult.Success(bestDeal)
    }
}
