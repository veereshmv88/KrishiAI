package com.krishiai.app.core.sync

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.krishiai.app.data.local.dao.NotificationDao
import com.krishiai.app.data.local.entity.NotificationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationSyncManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val notificationDao: NotificationDao
) {
    private var notificationListener: ListenerRegistration? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    fun startSync() {
        if (notificationListener != null) return

        val userId = auth.currentUser?.uid ?: return

        notificationListener = firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                scope.launch {
                    val added = mutableListOf<NotificationEntity>()
                    val modified = mutableListOf<NotificationEntity>()
                    for (change in snapshot.documentChanges) {
                        val doc = change.document
                        try {
                            val entity = NotificationEntity(
                                id = doc.id,
                                title = doc.getString("title") ?: "",
                                message = doc.getString("message") ?: "",
                                type = doc.getString("type") ?: "SYSTEM",
                                relatedId = doc.getString("relatedId") ?: "",
                                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                isRead = doc.getBoolean("isRead") ?: false,
                                category = doc.getString("category") ?: "GENERAL",
                                priority = doc.getString("priority") ?: "NORMAL",
                                senderId = doc.getString("senderId"),
                                senderName = doc.getString("senderName"),
                                actionPayload = doc.getString("actionPayload")
                            )
                            when (change.type) {
                                DocumentChange.Type.ADDED -> added.add(entity)
                                DocumentChange.Type.MODIFIED -> modified.add(entity)
                                DocumentChange.Type.REMOVED -> notificationDao.deleteNotification(doc.id)
                            }
                        } catch (ex: Exception) {
                            // Log error
                        }
                    }
                    if (added.isNotEmpty()) notificationDao.insertNotifications(added)
                    if (modified.isNotEmpty()) notificationDao.insertNotifications(modified)
                }
            }
    }

    fun stopSync() {
        notificationListener?.remove()
        notificationListener = null
    }
}
