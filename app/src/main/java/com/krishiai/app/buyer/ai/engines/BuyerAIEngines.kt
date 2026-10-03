package com.krishiai.app.buyer.ai.engines

import com.krishiai.app.buyer.ai.BuyerAIEngine
import com.krishiai.app.buyer.ai.BuyerAIResult
import com.krishiai.app.buyer.ai.AIRecommendation
import com.krishiai.app.buyer.ai.PricePrediction
import com.krishiai.app.buyer.ai.BuyingDecision
import com.krishiai.app.buyer.ai.SellerTrustScore
import com.krishiai.app.buyer.ai.MarketInsight
import com.krishiai.app.buyer.ai.FraudFlag
import com.krishiai.app.buyer.data.model.BuyerProduct
import com.krishiai.app.data.local.entity.APMCPriceEntity
import javax.inject.Inject
import javax.inject.Singleton

// ─────────────────────────────────────────────
// Best Deal Engine
// ─────────────────────────────────────────────
@Singleton
class BestDealEngine @Inject constructor() :
    BuyerAIEngine<List<BuyerProduct>, AIRecommendation?> {

    override val engineName = "BestDealEngine"

    override suspend fun process(input: List<BuyerProduct>): BuyerAIResult<AIRecommendation?> {
        return try {
            val best = input.filter { it.status == "AVAILABLE" }
                .maxByOrNull { product ->
                    val valueScore = if (product.aiRecommendedPrice > 0) {
                        (product.aiRecommendedPrice - product.pricePerKg) / product.aiRecommendedPrice
                    } else 0.0
                    val qualScore = when (product.quality.uppercase()) { "PREMIUM" -> 0.3; "GOOD" -> 0.2; else -> 0.0 }
                    val trustScore = product.farmerTrustScore / 100.0
                    val freshnessScore = if (product.freshnessScore > 0) product.freshnessScore.toDouble() / 100.0 else 0.5
                    valueScore + qualScore + trustScore + freshnessScore
                }

            if (best == null) return BuyerAIResult.Error("No available products")

            val marketPrice = best.aiRecommendedPrice.takeIf { it > 0 } ?: best.pricePerKg * 1.15
            val savings = maxOf(0.0, marketPrice - best.pricePerKg)
            val savingsPct = if (marketPrice > 0) (savings / marketPrice) * 100 else 0.0

            BuyerAIResult.Success(AIRecommendation(
                productId = best.productId,
                cropName = best.cropName,
                farmerName = best.farmerName,
                imageUrl = best.imageUrl,
                pricePerKg = best.pricePerKg,
                marketPrice = marketPrice,
                savings = savings,
                savingsPercent = savingsPct,
                quality = best.quality,
                grade = best.grade,
                district = best.district,
                distanceKm = best.distanceKm,
                aiScore = 95f,
                aiReason = "Best overall value combining price, quality, and trust score",
                tags = listOf("🏆 Best Deal Today", "💰 ${savingsPct.toInt()}% savings", "⭐ ${best.quality}")
            ))
        } catch (e: Exception) {
            BuyerAIResult.Error(e.message ?: "BestDealEngine failed")
        }
    }
}

// ─────────────────────────────────────────────
// Price Prediction Engine
// ─────────────────────────────────────────────
data class PricePredictionInput(val cropName: String, val district: String, val apmcHistory: List<APMCPriceEntity>)

@Singleton
class PricePredictionEngine @Inject constructor() :
    BuyerAIEngine<PricePredictionInput, PricePrediction> {

    override val engineName = "PricePredictionEngine"

    override suspend fun process(input: PricePredictionInput): BuyerAIResult<PricePrediction> {
        return try {
            val history = input.apmcHistory
                .filter { it.cropName.equals(input.cropName, true) }
                .sortedBy { it.dateFetched }

            val currentPrice = history.lastOrNull()?.modalPrice ?: 0.0
            if (currentPrice == 0.0) return BuyerAIResult.Error("No price data for ${input.cropName}")

            // Simple linear trend from last few records
            val prices = history.takeLast(7).map { it.modalPrice }
            val avgTrend = if (prices.size >= 2) {
                (prices.last() - prices.first()) / prices.size
            } else 0.0

            // Factor in seasonal variance (~3% weekly)
            val seasonalFactor = 1.0 + (Math.sin(System.currentTimeMillis() / (7 * 24 * 60 * 60 * 1000.0)) * 0.03)

            val tomorrowPrice = (currentPrice + avgTrend * seasonalFactor).coerceAtLeast(1.0)
            val week7Price = (currentPrice + avgTrend * 7 * seasonalFactor).coerceAtLeast(1.0)
            val month30Price = (currentPrice + avgTrend * 30 * seasonalFactor * 0.8).coerceAtLeast(1.0)

            val trendPct = if (currentPrice > 0) ((tomorrowPrice - currentPrice) / currentPrice) * 100 else 0.0
            val trend = when {
                trendPct > 2 -> "UP"; trendPct < -2 -> "DOWN"; else -> "STABLE"
            }

            val advice = when {
                trend == "DOWN" -> "Wait — Prices Decreasing"
                trend == "UP" && trendPct > 5 -> "Buy Now — Prices Rising Fast!"
                trend == "UP" -> "Buy Soon — Prices Increasing"
                else -> "Good Time to Buy — Stable Prices"
            }

            val predictedPrices = (1..7).map { d -> (currentPrice + avgTrend * d * seasonalFactor).coerceAtLeast(1.0) }
            val weeklyPrices = prices.takeLast(7).let { p ->
                if (p.size < 7) List(7 - p.size) { currentPrice } + p else p
            }

            BuyerAIResult.Success(PricePrediction(
                cropName = input.cropName,
                district = input.district,
                currentPrice = currentPrice,
                tomorrowPrice = tomorrowPrice,
                week7Price = week7Price,
                month30Price = month30Price,
                trend = trend,
                trendPercent = trendPct,
                advice = advice,
                confidenceScore = 0.78f,
                weeklyPrices = weeklyPrices,
                predictedPrices = predictedPrices
            ))
        } catch (e: Exception) {
            BuyerAIResult.Error("PricePrediction failed: ${e.message}")
        }
    }
}

// ─────────────────────────────────────────────
// Seller Trust Score Engine
// ─────────────────────────────────────────────
data class SellerInput(val farmerId: String, val rating: Float, val totalDeals: Int, val isVerified: Boolean)

@Singleton
class SellerTrustEngine @Inject constructor() :
    BuyerAIEngine<SellerInput, SellerTrustScore> {

    override val engineName = "SellerTrustEngine"

    override suspend fun process(input: SellerInput): BuyerAIResult<SellerTrustScore> {
        val ratingScore = (input.rating / 5f) * 40f
        val dealScore = minOf(30f, input.totalDeals * 0.5f)
        val verifiedBonus = if (input.isVerified) 20f else 0f
        val baseScore = 10f
        val total = (ratingScore + dealScore + verifiedBonus + baseScore).coerceIn(0f, 100f)

        val badge = when {
            total >= 90 -> "TOP_SELLER"
            input.isVerified -> "VERIFIED"
            input.totalDeals > 50 -> "TRUSTED"
            else -> "NEW"
        }

        return BuyerAIResult.Success(SellerTrustScore(
            farmerId = input.farmerId,
            score = total,
            rating = input.rating,
            totalDeals = input.totalDeals,
            deliverySuccessRate = minOf(1f, 0.9f + (input.totalDeals * 0.001f)),
            responseRate = 0.85f,
            complaintCount = maxOf(0, input.totalDeals / 20 - 1),
            isVerified = input.isVerified,
            badge = badge
        ))
    }
}

// ─────────────────────────────────────────────
// Market Intelligence Engine
// ─────────────────────────────────────────────
data class MarketInput(val cropName: String, val district: String, val apmcPrices: List<APMCPriceEntity>)

@Singleton
class MarketIntelligenceEngine @Inject constructor() :
    BuyerAIEngine<MarketInput, MarketInsight> {

    override val engineName = "MarketIntelligenceEngine"

    override suspend fun process(input: MarketInput): BuyerAIResult<MarketInsight> {
        return try {
            val prices = input.apmcPrices.filter { it.cropName.equals(input.cropName, true) }
            if (prices.isEmpty()) return BuyerAIResult.Error("No data for ${input.cropName}")

            val sorted = prices.sortedBy { it.dateFetched }
            val current = sorted.last().modalPrice
            val prev = sorted.dropLast(1).lastOrNull()?.modalPrice ?: current
            val changePct = if (prev > 0) ((current - prev) / prev) * 100 else 0.0

            val trend = sorted.lastOrNull()?.trend ?: "STABLE"
            val supplyLevel = if (current < prev * 0.95) "HIGH" else if (current > prev * 1.05) "LOW" else "MEDIUM"
            val demandLevel = if (trend == "UP") "HIGH" else if (trend == "DOWN") "LOW" else "MEDIUM"

            val insight = when {
                trend == "UP" && changePct > 5 -> "Prices rising sharply. Buy ${input.cropName} now before further increase."
                trend == "DOWN" -> "${input.cropName} prices are falling — wait a day for better rates."
                supplyLevel == "HIGH" -> "High supply available. Good time to buy in bulk."
                else -> "${input.cropName} market is stable. Safe to buy at current rates."
            }

            val buyingWindow = when {
                trend == "DOWN" -> "Tomorrow"
                trend == "UP" && changePct > 5 -> "Now"
                else -> "Now — Stable"
            }

            BuyerAIResult.Success(MarketInsight(
                cropName = input.cropName,
                supplyLevel = supplyLevel,
                demandLevel = demandLevel,
                priceMovement = trend,
                priceMovementPercent = changePct,
                bestBuyingWindow = buyingWindow,
                insight = insight,
                weeklyTrend = sorted.takeLast(7).map { it.modalPrice }
            ))
        } catch (e: Exception) {
            BuyerAIResult.Error(e.message ?: "MarketIntelligenceEngine error")
        }
    }
}

// ─────────────────────────────────────────────
// Buying Decision Engine
// ─────────────────────────────────────────────
data class BuyingDecisionInput(
    val cropName: String,
    val currentPrice: Double,
    val pricePrediction: PricePrediction?,
    val nearbyProducts: List<BuyerProduct>
)

@Singleton
class BuyingDecisionEngine @Inject constructor() :
    BuyerAIEngine<BuyingDecisionInput, BuyingDecision> {

    override val engineName = "BuyingDecisionEngine"

    override suspend fun process(input: BuyingDecisionInput): BuyerAIResult<BuyingDecision> {
        val pred = input.pricePrediction
        val betterDeal = input.nearbyProducts
            .filter { it.cropName.equals(input.cropName, true) && it.status == "AVAILABLE" }
            .minByOrNull { it.pricePerKg }

        val estimatedSavings = if (betterDeal != null && betterDeal.pricePerKg < input.currentPrice) {
            input.currentPrice - betterDeal.pricePerKg
        } else 0.0

        val decision = when {
            betterDeal != null && estimatedSavings > 2.0 -> "BETTER_SELLER_NEARBY"
            pred?.trend == "DOWN" -> "WAIT"
            pred?.trend == "UP" && (pred.trendPercent) > 5 -> "BUY_NOW"
            else -> "BUY_NOW"
        }

        val reason = when (decision) {
            "BETTER_SELLER_NEARBY" -> "Better price available nearby — save ₹${String.format("%.2f", estimatedSavings)}/kg"
            "WAIT" -> "Prices expected to drop ${pred?.trendPercent?.let { "%.1f".format(it) } ?: ""}% — wait 1-2 days"
            "BUY_NOW" -> if (pred?.trend == "UP") "Prices rising — buy now to lock in current rate" else "Good time to buy at stable price"
            else -> "Proceed with purchase"
        }

        return BuyerAIResult.Success(BuyingDecision(
            recommendation = decision,
            reason = reason,
            estimatedSavings = estimatedSavings,
            bestPrice = betterDeal?.pricePerKg ?: input.currentPrice,
            bestProductId = betterDeal?.productId ?: "",
            urgencyLevel = if (pred?.trend == "UP" && (pred.trendPercent) > 5) "HIGH" else "NORMAL"
        ))
    }
}

// ─────────────────────────────────────────────
// Fraud Detection Engine
// ─────────────────────────────────────────────
data class FraudInput(val product: BuyerProduct, val allProducts: List<BuyerProduct>)

@Singleton
class FraudDetectionEngine @Inject constructor() :
    BuyerAIEngine<FraudInput, FraudFlag> {

    override val engineName = "FraudDetectionEngine"

    override suspend fun process(input: FraudInput): BuyerAIResult<FraudFlag> {
        val flags = mutableListOf<String>()
        val p = input.product

        // Price anomaly: more than 60% below average market for same crop
        val sameCropPrices = input.allProducts
            .filter { it.cropName.equals(p.cropName, true) && it.productId != p.productId }
            .map { it.pricePerKg }
        if (sameCropPrices.isNotEmpty()) {
            val avgPrice = sameCropPrices.average()
            if (p.pricePerKg < avgPrice * 0.4) flags.add("PRICE_ANOMALY")
        }

        // Duplicate listing detection
        val duplicates = input.allProducts.count {
            it.farmerId == p.farmerId &&
            it.cropName.equals(p.cropName, true) &&
            it.productId != p.productId &&
            Math.abs(it.pricePerKg - p.pricePerKg) < 0.5
        }
        if (duplicates > 0) flags.add("DUPLICATE_LISTING")

        // New seller with suspiciously large quantity
        if (!p.isVerifiedFarmer && p.quantityKg > 5000) flags.add("UNVERIFIED_LARGE_QUANTITY")

        val riskLevel = when (flags.size) { 0 -> "NONE"; 1 -> "LOW"; 2 -> "MEDIUM"; else -> "HIGH" }

        return BuyerAIResult.Success(FraudFlag(
            productId = p.productId,
            isSuspicious = flags.isNotEmpty(),
            flags = flags,
            riskLevel = riskLevel
        ))
    }
}

// ─────────────────────────────────────────────
// Seasonal Advisor Engine
// ─────────────────────────────────────────────
data class SeasonalInput(val cropName: String)
data class SeasonalAdvice(val cropName: String, val currentSeason: String, val isBestSeason: Boolean, val advice: String, val nextBestMonth: String)

@Singleton
class SeasonalAdvisorEngine @Inject constructor() :
    BuyerAIEngine<SeasonalInput, SeasonalAdvice> {

    override val engineName = "SeasonalAdvisorEngine"

    private val cropSeasons = mapOf(
        "tomato" to listOf(1, 2, 11, 12),
        "onion" to listOf(3, 4, 5),
        "potato" to listOf(11, 12, 1, 2),
        "mango" to listOf(4, 5, 6),
        "banana" to listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12),
        "rice" to listOf(9, 10, 11),
        "wheat" to listOf(3, 4),
        "chilli" to listOf(1, 2, 12)
    )

    override suspend fun process(input: SeasonalInput): BuyerAIResult<SeasonalAdvice> {
        val month = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1
        val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
        val currentSeason = when (month) { in 3..5 -> "Summer"; in 6..9 -> "Monsoon"; in 10..11 -> "Post-Monsoon"; else -> "Winter" }

        val bestMonths = cropSeasons[input.cropName.lowercase()]
        val isBestSeason = bestMonths?.contains(month) ?: false

        val nextBest = if (!isBestSeason && bestMonths != null) {
            val next = bestMonths.firstOrNull { it > month } ?: bestMonths.first()
            months[next - 1]
        } else "Now"

        val advice = if (isBestSeason) {
            "Peak season for ${input.cropName}. Best quality and lowest prices available now."
        } else if (nextBest != "Now") {
            "Off-season for ${input.cropName}. Better prices and quality expected in $nextBest."
        } else {
            "${input.cropName} is available year-round at stable prices."
        }

        return BuyerAIResult.Success(SeasonalAdvice(input.cropName, currentSeason, isBestSeason, advice, nextBest))
    }
}
