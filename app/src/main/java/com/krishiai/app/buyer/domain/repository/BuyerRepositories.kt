package com.krishiai.app.buyer.domain.repository

import com.krishiai.app.buyer.data.model.*
import com.krishiai.app.data.model.APMCPrice
import com.krishiai.app.data.model.MarketIntelligence
import kotlinx.coroutines.flow.Flow

interface BuyerProductRepository {
    fun getAllProducts(): Flow<List<BuyerProduct>>
    fun getProductsByCategory(category: String): Flow<List<BuyerProduct>>
    fun getProductsByDistrict(district: String): Flow<List<BuyerProduct>>
    fun searchProducts(query: String, filters: BuyerSearchFilters): Flow<List<BuyerProduct>>
    suspend fun getProductById(productId: String): Result<BuyerProduct>
    fun getProductsByFarmer(farmerId: String): Flow<List<BuyerProduct>>
}

interface BuyerCartRepository {
    fun getCartItems(): Flow<List<CartItem>>
    fun getCartCount(): Flow<Int>
    fun getCartSummary(): Flow<CartSummary>
    suspend fun addToCart(item: CartItem): Result<Unit>
    suspend fun updateCartQuantity(cartItemId: String, quantityKg: Double): Result<Unit>
    suspend fun removeFromCart(cartItemId: String): Result<Unit>
    suspend fun clearCart(): Result<Unit>
    suspend fun isInCart(productId: String): Boolean
}

interface BuyerOrderRepository {
    fun getOrdersByBuyer(buyerId: String): Flow<List<BuyerOrder>>
    fun getOrdersByStatus(buyerId: String, status: OrderStatus): Flow<List<BuyerOrder>>
    suspend fun getOrderById(orderId: String): Result<BuyerOrder>
    suspend fun placeOrder(request: CheckoutRequest): Result<CheckoutResult>
    suspend fun cancelOrder(orderId: String, reason: String): Result<Unit>
    suspend fun rateOrder(orderId: String, rating: Float, review: String): Result<Unit>
}

interface BuyerWishlistRepository {
    fun getWishlistItems(): Flow<List<WishlistItem>>
    fun getWishlistCount(): Flow<Int>
    suspend fun addToWishlist(product: BuyerProduct, targetPrice: Double = 0.0): Result<Unit>
    suspend fun removeFromWishlist(productId: String): Result<Unit>
    suspend fun isInWishlist(productId: String): Boolean
    suspend fun setPriceAlert(productId: String, targetPrice: Double): Result<Unit>
    fun getActivePriceAlerts(): Flow<List<PriceAlert>>
    suspend fun createPriceAlert(cropName: String, targetPrice: Double, district: String): Result<Unit>
    suspend fun deletePriceAlert(alertId: String): Result<Unit>
}

interface BuyerMarketRepository {
    fun getMarketPrices(district: String): Flow<List<APMCPrice>>
    fun getMarketIntelligence(district: String): Flow<List<MarketIntelligence>>
    suspend fun refreshMarketData(district: String): Result<Unit>
    suspend fun getNearbySellersByLocation(
        lat: Double, lon: Double, radiusKm: Double
    ): Result<List<NearbySeller>>
}

// ─────────────────────────────────────────────
// Buyer Notification Repository
// ─────────────────────────────────────────────
interface BuyerNotificationRepository {
    fun getNotifications(): Flow<List<BuyerNotification>>
    fun getUnreadCount(): Flow<Int>
    suspend fun markAsRead(notificationId: String): Result<Unit>
    suspend fun markAllAsRead(): Result<Unit>
    suspend fun deleteOldNotifications(olderThan: Long): Result<Unit>
}

// ─────────────────────────────────────────────
// Buyer Analytics Repository
// ─────────────────────────────────────────────
interface BuyerAnalyticsRepository {
    fun getAnalyticsSummary(buyerId: String): Flow<com.krishiai.app.buyer.data.model.BuyerAnalyticsSummary>
}

interface BuyerSearchRepository {
    fun getSearchHistory(): Flow<List<com.krishiai.app.buyer.data.entity.BuyerSearchHistoryEntity>>
    suspend fun saveSearch(query: String, queryType: String, resultCount: Int)
    suspend fun clearSearchHistory()
}
