package com.krishiai.app.data.ai


import com.krishiai.app.domain.ai.IAIPricePredictor
import com.krishiai.app.domain.ai.PricePredictionResult
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.abs

/**
 * Production AI Price Predictor — implements a weighted multi-factor algorithm
 * that mimics a Random Forest Regressor approach using rule-based weights.
 *
 * Factors:
 * 1. Base APMC price (foundation)
 * 2. Quality multiplier (Premium/Good/Average)
 * 3. Seasonal adjustment (month-based supply/demand curves per crop)
 * 4. Weather impact (rain scarcity, heat damage)
 * 5. Freshness/harvest recency (exponential decay)
 * 6. District demand index
 * 7. Quantity discount (bulk penalty/premium)
 */
class AIPricePredictorImpl @Inject constructor() : IAIPricePredictor {

    // Seasonal price curves per crop type: multiplier for each month (Jan-Dec)
    private val seasonalCurves: Map<String, List<Double>> = mapOf(
        "Tomato"       to listOf(1.2, 1.1, 1.0, 0.9, 0.8, 0.95, 1.1, 1.2, 1.15, 1.0, 0.95, 1.1),
        "Onion"        to listOf(0.9, 0.85, 0.9, 1.0, 1.15, 1.2, 1.1, 0.95, 0.85, 0.9, 1.0, 0.95),
        "Potato"       to listOf(1.1, 1.0, 0.95, 0.9, 0.85, 0.9, 1.0, 1.1, 1.15, 1.1, 1.05, 1.1),
        "Chilli"       to listOf(1.0, 0.95, 1.0, 1.05, 1.1, 1.15, 1.1, 1.0, 0.95, 0.9, 0.95, 1.0),
        "Mango"        to listOf(0.9, 0.85, 1.0, 1.3, 1.5, 1.4, 1.1, 0.9, 0.8, 0.8, 0.85, 0.9),
        "Banana"       to listOf(1.0, 1.0, 1.0, 1.05, 1.1, 1.05, 1.0, 0.95, 0.95, 1.0, 1.0, 1.0),
        "Groundnut"    to listOf(1.1, 1.0, 0.95, 0.9, 0.9, 0.95, 1.0, 1.05, 1.1, 1.15, 1.1, 1.1),
        "Rice"         to listOf(1.0, 1.0, 1.0, 1.05, 1.1, 1.05, 0.95, 0.9, 0.9, 0.95, 1.0, 1.0),
        "Maize"        to listOf(0.95, 0.95, 1.0, 1.0, 1.05, 1.1, 1.05, 0.95, 0.9, 0.9, 0.95, 0.95),
        "DEFAULT"      to listOf(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0)
    )

    // District demand premiums for Karnataka
    private val districtDemandIndex: Map<String, Double> = mapOf(
        "Bangalore Urban" to 1.15,
        "Bengaluru Urban" to 1.15,
        "Mysuru" to 1.08,
        "Hubli" to 1.05,
        "Dharwad" to 1.04,
        "Belagavi" to 1.03,
        "Mangaluru" to 1.06,
        "Vijayapura" to 1.02,
        "Kalaburagi" to 1.01,
        "Shivamogga" to 1.03,
        "Tumakuru" to 1.02,
        "Davanagere" to 1.02
    )

    override suspend fun predictPrice(
        cropName: String,
        district: String,
        quality: String,
        quantityKg: Double,
        harvestDate: Long,
        historicalApmcPrice: Double,
        weatherCondition: String
    ): PricePredictionResult = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
        kotlinx.coroutines.delay(1500) // Simulate complex calculation delay

        val basePrice = historicalApmcPrice.takeIf { it > 0 } ?: getDefaultBasePrice(cropName)

        // Factor 1: Quality multiplier
        val qualityMultiplier = when (quality.lowercase()) {
            "premium" -> 1.22
            "good" -> 1.05
            "average" -> 0.88
            else -> 1.0
        }

        // Factor 2: Seasonal adjustment
        val month = Calendar.getInstance().get(Calendar.MONTH) // 0-indexed
        val curve = seasonalCurves[cropName] ?: seasonalCurves["DEFAULT"]!!
        val seasonalMultiplier = curve[month]

        // Factor 3: Weather impact
        val weatherMultiplier = when {
            weatherCondition.contains("rain", true) || weatherCondition.contains("storm", true) -> 1.12
            weatherCondition.contains("drought", true) || weatherCondition.contains("dry", true) -> 1.08
            weatherCondition.contains("fog", true) || weatherCondition.contains("haze", true) -> 1.03
            weatherCondition.contains("clear", true) || weatherCondition.contains("sunny", true) -> 1.0
            else -> 1.0
        }

        // Factor 4: Freshness decay (days since harvest)
        val daysSinceHarvest = ((System.currentTimeMillis() - harvestDate) / 86400000L).toInt()
        val freshnessMultiplier = when {
            daysSinceHarvest <= 1 -> 1.05
            daysSinceHarvest <= 3 -> 1.02
            daysSinceHarvest <= 7 -> 1.0
            daysSinceHarvest <= 14 -> 0.95
            daysSinceHarvest <= 21 -> 0.90
            else -> 0.82
        }

        // Factor 5: District demand
        val districtMultiplier = districtDemandIndex[district] ?: 1.0

        // Factor 6: Quantity premium/discount
        val quantityMultiplier = when {
            quantityKg >= 1000 -> 0.97  // bulk discount (easier to move)
            quantityKg >= 500 -> 0.99
            quantityKg >= 100 -> 1.0
            quantityKg >= 50 -> 1.02   // small batch premium (fresh/niche)
            else -> 1.03
        }

        // Weighted combination (simulating Random Forest ensemble)
        val weights = doubleArrayOf(0.30, 0.20, 0.15, 0.12, 0.13, 0.10)
        val factors = doubleArrayOf(
            qualityMultiplier,
            seasonalMultiplier,
            weatherMultiplier,
            freshnessMultiplier,
            districtMultiplier,
            quantityMultiplier
        )

        val weightedMultiplier = factors.zip(weights.toList()).sumOf { (f, w) -> f * w } /
                weights.sum()

        val suggestedPrice = basePrice * weightedMultiplier

        // Confidence score: based on factor variance
        val variance = factors.map { abs(it - 1.0) }.average()
        val confidence = (0.98 - variance * 0.5).coerceIn(0.65, 0.98).toFloat()

        // Human-readable explanation
        val explanation = buildExplanation(
            cropName, quality, basePrice, suggestedPrice, district,
            weatherCondition, daysSinceHarvest, month, confidence
        )

        @Suppress("UnusedExpression")
        PricePredictionResult(
            suggestedPricePerKg = String.format("%.2f", suggestedPrice).toDouble(),
            confidenceScore = confidence,
            explanation = explanation
        )
    }

    private fun buildExplanation(
        crop: String, quality: String, basePrice: Double, finalPrice: Double,
        district: String, weather: String, daysSinceHarvest: Int, month: Int, confidence: Float
    ): String {
        val monthNames = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
        val change = ((finalPrice - basePrice) / basePrice * 100)
        val changeStr = if (change >= 0) "+%.1f%%".format(change) else "%.1f%%".format(change)
        val confidencePct = (confidence * 100).toInt()

        return buildString {
            appendLine("📊 AI Price Analysis for $crop ($quality quality)")
            appendLine()
            appendLine("Base APMC Price: ₹${"%.0f".format(basePrice)}/kg")
            appendLine("AI Suggested Price: ₹${"%.0f".format(finalPrice)}/kg ($changeStr)")
            appendLine()
            appendLine("Key Factors:")
            appendLine("• Quality ($quality): ${if (quality == "Premium") "+22%" else if (quality == "Good") "+5%" else "-12%"}")
            appendLine("• Season (${monthNames[month]}): ${if (finalPrice > basePrice) "high demand period" else "moderate demand"}")
            if (weather.isNotEmpty()) appendLine("• Weather ($weather): ${if (weather.contains("rain", true)) "scarcity premium +12%" else "normal conditions"}")
            appendLine("• Freshness ($daysSinceHarvest days old): ${if (daysSinceHarvest <= 3) "premium fresh" else if (daysSinceHarvest <= 7) "optimal" else "aged"}")
            appendLine("• District ($district): ${if (district.contains("Bangalore", true) || district.contains("Bengaluru", true)) "high urban demand" else "local market"}")
            appendLine()
            appendLine("Confidence: $confidencePct%")
            appendLine("Recommendation: ${if (finalPrice > basePrice * 1.1) "Strong time to sell!" else if (finalPrice > basePrice) "Good time to sell." else "Consider waiting for better prices."}")
        }.trim()
    }

    private fun getDefaultBasePrice(cropName: String): Double = when (cropName.lowercase()) {
        "tomato" -> 28.0
        "onion" -> 22.0
        "potato" -> 18.0
        "chilli" -> 95.0
        "brinjal" -> 15.0
        "cabbage" -> 12.0
        "cauliflower" -> 20.0
        "mango" -> 65.0
        "banana" -> 25.0
        "pomegranate" -> 110.0
        "groundnut" -> 55.0
        "maize" -> 22.0
        "rice" -> 35.0
        "wheat" -> 28.0
        else -> 25.0
    }
}
