package com.krishiai.app.buyer.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// ─────────────────────────────────────────────
// Cart Item
// ─────────────────────────────────────────────
@Entity(
    tableName = "buyer_cart_items",
    indices = [androidx.room.Index(value = ["productId", "farmerId"])]
)
data class CartItemEntity(
    @PrimaryKey val cartItemId: String,
    val productId: String,
    val farmerId: String,
    val farmerName: String,
    val cropName: String,
    val cropCategory: String = "",
    val quality: String = "Good",
    val grade: String = "A",
    val imageUrl: String = "",
    val pricePerKg: Double,
    val quantityKg: Double,
    val totalAmount: Double,
    val district: String = "",
    val taluk: String = "",
    val isAvailable: Boolean = true,
    val addedAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "SYNCED"
)

// ─────────────────────────────────────────────
// Buyer Order
// ─────────────────────────────────────────────
@Entity(
    tableName = "buyer_orders",
    indices = [androidx.room.Index(value = ["buyerId", "status"])]
)
data class BuyerOrderEntity(
    @PrimaryKey val orderId: String,
    val productId: String,
    val farmerId: String,
    val farmerName: String,
    val buyerId: String,
    val cropName: String,
    val cropCategory: String = "",
    val quality: String = "Good",
    val grade: String = "A",
    val imageUrl: String = "",
    val quantityKg: Double,
    val pricePerKg: Double,
    val subtotal: Double,
    val deliveryCharge: Double = 0.0,
    val gst: Double = 0.0,
    val discount: Double = 0.0,
    val totalAmount: Double,
    val status: String = "PROCESSING", // PROCESSING, ACCEPTED, PACKED, SHIPPED, DELIVERED, CANCELLED
    val deliveryAddress: String = "",
    val paymentMethod: String = "CASH_ON_DELIVERY",
    val paymentStatus: String = "PENDING",
    val estimatedDelivery: Long = 0L,
    val deliveredAt: Long = 0L,
    val cancellationReason: String = "",
    val farmerRating: Float = 0f,
    val review: String = "",
    val trackingEvents: String = "[]", // JSON array of tracking events
    val couponCode: String = "",
    val couponDiscount: Double = 0.0,
    val invoiceUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING"
)

// ─────────────────────────────────────────────
// Wishlist
// ─────────────────────────────────────────────
@Entity(
    tableName = "buyer_wishlist",
    indices = [androidx.room.Index(value = ["productId"])]
)
data class WishlistItemEntity(
    @PrimaryKey val wishlistId: String,
    val productId: String,
    val cropName: String,
    val cropCategory: String = "",
    val farmerId: String,
    val farmerName: String,
    val imageUrl: String = "",
    val priceAtAdd: Double,
    val currentPrice: Double = 0.0,
    val targetPrice: Double = 0.0, // Price drop alert threshold
    val quality: String = "Good",
    val district: String = "",
    val hasPriceAlert: Boolean = false,
    val alertTriggered: Boolean = false,
    val addedAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "SYNCED"
)

// ─────────────────────────────────────────────
// Search History
// ─────────────────────────────────────────────
@Entity(tableName = "buyer_search_history")
data class BuyerSearchHistoryEntity(
    @PrimaryKey val searchId: String,
    val query: String,
    val queryType: String = "TEXT", // TEXT, VOICE, IMAGE, OCR
    val resultCount: Int = 0,
    val filtersApplied: String = "{}", // JSON
    val searchedAt: Long = System.currentTimeMillis()
)

// ─────────────────────────────────────────────
// Price Alert
// ─────────────────────────────────────────────
@Entity(tableName = "buyer_price_alerts")
data class PriceAlertEntity(
    @PrimaryKey val alertId: String,
    val cropName: String,
    val cropCategory: String = "",
    val targetPrice: Double, // Alert triggers when price drops to/below this
    val currentPrice: Double = 0.0,
    val district: String = "",
    val isActive: Boolean = true,
    val isTriggered: Boolean = false,
    val triggeredAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)

// ─────────────────────────────────────────────
// Compared Products (save comparison sessions)
// ─────────────────────────────────────────────
@Entity(tableName = "buyer_compared_products")
data class ComparedProductEntity(
    @PrimaryKey val comparisonId: String,
    val productIds: String, // Comma-separated product IDs
    val cropName: String,
    val bestProductId: String = "",
    val bestReason: String = "",
    val savedAt: Long = System.currentTimeMillis()
)
