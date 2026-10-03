package com.krishiai.app.services

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.krishiai.app.buyer.data.dao.BuyerOrderDao
import com.krishiai.app.buyer.data.dao.CartItemDao
import com.krishiai.app.buyer.data.dao.WishlistDao
import com.krishiai.app.data.model.SyncStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

@HiltWorker
class BuyerSyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val orderDao: BuyerOrderDao,
    private val cartItemDao: CartItemDao,
    private val wishlistDao: WishlistDao,
    private val firestore: FirebaseFirestore
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val pendingOrders = orderDao.getPendingSyncOrders()

            // Push pending orders
            for (order in pendingOrders) {
                try {
                    val orderMap = hashMapOf(
                        "productId" to order.productId,
                        "farmerId" to order.farmerId,
                        "farmerName" to order.farmerName,
                        "buyerId" to order.buyerId,
                        "cropName" to order.cropName,
                        "cropCategory" to order.cropCategory,
                        "quality" to order.quality,
                        "grade" to order.grade,
                        "imageUrl" to order.imageUrl,
                        "quantityKg" to order.quantityKg,
                        "pricePerKg" to order.pricePerKg,
                        "subtotal" to order.subtotal,
                        "deliveryCharge" to order.deliveryCharge,
                        "gst" to order.gst,
                        "discount" to order.discount,
                        "totalAmount" to order.totalAmount,
                        "status" to order.status,
                        "deliveryAddress" to order.deliveryAddress,
                        "paymentMethod" to order.paymentMethod,
                        "paymentStatus" to order.paymentStatus,
                        "estimatedDelivery" to order.estimatedDelivery,
                        "deliveredAt" to order.deliveredAt,
                        "cancellationReason" to order.cancellationReason,
                        "farmerRating" to order.farmerRating,
                        "review" to order.review,
                        "trackingEvents" to order.trackingEvents,
                        "couponCode" to order.couponCode,
                        "couponDiscount" to order.couponDiscount,
                        "invoiceUrl" to order.invoiceUrl,
                        "createdAt" to order.createdAt,
                        "lastUpdated" to order.lastUpdated
                    )
                    
                    firestore.collection("buyer_orders").document(order.orderId)
                        .set(orderMap)
                        .await()
                        
                    orderDao.updateSyncStatus(order.orderId, SyncStatus.SYNCED.name)
                } catch (e: Exception) {
                    // Fail individual order but continue loop
                }
            }

            // Push pending cart items
            val pendingCart = cartItemDao.getPendingSyncCartItems()
            for (cart in pendingCart) {
                try {
                    val map = hashMapOf(
                        "productId" to cart.productId,
                        "farmerId" to cart.farmerId,
                        "farmerName" to cart.farmerName,
                        "buyerId" to (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""),
                        "cropName" to cart.cropName,
                        "cropCategory" to cart.cropCategory,
                        "quality" to cart.quality,
                        "grade" to cart.grade,
                        "imageUrl" to cart.imageUrl,
                        "pricePerKg" to cart.pricePerKg,
                        "quantityKg" to cart.quantityKg,
                        "totalAmount" to cart.totalAmount,
                        "district" to cart.district,
                        "taluk" to cart.taluk,
                        "isAvailable" to cart.isAvailable,
                        "addedAt" to cart.addedAt
                    )
                    firestore.collection("buyer_cart").document(cart.cartItemId).set(map).await()
                    cartItemDao.updateSyncStatus(cart.cartItemId, SyncStatus.SYNCED.name)
                } catch (e: Exception) {
                    // Ignore
                }
            }

            // Push pending wishlist items
            val pendingWishlist = wishlistDao.getPendingSyncWishlistItems()
            for (item in pendingWishlist) {
                try {
                    val map = hashMapOf(
                        "productId" to item.productId,
                        "cropName" to item.cropName,
                        "cropCategory" to item.cropCategory,
                        "farmerId" to item.farmerId,
                        "farmerName" to item.farmerName,
                        "buyerId" to (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""),
                        "imageUrl" to item.imageUrl,
                        "priceAtAdd" to item.priceAtAdd,
                        "currentPrice" to item.currentPrice,
                        "targetPrice" to item.targetPrice,
                        "quality" to item.quality,
                        "district" to item.district,
                        "hasPriceAlert" to item.hasPriceAlert,
                        "alertTriggered" to item.alertTriggered,
                        "addedAt" to item.addedAt
                    )
                    firestore.collection("buyer_wishlist").document(item.wishlistId).set(map).await()
                    wishlistDao.updateSyncStatus(item.wishlistId, SyncStatus.SYNCED.name)
                } catch (e: Exception) {
                    // Ignore
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
