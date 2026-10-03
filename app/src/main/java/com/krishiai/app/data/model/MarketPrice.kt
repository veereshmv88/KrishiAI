package com.krishiai.app.data.model

data class MarketPrice(
    val cropName: String = "",
    val basePricePerKg: Double = 0.0,
    val category: String = "", // "Vegetables" or "Fruits"
    val peakSeason: String = "" // "Monsoon", "Winter", "Summer"
)
