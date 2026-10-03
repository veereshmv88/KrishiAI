package com.krishiai.app.core.ai.buyer

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.system.measureTimeMillis

@Singleton
class BuyerAIManager @Inject constructor(
    // AI Engines
    private val buyerRecommendationEngine: com.krishiai.app.core.ai.buyer.engines.BuyerRecommendationEngine,
    private val bestDealEngine: com.krishiai.app.core.ai.buyer.engines.BestDealEngine,
    private val marketIntelligenceEngine: com.krishiai.app.core.ai.buyer.engines.MarketIntelligenceEngine
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val resultCache = mutableMapOf<String, CacheEntry<*>>()
    
    data class CacheEntry<T>(val data: T, val timestamp: Long)

    private val CACHE_EXPIRATION_MS = 5 * 60 * 1000L // 5 minutes

    /**
     * Centralized execution wrapper for all AI Engines.
     * Handles background processing, performance monitoring, logging, and offline fallbacks.
     */
    suspend fun <I, O> executeEngine(
        engineName: String,
        engine: AIEngine<I, O>,
        input: I,
        useCache: Boolean = true
    ): AIResult<O> = withContext(Dispatchers.Default) {
        val cacheKey = "${engineName}_${input.hashCode()}"

        if (useCache) {
            val cached = getCachedResult<O>(cacheKey)
            if (cached != null) {
                logAIEvent("Cache Hit", engineName, 0L)
                return@withContext AIResult.Success(cached)
            }
        }

        var result: AIResult<O>
        val executionTime = measureTimeMillis {
            try {
                result = engine.process(input)
            } catch (e: Exception) {
                result = AIResult.Error("Engine $engineName failed: ${e.message}", e)
            }
        }

        logAIEvent("Inference Completed", engineName, executionTime)

        if (result is AIResult.Success) {
            cacheResult(cacheKey, (result as AIResult.Success).data)
        }

        result
    }

    private fun <T> getCachedResult(key: String): T? {
        val entry = resultCache[key] ?: return null
        if (System.currentTimeMillis() - entry.timestamp > CACHE_EXPIRATION_MS) {
            resultCache.remove(key)
            return null
        }
        @Suppress("UNCHECKED_CAST")
        return entry.data as? T
    }

    private fun <T> cacheResult(key: String, data: T) {
        resultCache[key] = CacheEntry(data, System.currentTimeMillis())
    }

    private fun logAIEvent(event: String, engineName: String, durationMs: Long) {
        if (com.krishiai.app.BuildConfig.DEBUG) {
            Log.d("BuyerAIManager", "[$engineName] $event - ${durationMs}ms")
        }
    }

    // --- Orchestration Methods ---

    suspend fun getPersonalizedRecommendations(input: com.krishiai.app.core.ai.buyer.engines.RecommendationInput): AIResult<List<com.krishiai.app.core.ai.buyer.engines.PurchaseRecommendation>> {
        return executeEngine("BuyerRecommendationEngine", buyerRecommendationEngine, input)
    }

    suspend fun getBestDeal(input: com.krishiai.app.core.ai.buyer.engines.RecommendationInput): AIResult<com.krishiai.app.core.ai.buyer.engines.PurchaseRecommendation?> {
        return executeEngine("BestDealEngine", bestDealEngine, input)
    }

    suspend fun getMarketIntelligence(input: com.krishiai.app.core.ai.buyer.engines.MarketIntelligenceInput): AIResult<com.krishiai.app.core.ai.buyer.engines.MarketIntelligenceOutput> {
        return executeEngine("MarketIntelligenceEngine", marketIntelligenceEngine, input)
    }
}
