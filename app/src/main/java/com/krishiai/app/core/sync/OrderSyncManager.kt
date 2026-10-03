package com.krishiai.app.core.sync

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.krishiai.app.buyer.data.dao.BuyerOrderDao
import com.krishiai.app.buyer.data.entity.BuyerOrderEntity
import com.krishiai.app.data.model.SyncStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderSyncManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val orderDao: BuyerOrderDao
) {
    private var ordersListener: ListenerRegistration? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    fun startSync() {
        if (ordersListener != null) return

        val userId = auth.currentUser?.uid ?: return

        ordersListener = firestore.collection("buyer_orders")
            .whereEqualTo("buyerId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener

                scope.launch {
                    val added = mutableListOf<BuyerOrderEntity>()
                    val modified = mutableListOf<BuyerOrderEntity>()

                    for (change in snapshot.documentChanges) {
                        val doc = change.document
                        try {
                            val entity = BuyerOrderEntity(
                                orderId = doc.id,
                                productId = doc.getString("productId") ?: "",
                                farmerId = doc.getString("farmerId") ?: "",
                                farmerName = doc.getString("farmerName") ?: "",
                                buyerId = doc.getString("buyerId") ?: "",
                                cropName = doc.getString("cropName") ?: "",
                                cropCategory = doc.getString("cropCategory") ?: "",
                                quality = doc.getString("quality") ?: "Good",
                                grade = doc.getString("grade") ?: "A",
                                imageUrl = doc.getString("imageUrl") ?: "",
                                quantityKg = doc.getDouble("quantityKg") ?: 0.0,
                                pricePerKg = doc.getDouble("pricePerKg") ?: 0.0,
                                subtotal = doc.getDouble("subtotal") ?: 0.0,
                                deliveryCharge = doc.getDouble("deliveryCharge") ?: 0.0,
                                gst = doc.getDouble("gst") ?: 0.0,
                                discount = doc.getDouble("discount") ?: 0.0,
                                totalAmount = doc.getDouble("totalAmount") ?: 0.0,
                                status = doc.getString("status") ?: "PROCESSING",
                                deliveryAddress = doc.getString("deliveryAddress") ?: "",
                                paymentMethod = doc.getString("paymentMethod") ?: "CASH_ON_DELIVERY",
                                paymentStatus = doc.getString("paymentStatus") ?: "PENDING",
                                estimatedDelivery = doc.getLong("estimatedDelivery") ?: 0L,
                                deliveredAt = doc.getLong("deliveredAt") ?: 0L,
                                cancellationReason = doc.getString("cancellationReason") ?: "",
                                farmerRating = (doc.getDouble("farmerRating") ?: 0.0).toFloat(),
                                review = doc.getString("review") ?: "",
                                trackingEvents = doc.getString("trackingEvents") ?: "[]",
                                couponCode = doc.getString("couponCode") ?: "",
                                couponDiscount = doc.getDouble("couponDiscount") ?: 0.0,
                                invoiceUrl = doc.getString("invoiceUrl") ?: "",
                                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                                lastUpdated = doc.getLong("lastUpdated") ?: System.currentTimeMillis(),
                                syncStatus = SyncStatus.SYNCED.name
                            )

                            when (change.type) {
                                DocumentChange.Type.ADDED -> added.add(entity)
                                DocumentChange.Type.MODIFIED -> modified.add(entity)
                                DocumentChange.Type.REMOVED -> orderDao.deleteOrder(doc.id)
                            }
                        } catch (ex: Exception) {
                            // Log error
                        }
                    }

                    if (added.isNotEmpty()) orderDao.insertOrders(added)
                    if (modified.isNotEmpty()) orderDao.insertOrders(modified)
                }
            }
    }

    fun stopSync() {
        ordersListener?.remove()
        ordersListener = null
    }
}
