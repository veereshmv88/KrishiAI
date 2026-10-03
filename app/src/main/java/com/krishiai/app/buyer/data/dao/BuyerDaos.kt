package com.krishiai.app.buyer.data.dao

import androidx.room.*
import com.krishiai.app.buyer.data.entity.*
import kotlinx.coroutines.flow.Flow

// ─────────────────────────────────────────────
// Cart DAO
// ─────────────────────────────────────────────
@Dao
interface CartItemDao {
    @Query("SELECT * FROM buyer_cart_items ORDER BY addedAt DESC")
    fun getAllCartItems(): Flow<List<CartItemEntity>>

    @Query("SELECT COUNT(*) FROM buyer_cart_items")
    fun getCartCount(): Flow<Int>

    @Query("SELECT SUM(totalAmount) FROM buyer_cart_items")
    fun getCartTotal(): Flow<Double?>

    @Query("SELECT * FROM buyer_cart_items WHERE cartItemId = :id")
    suspend fun getCartItem(id: String): CartItemEntity?

    @Query("SELECT * FROM buyer_cart_items WHERE syncStatus = 'PENDING'")
    suspend fun getPendingSyncCartItems(): List<CartItemEntity>

    @Query("UPDATE buyer_cart_items SET syncStatus = :syncStatus WHERE cartItemId = :id")
    suspend fun updateSyncStatus(id: String, syncStatus: String)

    @Query("SELECT * FROM buyer_cart_items WHERE productId = :productId LIMIT 1")
    suspend fun getCartItemByProduct(productId: String): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity)

    @Update
    suspend fun updateCartItem(item: CartItemEntity)

    @Query("DELETE FROM buyer_cart_items WHERE cartItemId = :id")
    suspend fun deleteCartItem(id: String)

    @Query("DELETE FROM buyer_cart_items")
    suspend fun clearCart()

    @Query("UPDATE buyer_cart_items SET quantityKg = :qty, totalAmount = :total WHERE cartItemId = :id")
    suspend fun updateQuantity(id: String, qty: Double, total: Double)
}

// ─────────────────────────────────────────────
// Order DAO
// ─────────────────────────────────────────────
@Dao
interface BuyerOrderDao {
    @Query("SELECT * FROM buyer_orders WHERE buyerId = :buyerId ORDER BY createdAt DESC")
    fun getOrdersByBuyer(buyerId: String): Flow<List<BuyerOrderEntity>>

    @Query("SELECT * FROM buyer_orders WHERE buyerId = :buyerId ORDER BY createdAt DESC")
    suspend fun getOrdersByBuyerSync(buyerId: String): List<BuyerOrderEntity>

    @Query("SELECT * FROM buyer_orders WHERE syncStatus = 'PENDING'")
    suspend fun getPendingSyncOrders(): List<BuyerOrderEntity>

    @Query("SELECT * FROM buyer_orders WHERE buyerId = :buyerId AND status = :status ORDER BY createdAt DESC")
    fun getOrdersByStatus(buyerId: String, status: String): Flow<List<BuyerOrderEntity>>

    @Query("SELECT * FROM buyer_orders WHERE orderId = :orderId")
    suspend fun getOrderById(orderId: String): BuyerOrderEntity?



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: BuyerOrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<BuyerOrderEntity>)

    @Query("UPDATE buyer_orders SET status = :status, lastUpdated = :timestamp WHERE orderId = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String, timestamp: Long)

    @Query("UPDATE buyer_orders SET syncStatus = :syncStatus WHERE orderId = :orderId")
    suspend fun updateSyncStatus(orderId: String, syncStatus: String)

    @Query("UPDATE buyer_orders SET trackingEvents = :events, lastUpdated = :timestamp WHERE orderId = :orderId")
    suspend fun updateTrackingEvents(orderId: String, events: String, timestamp: Long)

    @Query("DELETE FROM buyer_orders WHERE orderId = :orderId")
    suspend fun deleteOrder(orderId: String)
}

// ─────────────────────────────────────────────
// Wishlist DAO
// ─────────────────────────────────────────────
@Dao
interface WishlistDao {
    @Query("SELECT * FROM buyer_wishlist ORDER BY addedAt DESC")
    fun getAllWishlistItems(): Flow<List<WishlistItemEntity>>

    @Query("SELECT COUNT(*) FROM buyer_wishlist")
    fun getWishlistCount(): Flow<Int>

    @Query("SELECT * FROM buyer_wishlist WHERE productId = :productId LIMIT 1")
    suspend fun getWishlistItemByProduct(productId: String): WishlistItemEntity?

    @Query("SELECT COUNT(*) FROM buyer_wishlist WHERE productId = :productId")
    suspend fun isInWishlist(productId: String): Int

    @Query("SELECT * FROM buyer_wishlist WHERE syncStatus = 'PENDING'")
    suspend fun getPendingSyncWishlistItems(): List<WishlistItemEntity>

    @Query("UPDATE buyer_wishlist SET syncStatus = :syncStatus WHERE wishlistId = :id")
    suspend fun updateSyncStatus(id: String, syncStatus: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWishlistItem(item: WishlistItemEntity)

    @Query("DELETE FROM buyer_wishlist WHERE wishlistId = :id")
    suspend fun removeWishlistItem(id: String)

    @Query("DELETE FROM buyer_wishlist WHERE productId = :productId")
    suspend fun removeByProductId(productId: String)

    @Query("UPDATE buyer_wishlist SET currentPrice = :price, alertTriggered = :triggered WHERE productId = :productId")
    suspend fun updateCurrentPrice(productId: String, price: Double, triggered: Boolean)

    @Query("SELECT * FROM buyer_wishlist WHERE hasPriceAlert = 1 AND alertTriggered = 0")
    suspend fun getActiveAlerts(): List<WishlistItemEntity>
}

// ─────────────────────────────────────────────
// Search History DAO
// ─────────────────────────────────────────────
@Dao
interface BuyerSearchHistoryDao {
    @Query("SELECT * FROM buyer_search_history ORDER BY searchedAt DESC LIMIT 20")
    fun getRecentSearches(): Flow<List<BuyerSearchHistoryEntity>>

    @Query("SELECT * FROM buyer_search_history WHERE query LIKE '%' || :q || '%' LIMIT 10")
    suspend fun searchHistory(q: String): List<BuyerSearchHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: BuyerSearchHistoryEntity)

    @Query("DELETE FROM buyer_search_history WHERE searchId = :id")
    suspend fun deleteSearch(id: String)

    @Query("DELETE FROM buyer_search_history")
    suspend fun clearHistory()

    @Query("DELETE FROM buyer_search_history WHERE searchedAt < :olderThan")
    suspend fun deleteOldSearches(olderThan: Long)
}

// ─────────────────────────────────────────────
// Price Alert DAO
// ─────────────────────────────────────────────
@Dao
interface PriceAlertDao {
    @Query("SELECT * FROM buyer_price_alerts WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveAlerts(): Flow<List<PriceAlertEntity>>

    @Query("SELECT * FROM buyer_price_alerts ORDER BY createdAt DESC")
    fun getAllAlerts(): Flow<List<PriceAlertEntity>>

    @Query("SELECT * FROM buyer_price_alerts WHERE alertId = :id")
    suspend fun getAlertById(id: String): PriceAlertEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: PriceAlertEntity)

    @Query("UPDATE buyer_price_alerts SET isActive = :active WHERE alertId = :id")
    suspend fun setAlertActive(id: String, active: Boolean)

    @Query("UPDATE buyer_price_alerts SET currentPrice = :price WHERE alertId = :id")
    suspend fun updateCurrentPrice(id: String, price: Double)

    @Query("UPDATE buyer_price_alerts SET isTriggered = 1, triggeredAt = :timestamp WHERE alertId = :id")
    suspend fun triggerAlert(id: String, timestamp: Long)

    @Query("DELETE FROM buyer_price_alerts WHERE alertId = :id")
    suspend fun deleteAlert(id: String)
}

// ─────────────────────────────────────────────
// Compared Products DAO
// ─────────────────────────────────────────────
@Dao
interface ComparedProductDao {
    @Query("SELECT * FROM buyer_compared_products ORDER BY savedAt DESC LIMIT 10")
    fun getSavedComparisons(): Flow<List<ComparedProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveComparison(comparison: ComparedProductEntity)

    @Query("DELETE FROM buyer_compared_products WHERE comparisonId = :id")
    suspend fun deleteComparison(id: String)

    @Query("DELETE FROM buyer_compared_products WHERE savedAt < :olderThan")
    suspend fun deleteOldComparisons(olderThan: Long)
}
