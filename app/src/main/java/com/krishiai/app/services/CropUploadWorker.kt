package com.krishiai.app.services

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.krishiai.app.data.local.dao.CropListingDao
import com.krishiai.app.data.model.SyncStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.krishiai.app.data.model.Product
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

@HiltWorker
class CropUploadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val cropListingDao: CropListingDao,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val listingId = inputData.getString(KEY_LISTING_ID) ?: return Result.failure()

        val entity = cropListingDao.getListingById(listingId) ?: return Result.failure()

        if (entity.syncStatus == SyncStatus.SYNCED.name) {
            return Result.success()
        }

        return try {
            cropListingDao.updateSyncStatus(listingId, SyncStatus.UPLOADING.name)

            // Image Upload Logic
            var imageUrl = entity.images.split(",").firstOrNull().orEmpty()
            if (imageUrl.startsWith("file://")) {
                val filePath = imageUrl.replace("file://", "")
                val file = java.io.File(filePath)
                if (file.exists()) {
                    val ref = storage.reference.child("products/${java.util.UUID.randomUUID()}.jpg")
                    val uri = android.net.Uri.fromFile(file)
                    ref.putFile(uri).await()
                    imageUrl = ref.downloadUrl.await().toString()
                    
                    // Cleanup local cache
                    try { file.delete() } catch (e: Exception) {}
                }
            }
            
            // Map Entity to Firestore Map
            val map = mapOf(
                "farmerId" to entity.farmerId,
                "farmerName" to entity.farmerName,
                "farmerPhone" to entity.farmerPhone,
                "cropCategory" to entity.cropCategory,
                "cropName" to entity.cropName,
                "quantityKg" to entity.quantityKg,
                "quality" to entity.quality,
                "harvestDate" to entity.harvestDate,
                "description" to entity.description,
                "district" to entity.district,
                "taluk" to entity.taluk,
                "imageUrl" to imageUrl,
                "pricePerKg" to entity.pricePerKg,
                "aiRecommendedPrice" to entity.aiRecommendedPrice,
                "status" to if (entity.isSold) "SOLD" else "AVAILABLE",
                "createdAt" to entity.createdAt,
                "serverTimestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )

            // Firestore transaction or set
            firestore.collection("products").document(listingId)
                .set(map)
                .await()

            cropListingDao.updateSyncStatus(listingId, SyncStatus.SYNCED.name)
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            cropListingDao.updateSyncStatus(listingId, SyncStatus.FAILED.name)
            Result.retry()
        }
    }

    companion object {
        const val KEY_LISTING_ID = "LISTING_ID"
    }
}
