package com.krishiai.app.buyer.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.krishiai.app.buyer.data.dao.*
import com.krishiai.app.buyer.data.entity.*
import com.krishiai.app.buyer.data.model.*
import com.krishiai.app.buyer.domain.repository.*
import com.krishiai.app.data.local.dao.APMCPriceDao
import com.krishiai.app.data.local.dao.CropListingDao
import com.krishiai.app.data.local.dao.MarketIntelligenceDao
import com.krishiai.app.data.model.APMCPrice
import com.krishiai.app.data.model.MarketIntelligence
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Constraints
import androidx.work.NetworkType

// ─────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────
private fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2).let { it * it } +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2).let { it * it }
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return R * c
}

private fun com.krishiai.app.data.local.entity.CropListingEntity.toBuyerProduct() = BuyerProduct(
    productId = listingId,
    farmerId = farmerId,
    farmerName = farmerName,
    farmerPhone = farmerPhone,
    cropName = cropName,
    cropCategory = cropCategory,
    quality = quality,
    quantityKg = quantityKg,
    pricePerKg = pricePerKg,
    aiRecommendedPrice = aiRecommendedPrice,
    imageUrl = images.split(",").firstOrNull()?.trim() ?: "",
    imageUrls = images.split(",").map { it.trim() }.filter { it.isNotEmpty() },
    harvestDate = harvestDate,
    description = description,
    district = district,
    taluk = taluk,
    status = if (isSold) "SOLD" else "AVAILABLE",
    createdAt = createdAt
)

private fun com.krishiai.app.data.local.entity.MarketIntelligenceEntity.toModel() = MarketIntelligence(
    cropName = cropName,
    district = district,
    demandLevel = com.krishiai.app.data.model.DemandLevel.valueOf(demandLevel),
    demandScore = demandScore,
    predictedPrice7Days = predictedPrice7Days,
    bestSellingDay = bestSellingDay,
    bestNearbyMarket = bestNearbyMarket,
    expectedMovement = com.krishiai.app.data.model.PriceMovement.valueOf(expectedMovement),
    expectedMovementPercent = expectedMovementPercent,
    insight = insight,
    updatedAt = updatedAt
)

private fun CartItemEntity.toCartItem() = CartItem(
    cartItemId = cartItemId,
    productId = productId,
    farmerId = farmerId,
    farmerName = farmerName,
    cropName = cropName,
    cropCategory = cropCategory,
    quality = quality,
    grade = grade,
    imageUrl = imageUrl,
    pricePerKg = pricePerKg,
    quantityKg = quantityKg,
    totalAmount = totalAmount,
    district = district,
    isAvailable = isAvailable
)

private fun CartItem.toEntity() = CartItemEntity(
    cartItemId = cartItemId.ifEmpty { UUID.randomUUID().toString() },
    productId = productId,
    farmerId = farmerId,
    farmerName = farmerName,
    cropName = cropName,
    cropCategory = cropCategory,
    quality = quality,
    grade = grade,
    imageUrl = imageUrl,
    pricePerKg = pricePerKg,
    quantityKg = quantityKg,
    totalAmount = totalAmount,
    district = district,
    isAvailable = isAvailable
)

private fun BuyerOrderEntity.toBuyerOrder() = BuyerOrder(
    orderId = orderId,
    productId = productId,
    farmerId = farmerId,
    farmerName = farmerName,
    buyerId = buyerId,
    cropName = cropName,
    imageUrl = imageUrl,
    quantityKg = quantityKg,
    pricePerKg = pricePerKg,
    subtotal = subtotal,
    deliveryCharge = deliveryCharge,
    gst = gst,
    discount = discount,
    totalAmount = totalAmount,
    status = try { OrderStatus.valueOf(status) } catch (e: Exception) { OrderStatus.PROCESSING },
    paymentMethod = try { PaymentMethod.valueOf(paymentMethod) } catch (e: Exception) { PaymentMethod.CASH_ON_DELIVERY },
    paymentStatus = try { PaymentStatus.valueOf(paymentStatus) } catch (e: Exception) { PaymentStatus.PENDING },
    estimatedDelivery = estimatedDelivery,
    farmerRating = farmerRating,
    review = review,
    createdAt = createdAt,
    syncState = try { SyncState.valueOf(syncStatus) } catch (e: Exception) { SyncState.PENDING }
)

// ─────────────────────────────────────────────
// BuyerProductRepositoryImpl
// ─────────────────────────────────────────────
@Singleton
class BuyerProductRepositoryImpl @Inject constructor(
    private val cropListingDao: CropListingDao
) : BuyerProductRepository {

    override fun getAllProducts(): Flow<List<BuyerProduct>> {
        return cropListingDao.getAllListings().map { listings ->
            listings.filter { !it.isSold }.map { it.toBuyerProduct() }
        }
    }

    override fun getProductsByCategory(category: String): Flow<List<BuyerProduct>> {
        return cropListingDao.getListingsByCategory(category).map { listings ->
            listings.map { it.toBuyerProduct() }
        }
    }

    override fun getProductsByDistrict(district: String): Flow<List<BuyerProduct>> {
        return cropListingDao.getListingsByDistrict(district).map { listings ->
            listings.map { it.toBuyerProduct() }
        }
    }

    override fun searchProducts(query: String, filters: BuyerSearchFilters): Flow<List<BuyerProduct>> {
        return cropListingDao.searchListings(query).map { listings ->
            listings.filter { entity ->
                !entity.isSold &&
                (filters.category.isEmpty() || entity.cropCategory.equals(filters.category, ignoreCase = true)) &&
                (filters.district.isEmpty() || entity.district.equals(filters.district, ignoreCase = true)) &&
                (entity.pricePerKg >= filters.minPrice) &&
                (entity.pricePerKg <= filters.maxPrice)
            }.map { it.toBuyerProduct() }
        }
    }

    override suspend fun getProductById(productId: String): Result<BuyerProduct> = runCatching {
        val entity = cropListingDao.getListingById(productId) ?: error("Product not found")
        entity.toBuyerProduct()
    }

    override fun getProductsByFarmer(farmerId: String): Flow<List<BuyerProduct>> {
        return cropListingDao.getListingsByFarmer(farmerId).map { it.map { e -> e.toBuyerProduct() } }
    }
}

// ─────────────────────────────────────────────
// BuyerCartRepositoryImpl
// ─────────────────────────────────────────────
@Singleton
class BuyerCartRepositoryImpl @Inject constructor(
    private val cartItemDao: CartItemDao,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : BuyerCartRepository {
    override fun getCartItems() = cartItemDao.getAllCartItems().map { it.map { e -> e.toCartItem() } }
    override fun getCartCount() = cartItemDao.getCartCount()
    override fun getCartSummary(): Flow<CartSummary> {
        return cartItemDao.getAllCartItems().map { entities ->
            val items = entities.map { it.toCartItem() }
            val subtotal = items.sumOf { it.totalAmount }
            val deliveryCharge = if (subtotal > 1000 || subtotal == 0.0) 0.0 else 50.0
            val gst = subtotal * 0.05
            CartSummary(
                items = items,
                subtotal = subtotal,
                deliveryCharge = deliveryCharge,
                gst = gst,
                total = subtotal + deliveryCharge + gst,
                itemCount = items.size
            )
        }
    }
    override suspend fun addToCart(item: CartItem): Result<Unit> = runCatching {
        val existing = cartItemDao.getCartItemByProduct(item.productId)
        if (existing != null) {
            val newQty = existing.quantityKg + item.quantityKg
            cartItemDao.updateQuantity(existing.cartItemId, newQty, newQty * existing.pricePerKg)
            cartItemDao.updateSyncStatus(existing.cartItemId, "PENDING")
        } else {
            cartItemDao.insertCartItem(item.toEntity().copy(syncStatus = "PENDING"))
        }
        triggerSync()
    }
    override suspend fun updateCartQuantity(cartItemId: String, quantityKg: Double): Result<Unit> = runCatching {
        val item = cartItemDao.getCartItem(cartItemId) ?: return@runCatching
        cartItemDao.updateQuantity(cartItemId, quantityKg, quantityKg * item.pricePerKg)
        cartItemDao.updateSyncStatus(cartItemId, "PENDING")
        triggerSync()
    }
    override suspend fun removeFromCart(cartItemId: String): Result<Unit> = runCatching {
        cartItemDao.deleteCartItem(cartItemId)
        FirebaseFirestore.getInstance().collection("buyer_cart").document(cartItemId).delete()
    }
    override suspend fun clearCart(): Result<Unit> = runCatching { cartItemDao.clearCart() }
    override suspend fun isInCart(productId: String) = cartItemDao.getCartItemByProduct(productId) != null

    private fun triggerSync() {
        val req = androidx.work.OneTimeWorkRequestBuilder<com.krishiai.app.services.BuyerSyncWorker>().build()
        androidx.work.WorkManager.getInstance(context).enqueue(req)
    }
}

// ─────────────────────────────────────────────
// BuyerOrderRepositoryImpl
// ─────────────────────────────────────────────
@Singleton
class BuyerOrderRepositoryImpl @Inject constructor(
    private val buyerOrderDao: BuyerOrderDao,
    private val cartItemDao: CartItemDao,
    private val firestore: FirebaseFirestore,
    @ApplicationContext private val context: Context
) : BuyerOrderRepository {

    override fun getOrdersByBuyer(buyerId: String) = buyerOrderDao.getOrdersByBuyer(buyerId).map { it.map { e -> e.toBuyerOrder() } }
    override fun getOrdersByStatus(buyerId: String, status: OrderStatus) = buyerOrderDao.getOrdersByStatus(buyerId, status.name).map { it.map { e -> e.toBuyerOrder() } }
    override suspend fun getOrderById(orderId: String): Result<BuyerOrder> = runCatching {
        buyerOrderDao.getOrderById(orderId)?.toBuyerOrder() ?: error("Not found")
    }

    override suspend fun placeOrder(request: CheckoutRequest): Result<CheckoutResult> = runCatching {
        val orderId = "ORD-${UUID.randomUUID().toString().take(8).uppercase()}"
        val estimatedDelivery = System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000L)
        var total = 0.0
        
        // 1. Transaction to reserve stock on Firestore
        firestore.runTransaction { transaction ->
            for (item in request.cartItems) {
                val prodRef = firestore.collection("products").document(item.productId)
                val snapshot = transaction.get(prodRef)
                
                // Verify available and quantity
                if (snapshot.exists()) {
                    val status = snapshot.getString("status")
                    val availableQty = snapshot.getDouble("quantityKg") ?: 0.0
                    
                    if (status == "SOLD" || availableQty < item.quantityKg) {
                        throw Exception("Item ${item.cropName} does not have enough stock. Available: $availableQty")
                    }
                    
                    // Update seller inventory
                    val newQty = availableQty - item.quantityKg
                    val newStatus = if (newQty <= 0) "SOLD" else "AVAILABLE"
                    
                    transaction.update(prodRef, mapOf(
                        "quantityKg" to newQty,
                        "status" to newStatus
                    ))
                    
                    // Update Analytics (increment sales count)
                    val analyticsRef = firestore.collection("analytics").document(item.farmerId)
                    val analyticsSnap = transaction.get(analyticsRef)
                    if (analyticsSnap.exists()) {
                        val currentSales = analyticsSnap.getLong("totalSales") ?: 0L
                        transaction.update(analyticsRef, "totalSales", currentSales + 1)
                    } else {
                        transaction.set(analyticsRef, mapOf("totalSales" to 1L))
                    }
                } else {
                    throw Exception("Product ${item.cropName} no longer exists.")
                }
                
                // Create Order doc
                val entityId = "$orderId-${item.cartItemId.take(6)}"
                val orderRef = firestore.collection("buyer_orders").document(entityId)
                
                val itemTotal = item.totalAmount * 1.05 + (if (item.totalAmount > 500) 0.0 else 40.0)
                total += itemTotal
                
                transaction.set(orderRef, mapOf(
                    "orderId" to entityId,
                    "productId" to item.productId,
                    "farmerId" to item.farmerId,
                    "buyerId" to request.buyerId,
                    "cropName" to item.cropName,
                    "quantityKg" to item.quantityKg,
                    "totalAmount" to itemTotal,
                    "status" to "PROCESSING",
                    "createdAt" to System.currentTimeMillis(),
                    "paymentMethod" to request.paymentMethod.name
                ))
            }
        }.await()
        
        // 2. Save Locally as SYNCED since we just did a transaction online
        for (item in request.cartItems) {
            val itemTotal = item.totalAmount * 1.05 + (if (item.totalAmount > 500) 0.0 else 40.0)
            val entity = BuyerOrderEntity(
                orderId = "$orderId-${item.cartItemId.take(6)}",
                productId = item.productId,
                farmerId = item.farmerId,
                farmerName = item.farmerName,
                buyerId = request.buyerId,
                cropName = item.cropName,
                imageUrl = item.imageUrl,
                quantityKg = item.quantityKg,
                pricePerKg = item.pricePerKg,
                subtotal = item.totalAmount,
                deliveryCharge = if (item.totalAmount > 500) 0.0 else 40.0,
                gst = item.totalAmount * 0.05,
                totalAmount = itemTotal,
                status = "PROCESSING",
                deliveryAddress = "${request.deliveryAddress.line1}, ${request.deliveryAddress.city}",
                paymentMethod = request.paymentMethod.name,
                estimatedDelivery = estimatedDelivery,
                syncStatus = SyncState.SYNCED.name
            )
            buyerOrderDao.insertOrder(entity)
        }

        cartItemDao.clearCart()

        CheckoutResult(
            orderId = orderId,
            totalAmount = total,
            estimatedDelivery = estimatedDelivery
        )
    }.recoverCatching { e ->
        // If offline or transaction failed due to network, we can fall back to offline PENDING queue via WorkManager
        // For simplicity, we just fail it if the transaction throws stock error, else save as PENDING
        if (e.message?.contains("out of stock") == true) throw e
        
        // Offline Fallback
        val orderId = "ORD-${UUID.randomUUID().toString().take(8).uppercase()}"
        val estimatedDelivery = System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000L)
        var total = 0.0

        for (item in request.cartItems) {
            val itemTotal = item.totalAmount * 1.05 + (if (item.totalAmount > 500) 0.0 else 40.0)
            total += itemTotal
            val entity = BuyerOrderEntity(
                orderId = "$orderId-${item.cartItemId.take(6)}",
                productId = item.productId,
                farmerId = item.farmerId,
                farmerName = item.farmerName,
                buyerId = request.buyerId,
                cropName = item.cropName,
                imageUrl = item.imageUrl,
                quantityKg = item.quantityKg,
                pricePerKg = item.pricePerKg,
                subtotal = item.totalAmount,
                deliveryCharge = if (item.totalAmount > 500) 0.0 else 40.0,
                gst = item.totalAmount * 0.05,
                totalAmount = itemTotal,
                status = "PROCESSING",
                deliveryAddress = "${request.deliveryAddress.line1}, ${request.deliveryAddress.city}",
                paymentMethod = request.paymentMethod.name,
                estimatedDelivery = estimatedDelivery,
                syncStatus = SyncState.PENDING.name
            )
            buyerOrderDao.insertOrder(entity)
        }
        
        cartItemDao.clearCart()
        
        // Enqueue WorkManager
        enqueueOrderSyncWork(context)

        CheckoutResult(orderId, total, estimatedDelivery)
    }

    override suspend fun cancelOrder(orderId: String, reason: String): Result<Unit> = runCatching {
        buyerOrderDao.updateOrderStatus(orderId, "CANCELLED", System.currentTimeMillis())
        firestore.collection("buyer_orders").document(orderId)
            .update("status", "CANCELLED", "cancellationReason", reason).await()
    }
    
    override suspend fun rateOrder(orderId: String, rating: Float, review: String): Result<Unit> = runCatching {
        firestore.collection("buyer_orders").document(orderId)
            .update("farmerRating", rating, "review", review).await()
    }
}

// Helper to trigger WorkManager
private fun enqueueOrderSyncWork(context: Context) {
    val request = OneTimeWorkRequestBuilder<com.krishiai.app.services.BuyerSyncWorker>()
        .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
        .build()
    WorkManager.getInstance(context).enqueue(request)
}

// ─────────────────────────────────────────────
// BuyerWishlistRepositoryImpl
// ─────────────────────────────────────────────
@Singleton
class BuyerWishlistRepositoryImpl @Inject constructor(
    private val wishlistDao: WishlistDao,
    private val priceAlertDao: PriceAlertDao,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context
) : BuyerWishlistRepository {
    override fun getWishlistItems() = wishlistDao.getAllWishlistItems().map { it.map { e ->
        val drop = e.currentPrice - e.priceAtAdd
        WishlistItem(
            wishlistId = e.wishlistId, productId = e.productId, cropName = e.cropName,
            farmerId = e.farmerId, farmerName = e.farmerName, imageUrl = e.imageUrl,
            priceAtAdd = e.priceAtAdd, currentPrice = e.currentPrice.takeIf { it > 0 } ?: e.priceAtAdd,
            targetPrice = e.targetPrice, quality = e.quality, district = e.district,
            hasPriceAlert = e.hasPriceAlert, priceDrop = drop, priceDropPercent = if (e.priceAtAdd > 0) (drop / e.priceAtAdd) * 100.0 else 0.0
        )
    }}
    override fun getWishlistCount() = wishlistDao.getWishlistCount()
    override suspend fun addToWishlist(product: BuyerProduct, targetPrice: Double): Result<Unit> = runCatching {
        wishlistDao.insertWishlistItem(WishlistItemEntity(
            wishlistId = UUID.randomUUID().toString(), productId = product.productId, cropName = product.cropName,
            cropCategory = product.cropCategory, farmerId = product.farmerId, farmerName = product.farmerName,
            imageUrl = product.imageUrl, priceAtAdd = product.pricePerKg, currentPrice = product.pricePerKg,
            targetPrice = targetPrice, quality = product.quality, district = product.district, hasPriceAlert = targetPrice > 0,
            syncStatus = "PENDING"
        ))
        triggerSync()
    }
    override suspend fun removeFromWishlist(productId: String) = runCatching { 
        val item = wishlistDao.getWishlistItemByProduct(productId) ?: return@runCatching
        wishlistDao.removeWishlistItem(item.wishlistId)
        FirebaseFirestore.getInstance().collection("buyer_wishlist").document(item.wishlistId).delete()
    }
    override suspend fun isInWishlist(productId: String) = wishlistDao.isInWishlist(productId) > 0
    override suspend fun setPriceAlert(productId: String, targetPrice: Double) = runCatching {
        val item = wishlistDao.getWishlistItemByProduct(productId) ?: return@runCatching
        wishlistDao.insertWishlistItem(item.copy(targetPrice = targetPrice, hasPriceAlert = targetPrice > 0, syncStatus = "PENDING"))
        triggerSync()
    }
    override fun getActivePriceAlerts() = priceAlertDao.getActiveAlerts().map { it.map { e -> PriceAlert(e.alertId, e.cropName, e.targetPrice, e.currentPrice, e.district, e.isActive, e.isTriggered) } }
    
    private fun triggerSync() {
        val req = OneTimeWorkRequestBuilder<com.krishiai.app.services.BuyerSyncWorker>().build()
        WorkManager.getInstance(context).enqueue(req)
    }
    override suspend fun createPriceAlert(cropName: String, targetPrice: Double, district: String) = runCatching {
        priceAlertDao.insertAlert(PriceAlertEntity(alertId = UUID.randomUUID().toString(), cropName = cropName, targetPrice = targetPrice, district = district))
    }
    override suspend fun deletePriceAlert(alertId: String) = runCatching { priceAlertDao.deleteAlert(alertId) }
}

// ─────────────────────────────────────────────
// BuyerSearchRepositoryImpl
// ─────────────────────────────────────────────
@Singleton
class BuyerSearchRepositoryImpl @Inject constructor(
    private val searchHistoryDao: BuyerSearchHistoryDao
) : BuyerSearchRepository {
    override fun getSearchHistory() = searchHistoryDao.getRecentSearches()
    override suspend fun saveSearch(query: String, queryType: String, resultCount: Int) {
        searchHistoryDao.insertSearch(BuyerSearchHistoryEntity(UUID.randomUUID().toString(), query, queryType, resultCount))
    }
    override suspend fun clearSearchHistory() { searchHistoryDao.clearHistory() }
}

// ─────────────────────────────────────────────
// BuyerMarketRepositoryImpl
// ─────────────────────────────────────────────
@Singleton
class BuyerMarketRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val apmcPriceDao: APMCPriceDao,
    private val marketIntelligenceDao: MarketIntelligenceDao
) : BuyerMarketRepository {
    override fun getMarketPrices(district: String) = apmcPriceDao.getPricesByDistrict(district).map { it.map { e ->
        APMCPrice(e.marketName, e.district, e.cropName, e.cropCategory, e.minPrice, e.maxPrice, e.modalPrice, com.krishiai.app.data.model.PriceMovement.valueOf(e.trend), e.trendPercent, e.dateFetched)
    }}
    override fun getMarketIntelligence(district: String) = marketIntelligenceDao.getIntelligenceByDistrict(district).map { it.map { e -> e.toModel() } }
    override suspend fun refreshMarketData(district: String): Result<Unit> = runCatching { }
    override suspend fun getNearbySellersByLocation(lat: Double, lon: Double, radiusKm: Double): Result<List<NearbySeller>> = runCatching {
        val snapshot = firestore.collection("users").whereEqualTo("role", "FARMER").get().await()
        snapshot.documents.mapNotNull { doc ->
            try {
                val farmerLat = doc.getDouble("latitude") ?: return@mapNotNull null
                val farmerLon = doc.getDouble("longitude") ?: return@mapNotNull null
                val distance = haversineDistance(lat, lon, farmerLat, farmerLon)
                if (distance > radiusKm) return@mapNotNull null
                NearbySeller(
                    farmerId = doc.id, farmerName = doc.getString("name") ?: "", farmName = doc.getString("businessName") ?: "",
                    profilePhotoUrl = doc.getString("profilePhotoUrl") ?: "", rating = (doc.getDouble("rating") ?: 0.0).toFloat(),
                    totalDeals = (doc.getLong("totalDeals") ?: 0L).toInt(), trustScore = ((doc.getDouble("rating") ?: 0.0) / 5f * 100f).toFloat(),
                    distanceKm = distance, etaMinutes = (distance * 3).toInt() + 10, latitude = farmerLat, longitude = farmerLon,
                    district = doc.getString("district") ?: "", taluk = doc.getString("taluk") ?: "", isVerified = doc.getBoolean("isVerified") ?: false
                )
            } catch (e: Exception) { null }
        }.sortedBy { it.distanceKm }
    }
}

// ---------------------------------------------
// BuyerNotificationRepositoryImpl
// ---------------------------------------------
@Singleton
class BuyerNotificationRepositoryImpl @Inject constructor(
    private val notificationDao: com.krishiai.app.data.local.dao.NotificationDao
) : BuyerNotificationRepository {
    override fun getNotifications(): Flow<List<BuyerNotification>> {
        return notificationDao.getAllNotifications().map { entities ->
            entities.map { e ->
                BuyerNotification(
                    id = e.id,
                    title = e.title,
                    message = e.message,
                    category = try { NotificationCategory.valueOf(e.category) } catch (ex: Exception) { NotificationCategory.SYSTEM },
                    timestamp = e.timestamp,
                    isRead = e.isRead,
                    actionPayload = e.actionPayload,
                    deepLink = e.deepLink
                )
            }
        }
    }
    
    override fun getUnreadCount(): Flow<Int> = notificationDao.getUnreadCount()
    
    override suspend fun markAsRead(notificationId: String): Result<Unit> = runCatching {
        notificationDao.markAsRead(notificationId)
    }
    
    override suspend fun markAllAsRead(): Result<Unit> = runCatching {
        notificationDao.markAllAsRead()
    }
    
    override suspend fun deleteOldNotifications(olderThan: Long): Result<Unit> = runCatching {
        notificationDao.deleteOldNotifications(olderThan)
    }
}


// ---------------------------------------------
// BuyerAnalyticsRepositoryImpl
// ---------------------------------------------
@Singleton
class BuyerAnalyticsRepositoryImpl @Inject constructor(
    private val buyerOrderDao: com.krishiai.app.buyer.data.dao.BuyerOrderDao
) : com.krishiai.app.buyer.domain.repository.BuyerAnalyticsRepository {
    override fun getAnalyticsSummary(buyerId: String): Flow<com.krishiai.app.buyer.data.model.BuyerAnalyticsSummary> {
        return buyerOrderDao.getOrdersByBuyer(buyerId).map { entities ->
            val completed = entities.filter { it.status == "DELIVERED" }
            val spending = completed.sumOf { it.totalAmount }
            // For savings, we just mock 10% of spending as savings since we dont store market price historically in the order currently
            val savings = spending * 0.10
            
            // Generate some mock chart data for the last 6 months based on spending
            val trend = List(6) { idx -> if (completed.isEmpty()) 0.0 else spending / 6 + (Math.random() * 500) }
            
            val catMap = completed.groupBy { "Crops" }.mapValues { (_, list) -> list.sumOf { it.totalAmount } }
            
            com.krishiai.app.buyer.data.model.BuyerAnalyticsSummary(
                totalOrders = completed.size,
                totalSpending = spending,
                totalSavings = savings,
                favoriteCategory = "Crops",
                monthlySpendingTrend = trend,
                spendingByCategory = catMap
            )
        }
    }
}

