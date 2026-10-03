package com.krishiai.app.core.sync

import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.krishiai.app.data.local.dao.CropListingDao
import com.krishiai.app.data.local.entity.CropListingEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarketplaceSyncManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val cropListingDao: CropListingDao
) {
    private var globalProductsListener: ListenerRegistration? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    fun startSync() {
        startProductsSync()
    }

    private fun startProductsSync() {
        if (globalProductsListener != null) return

        // Removing the 'whereEqualTo("status", "AVAILABLE")' filter ensures we receive updates 
        // when a product is marked as SOLD.
        globalProductsListener = firestore.collection("products")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener

                scope.launch {
                    val addedListings = mutableListOf<CropListingEntity>()
                    val modifiedListings = mutableListOf<CropListingEntity>()
                    val removedIds = mutableListOf<String>()

                    for (change in snapshot.documentChanges) {
                        val doc = change.document
                        when (change.type) {
                            DocumentChange.Type.ADDED -> {
                                parseCropListing(doc)?.let { addedListings.add(it) }
                            }
                            DocumentChange.Type.MODIFIED -> {
                                parseCropListing(doc)?.let { modifiedListings.add(it) }
                            }
                            DocumentChange.Type.REMOVED -> {
                                removedIds.add(doc.id)
                            }
                        }
                    }

                    if (addedListings.isNotEmpty()) {
                        cropListingDao.insertListings(addedListings)
                    }
                    if (modifiedListings.isNotEmpty()) {
                        // Assuming insertListings has OnConflictStrategy.REPLACE
                        cropListingDao.insertListings(modifiedListings)
                    }
                    if (removedIds.isNotEmpty()) {
                        removedIds.forEach { id ->
                            cropListingDao.deleteListingById(id)
                        }
                    }
                }
            }
    }

    private fun parseCropListing(doc: com.google.firebase.firestore.DocumentSnapshot): CropListingEntity? {
        return try {
            CropListingEntity(
                listingId = doc.id,
                farmerId = doc.getString("farmerId") ?: "",
                farmerName = doc.getString("farmerName") ?: "",
                farmerPhone = doc.getString("farmerPhone") ?: "",
                cropName = doc.getString("cropName") ?: "",
                cropCategory = doc.getString("cropCategory") ?: "",
                quantityKg = doc.getDouble("quantityKg") 
                    ?: doc.getString("quantity")?.filter { it.isDigit() || it == '.' }?.toDoubleOrNull() 
                    ?: 0.0,
                pricePerKg = doc.getDouble("pricePerKg")
                    ?: doc.getDouble("finalPrice") 
                    ?: doc.getDouble("basePrice") 
                    ?: 0.0,
                aiRecommendedPrice = doc.getDouble("aiRecommendedPrice") ?: 0.0,
                quality = doc.getString("quality") ?: "Good",
                harvestDate = doc.getLong("harvestDate") ?: System.currentTimeMillis(),
                images = doc.getString("imageUrl") ?: "",
                isSold = doc.getString("status") == "SOLD",
                district = doc.getString("district") ?: "",
                taluk = doc.getString("taluk") ?: "",
                description = doc.getString("description") ?: "",
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                syncStatus = "SYNCED",
                uploadProgress = 100,
                lastSyncTime = System.currentTimeMillis(),
                serverTimestamp = doc.getLong("serverTimestamp") ?: System.currentTimeMillis(),
                conflictVersion = 1
            )
        } catch (ex: Exception) {
            null
        }
    }

    fun stopSync() {
        globalProductsListener?.remove()
        globalProductsListener = null
    }
}
