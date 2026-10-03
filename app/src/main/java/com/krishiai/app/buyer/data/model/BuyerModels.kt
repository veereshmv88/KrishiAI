package com.krishiai.app.buyer.data.model

import androidx.compose.runtime.Immutable

// ─────────────────────────────────────────────
// Buyer Profile
// ─────────────────────────────────────────────
@Immutable
data class BuyerProfile(
    val userId: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val profilePhotoUrl: String = "",
    val businessName: String = "",
    val gstNumber: String = "",
    val district: String = "",
    val taluk: String = "",
    val state: String = "Karnataka",
    val isVerified: Boolean = false,
    val totalOrders: Int = 0,
    val totalSpent: Double = 0.0,
    val savedAddresses: List<DeliveryAddress> = emptyList()
)

// ─────────────────────────────────────────────
// Cart
// ─────────────────────────────────────────────
data class CartItem(
    val cartItemId: String = "",
    val productId: String = "",
    val farmerId: String = "",
    val farmerName: String = "",
    val cropName: String = "",
    val cropCategory: String = "",
    val quality: String = "Good",
    val grade: String = "A",
    val imageUrl: String = "",
    val pricePerKg: Double = 0.0,
    val quantityKg: Double = 1.0,
    val totalAmount: Double = 0.0,
    val district: String = "",
    val isAvailable: Boolean = true
)

@Immutable
data class CartSummary(
    val items: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0,
    val deliveryCharge: Double = 0.0,
    val gst: Double = 0.0,
    val discount: Double = 0.0,
    val couponDiscount: Double = 0.0,
    val total: Double = 0.0,
    val itemCount: Int = 0
)

// ─────────────────────────────────────────────
// Order
// ─────────────────────────────────────────────
enum class OrderStatus { PROCESSING, ACCEPTED, PACKED, SHIPPED, DELIVERED, CANCELLED }
enum class PaymentMethod { CASH_ON_DELIVERY, UPI, CREDIT_CARD, DEBIT_CARD, NET_BANKING }
enum class PaymentStatus { PENDING, PAID, FAILED, REFUNDED }
enum class SyncState { PENDING, SYNCING, SYNCED, FAILED }

@Immutable
data class BuyerOrder(
    val orderId: String = "",
    val productId: String = "",
    val farmerId: String = "",
    val farmerName: String = "",
    val buyerId: String = "",
    val cropName: String = "",
    val imageUrl: String = "",
    val quantityKg: Double = 0.0,
    val pricePerKg: Double = 0.0,
    val subtotal: Double = 0.0,
    val deliveryCharge: Double = 0.0,
    val gst: Double = 0.0,
    val discount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val status: OrderStatus = OrderStatus.PROCESSING,
    val deliveryAddress: DeliveryAddress = DeliveryAddress(),
    val paymentMethod: PaymentMethod = PaymentMethod.CASH_ON_DELIVERY,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val estimatedDelivery: Long = 0L,
    val trackingEvents: List<TrackingEvent> = emptyList(),
    val farmerRating: Float = 0f,
    val review: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val syncState: SyncState = SyncState.SYNCED
)

data class TrackingEvent(
    val title: String,
    val description: String,
    val timestamp: Long,
    val isCompleted: Boolean = true
)

// ─────────────────────────────────────────────
// Delivery Address
// ─────────────────────────────────────────────
data class DeliveryAddress(
    val addressId: String = "",
    val label: String = "Home",
    val name: String = "",
    val phone: String = "",
    val line1: String = "",
    val line2: String = "",
    val city: String = "",
    val district: String = "",
    val state: String = "Karnataka",
    val pincode: String = "",
    val isDefault: Boolean = false,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

// ─────────────────────────────────────────────
// Wishlist
// ─────────────────────────────────────────────
data class WishlistItem(
    val wishlistId: String = "",
    val productId: String = "",
    val cropName: String = "",
    val farmerId: String = "",
    val farmerName: String = "",
    val imageUrl: String = "",
    val priceAtAdd: Double = 0.0,
    val currentPrice: Double = 0.0,
    val targetPrice: Double = 0.0,
    val quality: String = "Good",
    val district: String = "",
    val hasPriceAlert: Boolean = false,
    val priceDrop: Double = 0.0, // currentPrice - priceAtAdd (negative = dropped)
    val priceDropPercent: Double = 0.0
)

// ─────────────────────────────────────────────
// Search
// ─────────────────────────────────────────────
data class BuyerSearchFilters(
    val category: String = "",
    val district: String = "",
    val taluk: String = "",
    val minPrice: Double = 0.0,
    val maxPrice: Double = Double.MAX_VALUE,
    val minRating: Float = 0f,
    val maxDistanceKm: Double = 100.0,
    val grade: String = "",
    val isOrganic: Boolean = false,
    val minQuantityKg: Double = 0.0,
    val sortBy: SearchSortBy = SearchSortBy.AI_RECOMMENDED
)

enum class SearchSortBy { AI_RECOMMENDED, LOWEST_PRICE, HIGHEST_RATING, NEAREST, FRESHEST }

// ─────────────────────────────────────────────
// Nearby Seller
// ─────────────────────────────────────────────
@Immutable
data class NearbySeller(
    val farmerId: String,
    val farmerName: String,
    val farmName: String = "",
    val profilePhotoUrl: String = "",
    val rating: Float = 0f,
    val totalDeals: Int = 0,
    val trustScore: Float = 0f,
    val distanceKm: Double = 0.0,
    val etaMinutes: Int = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val district: String = "",
    val taluk: String = "",
    val availableCrops: List<String> = emptyList(),
    val lowestPrice: Double = 0.0,
    val isVerified: Boolean = false
)

// ─────────────────────────────────────────────
// Price Alert
// ─────────────────────────────────────────────
data class PriceAlert(
    val alertId: String = "",
    val cropName: String = "",
    val targetPrice: Double = 0.0,
    val currentPrice: Double = 0.0,
    val district: String = "",
    val isActive: Boolean = true,
    val isTriggered: Boolean = false
)

// ─────────────────────────────────────────────
// Checkout
// ─────────────────────────────────────────────
@Immutable
data class CheckoutRequest(
    val buyerId: String,
    val cartItems: List<CartItem>,
    val deliveryAddress: DeliveryAddress,
    val paymentMethod: PaymentMethod,
    val couponCode: String = "",
    val notes: String = ""
)

data class CheckoutResult(
    val orderId: String,
    val totalAmount: Double,
    val estimatedDelivery: Long,
    val invoiceUrl: String = ""
)

// ─────────────────────────────────────────────
// Product (buyer-facing enriched view)
// ─────────────────────────────────────────────
@Immutable
data class BuyerProduct(
    val productId: String = "",
    val farmerId: String = "",
    val farmerName: String = "",
    val farmerPhone: String = "",
    val farmName: String = "",
    val profilePhotoUrl: String = "",
    val cropName: String = "",
    val cropCategory: String = "",
    val variety: String = "",
    val grade: String = "A",
    val quality: String = "Good",
    val isOrganic: Boolean = false,
    val quantityKg: Double = 0.0,
    val pricePerKg: Double = 0.0,
    val aiRecommendedPrice: Double = 0.0,
    val marketPrice: Double = 0.0, // APMC modal price
    val discount: Double = 0.0, // percent below market
    val savings: Double = 0.0, // absolute savings vs market
    val imageUrl: String = "",
    val imageUrls: List<String> = emptyList(),
    val harvestDate: Long = 0L,
    val freshnessScore: Float = 0f,
    val shelfLifeDays: Int = 0,
    val description: String = "",
    val district: String = "",
    val taluk: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val distanceKm: Double = 0.0,
    val etaMinutes: Int = 0,
    val farmerRating: Float = 0f,
    val farmerTrustScore: Float = 0f,
    val totalFarmerDeals: Int = 0,
    val isVerifiedFarmer: Boolean = false,
    val aiScore: Float = 0f, // overall AI recommendation score
    val aiScoreReason: String = "",
    val status: String = "AVAILABLE",
    val createdAt: Long = 0L
)

// ---------------------------------------------
// Notification Models
// ---------------------------------------------
enum class NotificationCategory {
    MARKETPLACE,
    ORDERS,
    PRICE_ALERTS,
    AI_RECOMMENDATIONS,
    NEARBY_SELLERS,
    WEATHER_ALERTS,
    WISHLIST,
    SYSTEM
}

data class BuyerNotification(
    val id: String,
    val title: String,
    val message: String,
    val category: NotificationCategory,
    val timestamp: Long,
    val isRead: Boolean,
    val actionPayload: String? = null,
    val deepLink: String? = null
)

// ---------------------------------------------
// Analytics Models
// ---------------------------------------------
@Immutable
data class BuyerAnalyticsSummary(
    val totalOrders: Int = 0,
    val totalSpending: Double = 0.0,
    val totalSavings: Double = 0.0,
    val favoriteCategory: String = "",
    val monthlySpendingTrend: List<Double> = emptyList(),
    val spendingByCategory: Map<String, Double> = emptyMap()
)
