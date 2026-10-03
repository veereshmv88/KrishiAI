package com.krishiai.app.buyer.ai.engines

import com.krishiai.app.buyer.ai.BuyerAIEngine
import com.krishiai.app.buyer.ai.BuyerAIResult
import com.krishiai.app.buyer.ai.AIRecommendation
import com.krishiai.app.buyer.data.model.BuyerProduct
import com.krishiai.app.data.local.entity.APMCPriceEntity
import javax.inject.Inject
import javax.inject.Singleton

data class RecommendationInput(
    val userId: String,
    val favoriteCrops: List<String> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val userLat: Double = 0.0,
    val userLon: Double = 0.0,
    val userDistrict: String = "",
    val allProducts: List<BuyerProduct> = emptyList(),
    val apmcPrices: List<APMCPriceEntity> = emptyList()
)

@Singleton
class BuyerRecommendationEngine @Inject constructor() :
    BuyerAIEngine<RecommendationInput, List<AIRecommendation>> {

    override val engineName = "BuyerRecommendationEngine"

    override suspend fun process(input: RecommendationInput): BuyerAIResult<List<AIRecommendation>> {
        val startTime = System.currentTimeMillis()
        return try {
            val priceMap = input.apmcPrices.associate { it.cropName.lowercase() to it.modalPrice }

            val scored = input.allProducts
                .filter { it.status == "AVAILABLE" && it.quantityKg > 0 }
                .map { product ->
                    val cropKey = product.cropName.lowercase()
                    val marketPrice = priceMap[cropKey] ?: product.pricePerKg
                    val savings = maxOf(0.0, marketPrice - product.pricePerKg)
                    val savingsPct = if (marketPrice > 0) (savings / marketPrice) * 100 else 0.0

                    // 1. Freshness score: 0-20 pts based on harvest date
                    val daysOld = ((System.currentTimeMillis() - product.harvestDate) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
                    val freshnessScore = maxOf(0f, 20f - daysOld * 2f)

                    // 2. Price value: 0-30 pts
                    val priceScore = (savingsPct / 100f * 30f).toFloat().coerceIn(0f, 30f)
                    
                    // 3. Distance: 0-15 pts (closer is better, max points if < 5km, 0 if > 100km)
                    val dist = product.distanceKm
                    val distScore = when {
                        dist <= 5.0 -> 15f
                        dist >= 100.0 -> 0f
                        else -> 15f * (1f - ((dist - 5.0) / 95.0).toFloat())
                    }

                    // 4. Trust: 0-15 pts (rating out of 5 = 10 pts, verified = 3 pts, deals > 10 = 2 pts)
                    val ratingScore = (product.farmerRating / 5f) * 10f
                    val verifiedScore = if (product.isVerifiedFarmer) 3f else 0f
                    val dealsScore = if (product.totalFarmerDeals > 10) 2f else 0f
                    val trustScore = (ratingScore + verifiedScore + dealsScore).coerceIn(0f, 15f)

                    // 5. Preferences & Quality: 0-20 pts
                    val favScore = if (input.favoriteCrops.any { it.equals(product.cropName, true) }) 10f else 0f
                    val qualScore = when (product.quality.uppercase()) {
                        "PREMIUM", "A+" -> 10f; "GOOD", "A" -> 7f; "AVERAGE", "B" -> 4f; else -> 2f
                    }

                    val totalScore = freshnessScore + priceScore + distScore + trustScore + favScore + qualScore

                    val tags = buildList {
                        if (savingsPct > 10) add("💰 ${savingsPct.toInt()}% below market")
                        if (daysOld <= 1) add("🌱 Fresh Today")
                        if (product.quality.uppercase() in listOf("PREMIUM", "A+")) add("⭐ Premium Quality")
                        if (favScore > 0) add("❤️ Your Favorite")
                    }

                    val explanation = buildList {
                        if (savings > 0) add("₹${savings.toInt()} cheaper than market average")
                        add("Seller Trust Score: ${(trustScore / 15f * 100).toInt()}%")
                        if (daysOld == 0) add("Harvested today") else add("Harvested $daysOld days ago")
                        if (dist < 100) add("${dist.toInt()} km from your location")
                    }

                    val confidence = (totalScore / 100f).coerceIn(0.7f, 0.99f)

                    AIRecommendation(
                        productId = product.productId,
                        cropName = product.cropName,
                        farmerName = product.farmerName,
                        imageUrl = product.imageUrl,
                        pricePerKg = product.pricePerKg,
                        marketPrice = marketPrice,
                        savings = savings,
                        savingsPercent = savingsPct,
                        quality = product.quality,
                        grade = product.grade,
                        district = product.district,
                        distanceKm = product.distanceKm,
                        aiScore = totalScore.coerceIn(0f, 100f),
                        aiReason = buildReason(product, savingsPct, daysOld),
                        explanationStrings = explanation,
                        confidence = confidence,
                        tags = tags
                    )
                }
                .sortedByDescending { it.aiScore }
                .take(20)

            BuyerAIResult.Success(scored, processingMs = System.currentTimeMillis() - startTime)
        } catch (e: Exception) {
            BuyerAIResult.Error("Recommendation engine error: ${e.message}", e)
        }
    }

    private fun buildReason(p: BuyerProduct, savingsPct: Double, daysOld: Int): String {
        return when {
            savingsPct > 15 -> "₹${savingsPct.toInt()}% below APMC market price — great savings!"
            daysOld <= 1 -> "Harvested today — freshest available in your area"
            p.quality.uppercase() in listOf("PREMIUM", "A+") -> "Premium grade crop from verified farmer"
            p.distanceKm < 5 -> "Only ${p.distanceKm.toInt()} km away — lowest transport cost"
            else -> "AI-curated recommendation based on your preferences"
        }
    }
}
