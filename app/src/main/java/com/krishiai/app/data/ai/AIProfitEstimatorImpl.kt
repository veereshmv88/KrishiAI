package com.krishiai.app.data.ai

import com.krishiai.app.domain.ai.IAIProfitEstimator
import com.krishiai.app.domain.ai.ProfitEstimationResult
import javax.inject.Inject

class AIProfitEstimatorImpl @Inject constructor() : IAIProfitEstimator {

    override suspend fun estimateProfit(
        expectedRevenue: Double,
        transportationCost: Double,
        labourCost: Double,
        fertilizerCost: Double,
        miscellaneousCost: Double
    ): ProfitEstimationResult = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
        kotlinx.coroutines.delay(800) // Simulate complex calculation delay
        val totalInvestment = transportationCost + labourCost + fertilizerCost + miscellaneousCost
        val expectedProfit = expectedRevenue - totalInvestment
        val profitMarginPercentage = if (expectedRevenue > 0) {
            (expectedProfit / expectedRevenue) * 100.0
        } else {
            0.0
        }

        // Analyze cost distribution to find optimization opportunities
        val transportPct = if (totalInvestment > 0) (transportationCost / totalInvestment) * 100 else 0.0
        val labourPct = if (totalInvestment > 0) (labourCost / totalInvestment) * 100 else 0.0
        val fertilizerPct = if (totalInvestment > 0) (fertilizerCost / totalInvestment) * 100 else 0.0

        val recommendation = buildString {
            appendLine("💡 AI Profitability Analysis:")
            if (profitMarginPercentage > 35.0) {
                appendLine("✅ Excellent Margin: Projected profit is very strong (${"%.1f".format(profitMarginPercentage)}%).")
                appendLine("Suggestion: Consider reinvesting a small portion into soil health for the next cycle.")
            } else if (profitMarginPercentage > 15.0) {
                appendLine("👍 Good Margin: Projected profit is healthy.")
            } else if (profitMarginPercentage > 0.0) {
                appendLine("⚠️ Low Margin: Your margin is tight (${"%.1f".format(profitMarginPercentage)}%).")
            } else {
                appendLine("❌ Projected Loss: You are operating below break-even.")
                appendLine("Suggestion: Wait for better market prices or negotiate directly with bulk buyers.")
            }

            appendLine()
            appendLine("Cost Optimization Insights:")
            if (transportPct > 25.0) {
                appendLine("• Transport costs are unusually high ($transportPct% of total). Try pooling transport with nearby farmers.")
            }
            if (labourPct > 40.0) {
                appendLine("• Labour costs are eating into margins. Consider mechanized harvesting if available in your taluk.")
            }
            if (fertilizerPct > 35.0) {
                appendLine("• Fertilizer spend is high. A soil test might reveal you can use less chemical inputs.")
            }
            
            if (transportPct <= 25.0 && labourPct <= 40.0 && fertilizerPct <= 35.0) {
                appendLine("• Your expense breakdown looks balanced compared to regional averages.")
            }
        }.trim()

        @Suppress("UnusedExpression")
        ProfitEstimationResult(
            totalInvestment = totalInvestment,
            expectedProfit = expectedProfit,
            profitMarginPercentage = profitMarginPercentage,
            recommendation = recommendation
        )
    }
}
