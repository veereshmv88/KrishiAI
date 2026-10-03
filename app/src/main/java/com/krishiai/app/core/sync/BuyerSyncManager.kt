package com.krishiai.app.core.sync

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.krishiai.app.buyer.data.dao.CartItemDao
import com.krishiai.app.buyer.data.dao.WishlistDao
import com.krishiai.app.buyer.data.entity.CartItemEntity
import com.krishiai.app.buyer.data.entity.WishlistItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BuyerSyncManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val cartDao: CartItemDao,
    private val wishlistDao: WishlistDao
) {
    private var cartListener: ListenerRegistration? = null
    private var wishlistListener: ListenerRegistration? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    fun startSync() {
        startCartSync()
        startWishlistSync()
    }

    private fun startCartSync() {
        if (cartListener != null) return
        val userId = auth.currentUser?.uid ?: return

        cartListener = firestore.collection("buyer_cart")
            .whereEqualTo("buyerId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                scope.launch {
                    val added = mutableListOf<CartItemEntity>()
                    for (change in snapshot.documentChanges) {
                        val doc = change.document
                        when (change.type) {
                            DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                                added.add(
                                    CartItemEntity(
                                        cartItemId = doc.id,
                                        productId = doc.getString("productId") ?: "",
                                        farmerId = doc.getString("farmerId") ?: "",
                                        farmerName = doc.getString("farmerName") ?: "",
                                        cropName = doc.getString("cropName") ?: "",
                                        cropCategory = doc.getString("cropCategory") ?: "",
                                        quality = doc.getString("quality") ?: "Good",
                                        grade = doc.getString("grade") ?: "A",
                                        imageUrl = doc.getString("imageUrl") ?: "",
                                        pricePerKg = doc.getDouble("pricePerKg") ?: 0.0,
                                        quantityKg = doc.getDouble("quantityKg") ?: 0.0,
                                        totalAmount = doc.getDouble("totalAmount") ?: 0.0,
                                        district = doc.getString("district") ?: "",
                                        taluk = doc.getString("taluk") ?: "",
                                        isAvailable = doc.getBoolean("isAvailable") ?: true,
                                        addedAt = doc.getLong("addedAt") ?: System.currentTimeMillis()
                                    )
                                )
                            }
                            DocumentChange.Type.REMOVED -> {
                                cartDao.deleteCartItem(doc.id)
                            }
                        }
                    }
                    added.forEach { cartDao.insertCartItem(it) }
                }
            }
    }

    private fun startWishlistSync() {
        if (wishlistListener != null) return
        val userId = auth.currentUser?.uid ?: return

        wishlistListener = firestore.collection("buyer_wishlist")
            .whereEqualTo("buyerId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                scope.launch {
                    for (change in snapshot.documentChanges) {
                        val doc = change.document
                        when (change.type) {
                            DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                                wishlistDao.insertWishlistItem(
                                    WishlistItemEntity(
                                        wishlistId = doc.id,
                                        productId = doc.getString("productId") ?: "",
                                        cropName = doc.getString("cropName") ?: "",
                                        cropCategory = doc.getString("cropCategory") ?: "",
                                        farmerId = doc.getString("farmerId") ?: "",
                                        farmerName = doc.getString("farmerName") ?: "",
                                        imageUrl = doc.getString("imageUrl") ?: "",
                                        priceAtAdd = doc.getDouble("priceAtAdd") ?: 0.0,
                                        currentPrice = doc.getDouble("currentPrice") ?: 0.0,
                                        targetPrice = doc.getDouble("targetPrice") ?: 0.0,
                                        quality = doc.getString("quality") ?: "Good",
                                        district = doc.getString("district") ?: "",
                                        hasPriceAlert = doc.getBoolean("hasPriceAlert") ?: false,
                                        alertTriggered = doc.getBoolean("alertTriggered") ?: false,
                                        addedAt = doc.getLong("addedAt") ?: System.currentTimeMillis()
                                    )
                                )
                            }
                            DocumentChange.Type.REMOVED -> {
                                wishlistDao.removeWishlistItem(doc.id)
                            }
                        }
                    }
                }
            }
    }

    fun stopSync() {
        cartListener?.remove()
        wishlistListener?.remove()
        cartListener = null
        wishlistListener = null
    }
}
