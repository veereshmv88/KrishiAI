package com.krishiai.app.services

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.krishiai.app.core.notifications.NotificationSyncStatus
import com.krishiai.app.data.local.dao.NotificationDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await

@HiltWorker
class NotificationSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationDao: NotificationDao,
    private val firestore: FirebaseFirestore
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val pendingNotifs = notificationDao.getAllNotifications().firstOrNull()?.filter { it.syncStatus == NotificationSyncStatus.PENDING.name }
            
            if (pendingNotifs.isNullOrEmpty()) {
                return Result.success()
            }
            
            // Assume the user is "dummy_user_id" for now, ideally retrieved from UserAuth
            val userId = "dummy_user_id"
            
            val batch = firestore.batch()
            
            for (notif in pendingNotifs) {
                val docRef = firestore.collection("users").document(userId).collection("notifications").document(notif.id)
                // If it's archived (deleted), we might want to delete from firestore or update status
                if (notif.isArchived) {
                    batch.delete(docRef)
                } else {
                    batch.set(docRef, mapOf(
                        "isRead" to notif.isRead,
                        "isFavorite" to notif.isFavorite
                    ))
                }
            }
            
            batch.commit().await()
            
            // Update local DB to SYNCED
            val syncedNotifs = pendingNotifs.map { it.copy(syncStatus = NotificationSyncStatus.SYNCED.name) }
            notificationDao.insertNotifications(syncedNotifs)
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
