package com.krishiai.app.data.model

enum class ProductStatus {
    AVAILABLE,
    RESERVED,
    SOLD,
    EXPIRED
}

enum class SyncStatus {
    PENDING,
    UPLOADING,
    SYNCED,
    FAILED
}

data class AiPriceExplanation(
    val basePrice: Double = 0.0,
    val weatherAdjustment: Double = 0.0,
    val qualityAdjustment: Double = 0.0,
    val seasonalAdjustment: Double = 0.0,
    val freshnessAdjustment: Double = 0.0,
    val finalPrice: Double = 0.0,
    val confidencePercentage: Int = 90,
    val recommendation: String = ""
)

data class Product(
    val id: String = "",
    val farmerId: String = "",
    val farmerName: String = "",
    val farmerPhone: String = "",
    val cropCategory: String = "", // "Vegetables" or "Fruits"
    val cropName: String = "",
    val quantity: String = "", // e.g., "500 kg", "20 quintals"
    val quality: String = "Good", // "Premium", "Good", "Average"
    val harvestDate: Long = System.currentTimeMillis(),
    val description: String = "",
    val district: String = "",
    val taluk: String = "",
    val imageUrl: String = "",
    val basePrice: Double = 0.0,
    val aiRecommendedPrice: Double = 0.0,
    val finalPrice: Double = 0.0, // Chosen by the farmer, typically close to the AI price
    val status: String = "AVAILABLE", // AVAILABLE, RESERVED, SOLD, EXPIRED
    val createdAt: Long = System.currentTimeMillis(),
    val aiExplanation: AiPriceExplanation = AiPriceExplanation(),
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val uploadProgress: Int = 0,
    val lastSyncTime: Long = 0L,
    val serverTimestamp: Long = 0L,
    val conflictVersion: Int = 1
)
