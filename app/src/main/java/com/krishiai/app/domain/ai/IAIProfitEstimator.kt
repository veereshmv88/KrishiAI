package com.krishiai.app.domain.ai

interface IAIProfitEstimator {
    suspend fun estimateProfit(
        expectedRevenue: Double,
        transportationCost: Double,
        labourCost: Double,
        fertilizerCost: Double,
        miscellaneousCost: Double
    ): ProfitEstimationResult
}

data class ProfitEstimationResult(
    val totalInvestment: Double,
    val expectedProfit: Double,
    val profitMarginPercentage: Double,
    val recommendation: String
)
