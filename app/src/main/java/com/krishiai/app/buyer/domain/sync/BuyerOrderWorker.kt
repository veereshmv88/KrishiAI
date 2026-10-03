package com.krishiai.app.buyer.domain.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.krishiai.app.buyer.data.dao.BuyerOrderDao
import com.krishiai.app.buyer.data.model.SyncState
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

@HiltWorker
class BuyerOrderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val buyerOrderDao: BuyerOrderDao,
    private val firestore: FirebaseFirestore
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val pendingOrders = buyerOrderDao.getPendingSyncOrders()
            
            if (pendingOrders.isEmpty()) {
                return Result.success()
            }
            
            var successCount = 0
            
            for (order in pendingOrders) {
                try {
                    buyerOrderDao.updateSyncStatus(order.orderId, SyncState.SYNCING.name)
                    
                    firestore.runTransaction { transaction ->
                        val prodRef = firestore.collection("products").document(order.productId)
                        val snapshot = transaction.get(prodRef)
                        
                        if (snapshot.exists()) {
                            val status = snapshot.getString("status")
                            val availableQty = snapshot.getDouble("quantityKg") ?: 0.0
                            
                            if (status == "SOLD" || availableQty < order.quantityKg) {
                                throw Exception("Out of stock")
                            }
                            
                            val newQty = availableQty - order.quantityKg
                            val newStatus = if (newQty <= 0) "SOLD" else "AVAILABLE"
                            
                            transaction.update(prodRef, mapOf(
                                "quantityKg" to newQty,
                                "status" to newStatus
                            ))
                            
                            val analyticsRef = firestore.collection("analytics").document(order.farmerId)
                            val analyticsSnap = transaction.get(analyticsRef)
                            if (analyticsSnap.exists()) {
                                val currentSales = analyticsSnap.getLong("totalSales") ?: 0L
                                transaction.update(analyticsRef, "totalSales", currentSales + 1)
                            } else {
                                transaction.set(analyticsRef, mapOf("totalSales" to 1L))
                            }
                        } else {
                            throw Exception("Product removed")
                        }
                        
                        val orderRef = firestore.collection("buyer_orders").document(order.orderId)
                        transaction.set(orderRef, mapOf<String, Any>(
                            "orderId" to order.orderId,
                            "productId" to order.productId,
                            "farmerId" to order.farmerId,
                            "buyerId" to order.buyerId,
                            "cropName" to order.cropName,
                            "quantityKg" to order.quantityKg,
                            "totalAmount" to order.totalAmount,
                            "status" to order.status,
                            "createdAt" to order.createdAt,
                            "paymentMethod" to order.paymentMethod
                        ))
                    }.await()
                    
                    buyerOrderDao.updateSyncStatus(order.orderId, SyncState.SYNCED.name)
                    successCount++
                } catch (e: Exception) {
                    val isStockError = e.message?.contains("Out of stock") == true || e.message?.contains("Product removed") == true
                    if (isStockError) {
                        buyerOrderDao.updateSyncStatus(order.orderId, SyncState.FAILED.name)
                        buyerOrderDao.updateOrderStatus(order.orderId, "CANCELLED", System.currentTimeMillis())
                        successCount++ // Treat as processed, don't retry
                    } else {
                        buyerOrderDao.updateSyncStatus(order.orderId, SyncState.PENDING.name)
                    }
                }
            }
            
            if (successCount == pendingOrders.size) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
