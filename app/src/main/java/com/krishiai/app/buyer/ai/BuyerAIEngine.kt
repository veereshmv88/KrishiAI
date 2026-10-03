package com.krishiai.app.buyer.ai

// ─────────────────────────────────────────────
// Generic AI Engine interface
// ─────────────────────────────────────────────
interface BuyerAIEngine<Input, Output> {
    suspend fun process(input: Input): BuyerAIResult<Output>
    val engineName: String
}

// ─────────────────────────────────────────────
// AI Result wrapper
// ─────────────────────────────────────────────
sealed class BuyerAIResult<out T> {
    data class Success<T>(val data: T, val confidenceScore: Float = 1f, val processingMs: Long = 0L) : BuyerAIResult<T>()
    data class Error(val message: String, val cause: Throwable? = null) : BuyerAIResult<Nothing>()
    object Loading : BuyerAIResult<Nothing>()

    val isSuccess get() = this is Success
    val dataOrNull get() = (this as? Success)?.data
}

// ─────────────────────────────────────────────
// Shared AI output models
// ─────────────────────────────────────────────
data class AIRecommendation(
    val productId: String,
    val cropName: String,
    val farmerName: String,
    val imageUrl: String,
    val pricePerKg: Double,
    val marketPrice: Double,
    val savings: Double,
    val savingsPercent: Double,
    val quality: String,
    val grade: String,
    val district: String,
    val distanceKm: Double,
    val aiScore: Float, // 0-100
    val aiReason: String,
    val explanationStrings: List<String> = emptyList(),
    val confidence: Float = 0.95f,
    val tags: List<String> = emptyList()
)

data class PricePrediction(
    val cropName: String,
    val district: String,
    val currentPrice: Double,
    val tomorrowPrice: Double,
    val week7Price: Double,
    val month30Price: Double,
    val trend: String, // UP, DOWN, STABLE
    val trendPercent: Double,
    val advice: String, // "Buy Today", "Wait 2 Days", "Prices Rising"
    val confidenceScore: Float,
    val weeklyPrices: List<Double> = emptyList(), // last 7 days
    val predictedPrices: List<Double> = emptyList() // next 7 days
)

data class BuyingDecision(
    val recommendation: String, // "BUY_NOW", "WAIT", "BETTER_SELLER_NEARBY"
    val reason: String,
    val estimatedSavings: Double,
    val bestPrice: Double,
    val bestProductId: String = "",
    val urgencyLevel: String = "NORMAL", // LOW, NORMAL, HIGH
    val validForHours: Int = 24
)

data class SellerTrustScore(
    val farmerId: String,
    val score: Float, // 0-100
    val rating: Float,
    val totalDeals: Int,
    val deliverySuccessRate: Float,
    val responseRate: Float,
    val complaintCount: Int,
    val isVerified: Boolean,
    val badge: String // "TOP_SELLER", "VERIFIED", "NEW", "TRUSTED"
)

data class MarketInsight(
    val cropName: String,
    val supplyLevel: String, // HIGH, MEDIUM, LOW
    val demandLevel: String,
    val priceMovement: String, // UP, DOWN, STABLE
    val priceMovementPercent: Double,
    val bestBuyingWindow: String, // "Now", "Tomorrow", "Wait 1 Week"
    val insight: String,
    val weeklyTrend: List<Double> = emptyList()
)

data class FraudFlag(
    val productId: String,
    val isSuspicious: Boolean,
    val flags: List<String> = emptyList(), // "PRICE_ANOMALY", "FAKE_SELLER", "DUPLICATE_LISTING"
    val riskLevel: String = "NONE" // NONE, LOW, MEDIUM, HIGH
)
