package com.krishiai.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val phoneNumber: String,
    val role: String,
    val name: String,
    val isProfileComplete: Boolean,
    val profilePhotoUrl: String = "",
    val district: String = "",
    val taluk: String = "",
    val farmSizeAcres: Double = 0.0,
    val businessName: String = "",
    val isVerified: Boolean = false,
    val rating: Float = 0f,
    val totalDeals: Int = 0,
    val totalRevenue: Double = 0.0
)

@Entity(tableName = "farmer_profiles")
data class FarmerProfileEntity(
    @PrimaryKey val userId: String,
    val farmSizeAcres: Double,
    val state: String,
    val district: String,
    val taluk: String,
    val rating: Float,
    val totalDeals: Int,
    val totalRevenue: Double = 0.0,
    val isVerified: Boolean = false
)

@Entity(tableName = "buyer_profiles")
data class BuyerProfileEntity(
    @PrimaryKey val userId: String,
    val businessName: String,
    val gstNumber: String?,
    val state: String,
    val district: String,
    val isVerified: Boolean = false
)

@Entity(tableName = "crop_listings")
data class CropListingEntity(
    @PrimaryKey val listingId: String,
    val farmerId: String,
    val cropName: String,
    val cropCategory: String = "",
    val quantityKg: Double,
    val pricePerKg: Double,
    val aiRecommendedPrice: Double = 0.0,
    val quality: String,
    val harvestDate: Long,
    val images: String, // Comma separated URLs
    val isSold: Boolean,
    val district: String = "",
    val taluk: String = "",
    val farmerName: String = "",
    val farmerPhone: String = "",
    val description: String = "",
    val isFeatured: Boolean = false,
    val viewCount: Int = 0,
    val createdAt: Long,
    val syncStatus: String = "SYNCED",
    val uploadProgress: Int = 0,
    val lastSyncTime: Long = 0L,
    val serverTimestamp: Long = 0L,
    val conflictVersion: Int = 1
)

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey val locationKey: String,
    val temperature: Double,
    val feelsLike: Double = 0.0,
    val humidity: Int,
    val windSpeed: Double = 0.0,
    val condition: String,
    val conditionIcon: String = "",
    val rainProbability: Int,
    val uvIndex: Int = 0,
    val updatedAt: Long
)

@Entity(tableName = "apmc_prices")
data class APMCPriceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val marketName: String,
    val district: String = "",
    val cropName: String,
    val cropCategory: String = "",
    val minPrice: Double,
    val maxPrice: Double,
    val modalPrice: Double,
    val trend: String = "STABLE",
    val trendPercent: Double = 0.0,
    val dateFetched: Long
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val type: String, // Kept for backwards compatibility but should map to NotificationCategory
    val category: String = "GENERAL",
    val priority: String = "NORMAL",
    val senderId: String? = null,
    val senderName: String? = null,
    val relatedId: String = "",
    val actionPayload: String? = null,
    val deepLink: String? = null,
    val imageUrl: String? = null,
    val timestamp: Long,
    val isRead: Boolean,
    val isArchived: Boolean = false,
    val isFavorite: Boolean = false,
    val syncStatus: String = "SYNCED"
)

@Entity(tableName = "favourite_crops")
data class FavouriteCropEntity(
    @PrimaryKey val cropName: String,
    val addedAt: Long
)

@Entity(tableName = "favourite_products")
data class FavouriteProductEntity(
    @PrimaryKey val productId: String,
    val addedAt: Long
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val transactionId: String,
    val productId: String,
    val farmerId: String,
    val buyerId: String,
    val cropName: String,
    val quantityKg: Double,
    val pricePerKg: Double,
    val totalAmount: Double,
    val status: String,
    val completedAt: Long,
    val farmerRating: Float = 0f,
    val buyerRating: Float = 0f,
    val review: String = ""
)

@Entity(tableName = "market_intelligence")
data class MarketIntelligenceEntity(
    @PrimaryKey val id: String,
    val cropName: String,
    val district: String,
    val demandLevel: String,
    val demandScore: Int,
    val predictedPrice7Days: Double,
    val bestSellingDay: String,
    val bestNearbyMarket: String,
    val expectedMovement: String,
    val expectedMovementPercent: Double,
    val weeklyTrendJson: String,
    val insight: String,
    val updatedAt: Long
)

@Entity(tableName = "disease_history")
data class DiseaseHistoryEntity(
    @PrimaryKey val id: String,
    val cropName: String,
    val imagePath: String,
    val diseaseName: String,
    val confidenceScore: Float,
    val isHealthy: Boolean,
    val treatment: String, // Kept for backwards compatibility
    val detectedAt: Long,
    val severityLevel: String = "UNKNOWN",
    val healthScore: Int = 0,
    val scientificName: String = "",
    val organicTreatments: String = "",
    val chemicalTreatments: String = "",
    val recommendedFertilizers: String = "",
    val recommendedPesticides: String = "",
    val youtubeRecommendationsJson: String = "",
    val gpsLocation: String = "",
    val aiModelVersion: String = "1.0",
    val appVersion: String = "1.0"
)

@Entity(tableName = "ocr_history")
data class OcrHistoryEntity(
    @PrimaryKey val id: String,
    val imagePath: String,
    val extractedText: String,
    val translatedText: String,
    val timestamp: Long,
    val scanDurationMs: Long,
    val confidence: Float,
    val detectedLanguage: String,
    val documentType: String = "UNKNOWN",
    val category: String = "UNKNOWN",
    val npkRatio: String = ""
)

@Entity(tableName = "location_history")
data class LocationHistoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val parentId: String?,
    val breadcrumb: String,
    val isFavorite: Boolean = false,
    val lastUsedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_history")
data class ChatHistoryEntity(
    @PrimaryKey val messageId: String,
    val role: String, // "user" or "model"
    val content: String,
    val timestamp: Long,
    val language: String = "en",
    val isToolCall: Boolean = false,
    val toolName: String? = null,
    val toolResult: String? = null
)
