package com.krishiai.app.domain.ai

interface IAINegotiationAssistant {
    suspend fun analyzeOffer(
        cropName: String,
        buyerOfferPrice: Double,
        predictedFairPrice: Double,
        currentApmcPrice: Double
    ): NegotiationResult
}

enum class NegotiationAction {
    ACCEPT, REJECT, COUNTER
}

data class NegotiationResult(
    val recommendedAction: NegotiationAction,
    val suggestedCounterOfferPrice: Double?,
    val reasoning: String,
    val suggestedResponseEnglish: String,
    val suggestedResponseKannada: String
)
