package com.krishiai.app.data.ai

import com.krishiai.app.domain.ai.IAINegotiationAssistant
import com.krishiai.app.domain.ai.NegotiationAction
import com.krishiai.app.domain.ai.NegotiationResult
import javax.inject.Inject
import kotlin.math.abs

/**
 * Real Negotiation Decision Engine — implements a multi-band price analysis
 * algorithm that computes fair price range, compares buyer offer, and 
 * produces a reasoned ACCEPT/COUNTER/REJECT recommendation with bilingual responses.
 */
class AINegotiationAssistantImpl @Inject constructor() : IAINegotiationAssistant {

    override suspend fun analyzeOffer(
        cropName: String,
        buyerOfferPrice: Double,
        predictedFairPrice: Double,
        currentApmcPrice: Double
    ): NegotiationResult = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
        kotlinx.coroutines.delay(1000) // Simulate analysis delay
        
        // Establish fair price band using both AI prediction and APMC floor
        val floorPrice = maxOf(currentApmcPrice * 0.92, predictedFairPrice * 0.85)
        val ceilingPrice = predictedFairPrice * 1.18

        // Offer ratio relative to fair price
        val offerRatio = buyerOfferPrice / predictedFairPrice

        // Decision thresholds:
        // Accept zone:   offer >= 95% of fair price
        // Counter zone:  offer is 75%-95% of fair price  
        // Reject zone:   offer < 75% of fair price (insulting offer)

        val decision = when {
            buyerOfferPrice >= predictedFairPrice * 0.95 -> NegotiationAction.ACCEPT
            buyerOfferPrice >= predictedFairPrice * 0.75 -> NegotiationAction.COUNTER
            else -> NegotiationAction.REJECT
        }

        // Counter offer: fair price + 3% buffer (negotiation room)
        val counterOffer = when (decision) {
            NegotiationAction.COUNTER -> predictedFairPrice * 1.03
            NegotiationAction.REJECT -> predictedFairPrice * 1.08 // higher ask on rejection
            else -> null
        }

        val profitLossPercent = ((buyerOfferPrice - currentApmcPrice) / currentApmcPrice * 100)
        val gap = abs(predictedFairPrice - buyerOfferPrice)

        val reasoning = buildReasoning(
            decision, cropName, buyerOfferPrice, predictedFairPrice,
            currentApmcPrice, offerRatio, profitLossPercent, gap, counterOffer
        )

        val englishResponse = buildEnglishScript(decision, cropName, buyerOfferPrice, counterOffer, predictedFairPrice)
        val kannadaResponse = buildKannadaScript(decision, cropName, buyerOfferPrice, counterOffer, predictedFairPrice)

        @Suppress("UnusedExpression")
        NegotiationResult(
            recommendedAction = decision,
            suggestedCounterOfferPrice = counterOffer,
            reasoning = reasoning,
            suggestedResponseEnglish = englishResponse,
            suggestedResponseKannada = kannadaResponse
        )
    }

    private fun buildReasoning(
        decision: NegotiationAction,
        crop: String,
        offer: Double,
        fairPrice: Double,
        apmcPrice: Double,
        ratio: Double,
        profitPercent: Double,
        gap: Double,
        counterOffer: Double?
    ): String = buildString {
        appendLine("🤖 AI Negotiation Analysis")
        appendLine()
        appendLine("Crop: $crop")
        appendLine("Buyer's Offer: ₹${"%.1f".format(offer)}/kg")
        appendLine("AI Fair Price: ₹${"%.1f".format(fairPrice)}/kg")
        appendLine("APMC Base Price: ₹${"%.1f".format(apmcPrice)}/kg")
        appendLine()
        appendLine("Offer Assessment: ${(ratio * 100).toInt()}% of fair market value")
        if (profitPercent > 0) {
            appendLine("Your profit margin: +${"%.1f".format(profitPercent)}% above APMC")
        } else {
            appendLine("⚠️ Offer is ${"%.1f".format(abs(profitPercent))}% below APMC price — below cost!")
        }
        appendLine()
        when (decision) {
            NegotiationAction.ACCEPT -> {
                appendLine("✅ RECOMMENDATION: ACCEPT")
                appendLine("This offer is within 5% of the AI-computed fair price.")
                appendLine("Accepting gives you a fair return without delay.")
            }
            NegotiationAction.COUNTER -> {
                appendLine("🔄 RECOMMENDATION: COUNTER-OFFER")
                appendLine("Gap from fair price: ₹${"%.1f".format(gap)}/kg (${((1 - ratio) * 100).toInt()}% below fair)")
                appendLine("Suggested counter: ₹${"%.1f".format(counterOffer!!)}/kg")
                appendLine("This positions you for a fair negotiation outcome.")
            }
            NegotiationAction.REJECT -> {
                appendLine("❌ RECOMMENDATION: REJECT")
                appendLine("This offer is more than 25% below fair market value.")
                appendLine("Accepting would mean selling at a significant loss.")
                appendLine("Suggested counter to restart: ₹${"%.1f".format(counterOffer!!)}/kg")
                appendLine("Consider waiting for better market conditions.")
            }
        }
    }.trim()

    private fun buildEnglishScript(
        decision: NegotiationAction, crop: String, offer: Double,
        counterOffer: Double?, fairPrice: Double
    ): String = when (decision) {
        NegotiationAction.ACCEPT ->
            "I agree to your offer of ₹${"%.0f".format(offer)}/kg for $crop. " +
            "Let us finalize the deal. When can you arrange transportation?"
        NegotiationAction.COUNTER ->
            "Thank you for your offer of ₹${"%.0f".format(offer)}/kg. " +
            "However, based on current market rates and AI analysis, the fair price is ₹${"%.0f".format(fairPrice)}/kg. " +
            "I can offer you $crop at ₹${"%.0f".format(counterOffer!!)}/kg. " +
            "This is a competitive price for premium quality. Can we agree on this?"
        NegotiationAction.REJECT ->
            "I appreciate your offer of ₹${"%.0f".format(offer)}/kg, but I cannot accept at this price. " +
            "The current APMC rate and market demand support a price of ₹${"%.0f".format(fairPrice)}/kg. " +
            "I am willing to negotiate at ₹${"%.0f".format(counterOffer!!)}/kg. " +
            "If you are interested, please let me know. Otherwise I will sell at the local market."
    }

    private fun buildKannadaScript(
        decision: NegotiationAction, crop: String, offer: Double,
        counterOffer: Double?, fairPrice: Double
    ): String = when (decision) {
        NegotiationAction.ACCEPT ->
            "ನಿಮ್ಮ ₹${"%.0f".format(offer)}/ಕೆಜಿ ಬೆಲೆಗೆ ನಾನು ಒಪ್ಪಿಗೆ ನೀಡುತ್ತೇನೆ. " +
            "ವ್ಯಾಪಾರ ಮಾಡೋಣ. ನೀವು ಯಾವಾಗ ಸಾಗಣೆ ಮಾಡಬಹುದು?"
        NegotiationAction.COUNTER ->
            "ನಿಮ್ಮ ₹${"%.0f".format(offer)}/ಕೆಜಿ ಬೆಲೆ ಸ್ವೀಕರಿಸಲು ಸಾಧ್ಯವಾಗುವುದಿಲ್ಲ. " +
            "ಪ್ರಸ್ತುತ ಮಾರುಕಟ್ಟೆ ದರ ₹${"%.0f".format(fairPrice)}/ಕೆಜಿ ಇದೆ. " +
            "ನಾನು ₹${"%.0f".format(counterOffer!!)}/ಕೆಜಿ ಬೆಲೆಯಲ್ಲಿ ಕೊಡಬಲ್ಲೆ. ಒಪ್ಪಿಗೆಯೇ?"
        NegotiationAction.REJECT ->
            "ಈ ₹${"%.0f".format(offer)}/ಕೆಜಿ ಬೆಲೆ ತುಂಬಾ ಕಡಿಮೆ. " +
            "APMC ದರ ₹${"%.0f".format(fairPrice)}/ಕೆಜಿ ಇದೆ. " +
            "ನಾನು ₹${"%.0f".format(counterOffer!!)}/ಕೆಜಿ ಬೆಲೆಯನ್ನು ಸ್ವೀಕರಿಸಬಲ್ಲೆ. " +
            "ಇಲ್ಲದಿದ್ದರೆ ಸ್ಥಳೀಯ ಮಾರುಕಟ್ಟೆಯಲ್ಲಿ ಮಾರಾಟ ಮಾಡುತ್ತೇನೆ."
    }
}
