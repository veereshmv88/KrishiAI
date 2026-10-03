package com.krishiai.app.domain.ai

enum class IntentCategory {
    // FARMER
    WEATHER, MARKET_PRICE, MY_CROPS, CROP_ADVICE, DISEASE_ADVICE, FERTILIZER_ADVICE, PEST_ADVICE, IRRIGATION, SOWING, HARVEST, GENERAL_AGRICULTURE,
    
    // BUYER
    SEARCH_CROP, CROP_PRICE, BEST_DEAL, COMPARE_PRICES, NEARBY_SELLER, ORDER_STATUS, CART, RECOMMENDATION, PRICE_PREDICTION, GENERAL_MARKETPLACE,
    
    // GENERAL
    GREETING, HELP, UNKNOWN
}

data class IntentResult(
    val category: IntentCategory,
    val parameters: Map<String, Any> = emptyMap(),
    val requiresClarification: Boolean = false,
    val clarificationQuestion: String? = null
)

interface AIIntentRouter {
    suspend fun routeIntent(query: String, language: String, userRole: String): IntentResult
}
