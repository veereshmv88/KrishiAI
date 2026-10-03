package com.krishiai.app.data.model

// ---- Market Intelligence ----
data class MarketIntelligence(
    val cropName: String = "",
    val district: String = "",
    val demandLevel: DemandLevel = DemandLevel.MEDIUM,
    val demandScore: Int = 50, // 0-100
    val currentPrice: Double = 0.0,
    val predictedPrice7Days: Double = 0.0,
    val bestSellingDay: String = "Wednesday",
    val bestNearbyMarket: String = "",
    val expectedMovement: PriceMovement = PriceMovement.STABLE,
    val expectedMovementPercent: Double = 0.0,
    val weeklyTrend: List<Double> = emptyList(), // 7 price points
    val insight: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

enum class DemandLevel { LOW, MEDIUM, HIGH, VERY_HIGH }
enum class PriceMovement { UP, DOWN, STABLE }

// ---- Transaction ----
data class Transaction(
    val transactionId: String = "",
    val productId: String = "",
    val farmerId: String = "",
    val buyerId: String = "",
    val cropName: String = "",
    val quantityKg: Double = 0.0,
    val pricePerKg: Double = 0.0,
    val totalAmount: Double = 0.0,
    val status: TransactionStatus = TransactionStatus.PENDING,
    val completedAt: Long = System.currentTimeMillis(),
    val farmerRating: Float = 0f,
    val buyerRating: Float = 0f,
    val review: String = ""
)

enum class TransactionStatus { COMPLETED, PENDING, CANCELLED }

// ---- Notification ----
data class KrishiNotification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: NotificationType = NotificationType.AI_TIP,
    val relatedId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

enum class NotificationType { WEATHER, PRICE_ALERT, BUYER_INTEREST, LISTING_EXPIRY, AI_TIP, SYSTEM }

// ---- Revenue Stats ----
data class RevenueStats(
    val totalRevenue: Double = 0.0,
    val thisMonthRevenue: Double = 0.0,
    val lastMonthRevenue: Double = 0.0,
    val totalDeals: Int = 0,
    val thisMonthDeals: Int = 0,
    val pendingAmount: Double = 0.0,
    val monthlyTrend: List<Double> = emptyList() // 6 months
)

// ---- Negotiation Result ----
data class NegotiationResult(
    val decision: NegotiationDecision,
    val fairPriceMin: Double,
    val fairPriceMax: Double,
    val counterOfferPrice: Double,
    val confidenceScore: Float,
    val reasoning: String,
    val reasoningKannada: String = ""
)

enum class NegotiationDecision { ACCEPT, COUNTER, REJECT }

// ---- Disease Detection ----
data class DiseaseDetectionResult(
    val diseaseName: String = "",
    val cropName: String = "",
    val isHealthy: Boolean = false,
    val confidenceScore: Float = 0f,
    val symptoms: List<String> = emptyList(),
    val suggestedTreatments: List<String> = emptyList(),
    val preventionTips: List<String> = emptyList(),
    val severity: DiseaseSeverity = DiseaseSeverity.NONE
)

enum class DiseaseSeverity { NONE, MILD, MODERATE, SEVERE }

// ---- Profit Estimation ----
data class ProfitEstimation(
    val cropName: String = "",
    val quantityKg: Double = 0.0,
    val sellingPricePerKg: Double = 0.0,
    val totalRevenue: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netProfit: Double = 0.0,
    val profitMarginPercent: Double = 0.0,
    val breakEvenPrice: Double = 0.0,
    val recommendation: String = "",
    val expenses: ExpenseBreakdown = ExpenseBreakdown()
)

data class ExpenseBreakdown(
    val seeds: Double = 0.0,
    val fertilizer: Double = 0.0,
    val pesticide: Double = 0.0,
    val labor: Double = 0.0,
    val irrigation: Double = 0.0,
    val transport: Double = 0.0,
    val other: Double = 0.0
) {
    val total: Double get() = seeds + fertilizer + pesticide + labor + irrigation + transport + other
}

// ---- APMC Price ----
data class APMCPrice(
    val marketName: String = "",
    val district: String = "",
    val cropName: String = "",
    val cropCategory: String = "",
    val minPrice: Double = 0.0,
    val maxPrice: Double = 0.0,
    val modalPrice: Double = 0.0,
    val trend: PriceMovement = PriceMovement.STABLE,
    val trendPercent: Double = 0.0,
    val dateFetched: Long = System.currentTimeMillis()
)
