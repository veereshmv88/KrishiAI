package com.krishiai.app.buyer.ai

import com.krishiai.app.buyer.ai.engines.*
import com.krishiai.app.buyer.data.model.BuyerProduct
import com.krishiai.app.data.local.entity.APMCPriceEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BuyerAIOrchestrator @Inject constructor(
    private val recommendationEngine: BuyerRecommendationEngine,
    private val bestDealEngine: BestDealEngine,
    private val pricePredictionEngine: PricePredictionEngine,
    private val sellerTrustEngine: SellerTrustEngine,
    private val marketIntelligenceEngine: MarketIntelligenceEngine,
    private val buyingDecisionEngine: BuyingDecisionEngine,
    private val fraudDetectionEngine: FraudDetectionEngine,
    private val seasonalAdvisorEngine: SeasonalAdvisorEngine
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    // ─── Recommendations ──────────────────────
    suspend fun getRecommendations(
        userId: String,
        allProducts: List<BuyerProduct>,
        apmcPrices: List<APMCPriceEntity>,
        favoriteCrops: List<String> = emptyList(),
        recentSearches: List<String> = emptyList(),
        userLat: Double = 0.0,
        userLon: Double = 0.0,
        userDistrict: String = ""
    ): BuyerAIResult<List<AIRecommendation>> = withContext(Dispatchers.Default) {
        return@withContext recommendationEngine.process(RecommendationInput(
            userId = userId,
            favoriteCrops = favoriteCrops,
            recentSearches = recentSearches,
            userLat = userLat,
            userLon = userLon,
            userDistrict = userDistrict,
            allProducts = allProducts,
            apmcPrices = apmcPrices
        ))
    }

    // ─── Best Deal ────────────────────────────
    suspend fun getBestDeal(products: List<BuyerProduct>): BuyerAIResult<AIRecommendation?> = withContext(Dispatchers.Default) {
        return@withContext bestDealEngine.process(products)
    }

    // ─── Price Prediction ─────────────────────
    suspend fun predictPrice(
        cropName: String,
        district: String,
        apmcHistory: List<APMCPriceEntity>
    ): BuyerAIResult<PricePrediction> = withContext(Dispatchers.Default) {
        return@withContext pricePredictionEngine.process(PricePredictionInput(cropName, district, apmcHistory))
    }

    // ─── Seller Trust ─────────────────────────
    suspend fun getSellerTrust(
        farmerId: String,
        rating: Float,
        totalDeals: Int,
        isVerified: Boolean
    ): BuyerAIResult<SellerTrustScore> = withContext(Dispatchers.Default) {
        return@withContext sellerTrustEngine.process(SellerInput(farmerId, rating, totalDeals, isVerified))
    }

    // ─── Market Intelligence ──────────────────
    suspend fun getMarketInsight(
        cropName: String,
        district: String,
        prices: List<APMCPriceEntity>
    ): BuyerAIResult<MarketInsight> = withContext(Dispatchers.Default) {
        return@withContext marketIntelligenceEngine.process(MarketInput(cropName, district, prices))
    }

    // ─── Buying Decision ──────────────────────
    suspend fun getBuyingDecision(
        cropName: String,
        currentPrice: Double,
        pricePrediction: PricePrediction?,
        nearbyProducts: List<BuyerProduct>
    ): BuyerAIResult<BuyingDecision> = withContext(Dispatchers.Default) {
        return@withContext buyingDecisionEngine.process(BuyingDecisionInput(
            cropName, currentPrice, pricePrediction, nearbyProducts
        ))
    }

    // ─── Fraud Detection ──────────────────────
    suspend fun checkFraud(product: BuyerProduct, allProducts: List<BuyerProduct>): BuyerAIResult<FraudFlag> = withContext(Dispatchers.Default) {
        return@withContext fraudDetectionEngine.process(FraudInput(product, allProducts))
    }

    // ─── Seasonal Advice ──────────────────────
    suspend fun getSeasonalAdvice(cropName: String): BuyerAIResult<SeasonalAdvice> = withContext(Dispatchers.Default) {
        return@withContext seasonalAdvisorEngine.process(SeasonalInput(cropName))
    }

    // ─── Log (debug only) ─────────────────────
    private fun log(tag: String, message: String) {
        if (android.util.Log.isLoggable("BuyerAI", android.util.Log.DEBUG)) {
            android.util.Log.d("BuyerAI/$tag", message)
        }
    }
}
