package com.krishiai.app.core.sync

import com.google.firebase.auth.FirebaseAuth
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
class FarmerSyncManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val cropListingDao: CropListingDao
) {
    private var farmerProductsListener: ListenerRegistration? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    fun startSync() {
        startProductsSync()
    }

    private fun startProductsSync() {
        if (farmerProductsListener != null) return
        val userId = auth.currentUser?.uid ?: return

        farmerProductsListener = firestore.collection("products")
            .whereEqualTo("farmerId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                scope.launch {
                    val addedListings = mutableListOf<CropListingEntity>()
                    val removedIds = mutableListOf<String>()

                    for (change in snapshot.documentChanges) {
                        val doc = change.document
                        when (change.type) {
                            DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                                parseCropListing(doc)?.let { addedListings.add(it) }
                            }
                            DocumentChange.Type.REMOVED -> {
                                removedIds.add(doc.id)
                            }
                        }
                    }

                    if (addedListings.isNotEmpty()) {
                        cropListingDao.insertListings(addedListings)
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
                quality = doc.getString("quality") ?: "",
                quantityKg = doc.getDouble("quantityKg") 
                    ?: doc.getString("quantity")?.filter { it.isDigit() || it == '.' }?.toDoubleOrNull() 
                    ?: 0.0,
                pricePerKg = doc.getDouble("pricePerKg")
                    ?: doc.getDouble("finalPrice") 
                    ?: doc.getDouble("basePrice") 
                    ?: 0.0,
                description = doc.getString("description") ?: "",
                images = doc.getString("imageUrl") ?: "", // Mapping Firebase imageUrl to Room images
                district = doc.getString("district") ?: "",
                taluk = doc.getString("taluk") ?: "",
                isSold = doc.getString("status") == "SOLD",
                harvestDate = doc.getLong("harvestDate") ?: System.currentTimeMillis(),
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }

    fun stopSync() {
        farmerProductsListener?.remove()
        farmerProductsListener = null
    }
}
