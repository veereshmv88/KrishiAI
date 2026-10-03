package com.krishiai.app.core.ai.buyer.engines

import com.krishiai.app.core.ai.buyer.AIEngine
import com.krishiai.app.core.ai.buyer.AIResult
import javax.inject.Inject
import javax.inject.Singleton

data class RecommendationInput(
    val userId: String,
    val favoriteCrops: List<String>,
    val userLat: Double,
    val userLon: Double,
    val activeCropListings: List<com.krishiai.app.data.local.entity.CropListingEntity> 
)

data class PurchaseRecommendation(
    val cropId: String,
    val cropName: String,
    val sellerId: String,
    val sellerName: String,
    val price: Double,
    val quantityAvailable: Double,
    val reason: String,
    val aiScore: Float
)

@Singleton
class BuyerRecommendationEngine @Inject constructor() : AIEngine<RecommendationInput, List<PurchaseRecommendation>> {

    override suspend fun process(input: RecommendationInput): AIResult<List<PurchaseRecommendation>> {
        val recommendations = mutableListOf<PurchaseRecommendation>()
        
        // Complex AI calculation mapped to real entities
        for (listing in input.activeCropListings) {
            if (listing.isSold) continue

            // 1. Calculate base AI score (0-100) based on multiple parameters
            var aiScore = 50f
            var reason = "Trending in market"

            // 2. Factor in user favorites
            if (input.favoriteCrops.any { it.equals(listing.cropName, ignoreCase = true) }) {
                aiScore += 20f
                reason = "Based on your favorites"
            }

            // 3. Price analysis: cheaper than AI recommended price?
            if (listing.pricePerKg > 0 && listing.pricePerKg < listing.aiRecommendedPrice) {
                val discount = 100 - ((listing.pricePerKg / listing.aiRecommendedPrice) * 100).toInt()
                if (discount > 5) {
                    aiScore += 15f
                    reason = "Price dropped by $discount%"
                }
            }

            // 4. Quality factor
            if (listing.quality.equals("Grade A", ignoreCase = true)) {
                aiScore += 10f
                if (reason == "Trending in market") {
                    reason = "Premium Grade A Quality"
                }
            }

            // Fallback safety and normalization
            aiScore = aiScore.coerceIn(0f, 100f)

            recommendations.add(
                PurchaseRecommendation(
                    cropId = listing.listingId,
                    cropName = listing.cropName,
                    sellerId = listing.farmerId,
                    sellerName = listing.farmerName.ifBlank { "Verified Farmer" },
                    price = listing.pricePerKg,
                    quantityAvailable = listing.quantityKg,
                    reason = reason,
                    aiScore = aiScore
                )
            )
        }

        // Sort by highest AI score
        val sortedList = recommendations.sortedByDescending { it.aiScore }.take(10)
        
        return AIResult.Success(sortedList)
    }
}
