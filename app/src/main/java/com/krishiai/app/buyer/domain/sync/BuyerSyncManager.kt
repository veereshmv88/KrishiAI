package com.krishiai.app.buyer.domain.sync

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.krishiai.app.buyer.data.dao.BuyerOrderDao
import com.krishiai.app.buyer.data.dao.WishlistDao
import com.krishiai.app.buyer.data.entity.BuyerOrderEntity
import com.krishiai.app.buyer.data.model.SyncState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BuyerSyncManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val buyerOrderDao: BuyerOrderDao,
    private val wishlistDao: WishlistDao
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    
    private var orderListener: ListenerRegistration? = null
    private var productListener: ListenerRegistration? = null
    
    // Shared Flow to notify UI of sync events
    private val _syncEvents = MutableSharedFlow<SyncEvent>()
    val syncEvents = _syncEvents.asSharedFlow()

    fun startListening() {
        val uid = auth.currentUser?.uid ?: return
        
        // 1. Listen to Buyer Orders
        orderListener?.remove()
        orderListener = firestore.collection("buyer_orders")
            .whereEqualTo("buyerId", uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                
                scope.launch {
                    val currentOrders = buyerOrderDao.getOrdersByBuyerSync(uid)
                    
                    for (doc in snapshot.documents) {
                        val serverStatus = doc.getString("status") ?: "PROCESSING"
                        val localOrder = currentOrders.find { it.orderId == doc.id }
                        
                        if (localOrder != null) {
                            // If local is PENDING but server has it, mark SYNCED
                            if (localOrder.syncStatus == SyncState.PENDING.name) {
                                buyerOrderDao.updateSyncStatus(localOrder.orderId, SyncState.SYNCED.name)
                            }
                            // Sync status updates from seller/admin
                            if (localOrder.status != serverStatus) {
                                buyerOrderDao.updateOrderStatus(localOrder.orderId, serverStatus, System.currentTimeMillis())
                                _syncEvents.emit(SyncEvent.OrderUpdated(localOrder.orderId, serverStatus))
                            }
                        }
                    }
                }
            }
            
        // 2. Listen to Products (For live dashboard updates)
        productListener?.remove()
        productListener = firestore.collection("products")
            .whereEqualTo("status", "AVAILABLE")
            .limit(100) // Limit to avoid massive reads on dashboard
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                scope.launch {
                    _syncEvents.emit(SyncEvent.ProductsUpdated)
                }
            }
    }

    fun stopListening() {
        orderListener?.remove()
        orderListener = null
        productListener?.remove()
        productListener = null
    }
}

sealed class SyncEvent {
    data class OrderUpdated(val orderId: String, val status: String) : SyncEvent()
    object ProductsUpdated : SyncEvent()
}
