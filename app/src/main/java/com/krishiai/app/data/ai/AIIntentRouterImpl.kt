package com.krishiai.app.data.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.krishiai.app.BuildConfig
import com.krishiai.app.domain.ai.AIIntentRouter
import com.krishiai.app.domain.ai.IntentCategory
import com.krishiai.app.domain.ai.IntentResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIIntentRouterImpl @Inject constructor() : AIIntentRouter {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        systemInstruction = content { 
            text("You are an intent classifier for an agricultural app. Classify the user's intent into one of these categories: WEATHER, MARKET_PRICE, MY_CROPS, CROP_ADVICE, DISEASE_ADVICE, FERTILIZER_ADVICE, PEST_ADVICE, IRRIGATION, SOWING, HARVEST, GENERAL_AGRICULTURE, SEARCH_CROP, CROP_PRICE, BEST_DEAL, COMPARE_PRICES, NEARBY_SELLER, ORDER_STATUS, CART, RECOMMENDATION, PRICE_PREDICTION, GENERAL_MARKETPLACE, GREETING, HELP, UNKNOWN.\n" +
                 "Return ONLY the category name. If you are unsure, return UNKNOWN.")
        }
    )

    override suspend fun routeIntent(query: String, language: String, userRole: String): IntentResult {
        return withContext(Dispatchers.IO) {
            try {
                if (BuildConfig.GEMINI_API_KEY.isBlank()) {
                    return@withContext IntentResult(IntentCategory.UNKNOWN)
                }
                val response = generativeModel.generateContent(query)
                val responseText = response.text?.trim()?.uppercase() ?: "UNKNOWN"
                
                val category = try {
                    IntentCategory.valueOf(responseText)
                } catch (e: Exception) {
                    IntentCategory.UNKNOWN
                }
                
                IntentResult(category = category)
            } catch (e: Exception) {
                IntentResult(category = IntentCategory.UNKNOWN)
            }
        }
    }
}
