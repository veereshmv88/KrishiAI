package com.krishiai.app.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.RemoteMessage
import com.krishiai.app.MainActivity
import com.krishiai.app.R
import com.krishiai.app.data.local.dao.NotificationDao
import com.krishiai.app.data.local.entity.NotificationEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationDao: NotificationDao,
    private val firestore: FirebaseFirestore
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val androidNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationEntity>>(emptyList())
    val notifications: StateFlow<List<NotificationEntity>> = _notifications.asStateFlow()

    init {
        createNotificationChannels()
        observeLocalNotifications()
    }

    private fun observeLocalNotifications() {
        scope.launch {
            notificationDao.getAllNotifications().collectLatest { notifs ->
                val activeNotifs = notifs.filter { !it.isArchived }.sortedByDescending { it.timestamp }
                _notifications.value = activeNotifs
                _unreadCount.value = activeNotifs.count { !it.isRead }
            }
        }
    }

    fun handleFCMMessage(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data
        val title = remoteMessage.notification?.title ?: data["title"] ?: "KrishiAI Alert"
        val message = remoteMessage.notification?.body ?: data["message"] ?: ""
        
        val categoryStr = data["category"] ?: NotificationCategory.GENERAL.name
        val priorityStr = data["priority"] ?: NotificationPriority.NORMAL.name
        val actionPayload = data["actionPayload"]
        val deepLink = data["deepLink"]
        val senderId = data["senderId"]
        val senderName = data["senderName"]
        val imageUrl = data["imageUrl"]
        val notificationId = data["id"] ?: UUID.randomUUID().toString()

        val entity = NotificationEntity(
            id = notificationId,
            title = title,
            message = message,
            type = categoryStr, // Backwards compatibility
            category = categoryStr,
            priority = priorityStr,
            senderId = senderId,
            senderName = senderName,
            relatedId = data["relatedId"] ?: "",
            actionPayload = actionPayload,
            deepLink = deepLink,
            imageUrl = imageUrl,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            syncStatus = NotificationSyncStatus.SYNCED.name // Received from remote
        )

        scope.launch {
            notificationDao.insertNotification(entity)
            triggerAndroidNotification(entity)
        }
    }

    private fun triggerAndroidNotification(entity: NotificationEntity) {
        val channelId = getChannelIdForCategory(entity.category)
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_id", entity.id)
            putExtra("deep_link", entity.deepLink)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, entity.id.hashCode(), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(entity.title)
            .setContentText(entity.message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val priorityEnum = try { NotificationPriority.valueOf(entity.priority) } catch(e: Exception) { NotificationPriority.NORMAL }
        
        if (priorityEnum == NotificationPriority.HIGH || priorityEnum == NotificationPriority.CRITICAL) {
            builder.priority = NotificationCompat.PRIORITY_HIGH
            builder.setDefaults(NotificationCompat.DEFAULT_ALL)
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            builder.setVibrate(longArrayOf(1000, 1000, 1000, 1000))
        } else {
            builder.priority = NotificationCompat.PRIORITY_DEFAULT
        }

        androidNotificationManager.notify(entity.id.hashCode(), builder.build())
    }

    private fun getChannelIdForCategory(category: String): String {
        return when (category) {
            NotificationCategory.WEATHER_ALERTS.name -> "KrishiAI_Weather_Alerts"
            NotificationCategory.MARKETPLACE.name, NotificationCategory.BUY_REQUESTS.name, NotificationCategory.SELL_REQUESTS.name -> "KrishiAI_Marketplace"
            NotificationCategory.AI_DISEASE_DETECTION.name, NotificationCategory.AI_CROP_HEALTH_REPORTS.name, NotificationCategory.AI_RECOMMENDATIONS.name -> "KrishiAI_AI_Alerts"
            NotificationCategory.SECURITY_ALERTS.name, NotificationCategory.PAYMENTS.name -> "KrishiAI_Security"
            NotificationCategory.CHAT_MESSAGES.name -> "KrishiAI_Messages"
            NotificationCategory.GOVT_SCHEMES.name -> "KrishiAI_Govt_Schemes"
            NotificationCategory.ORDER_UPDATES.name -> "KrishiAI_Orders"
            else -> "KrishiAI_General"
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel("KrishiAI_Weather_Alerts", "Weather Alerts", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel("KrishiAI_Marketplace", "Marketplace Updates", NotificationManager.IMPORTANCE_DEFAULT),
                NotificationChannel("KrishiAI_AI_Alerts", "AI & Crop Health", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel("KrishiAI_Security", "Security & Payments", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel("KrishiAI_Messages", "Messages", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel("KrishiAI_Govt_Schemes", "Government Schemes", NotificationManager.IMPORTANCE_LOW),
                NotificationChannel("KrishiAI_Orders", "Order Updates", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel("KrishiAI_General", "General Notifications", NotificationManager.IMPORTANCE_DEFAULT)
            )
            
            channels.forEach { channel ->
                if (channel.id == "KrishiAI_Weather_Alerts" || channel.id == "KrishiAI_Security" || channel.id == "KrishiAI_AI_Alerts") {
                    channel.enableVibration(true)
                }
                androidNotificationManager.createNotificationChannel(channel)
            }
        }
    }

    fun markAsRead(id: String) {
        scope.launch {
            val notification = _notifications.value.find { it.id == id }
            if (notification != null && !notification.isRead) {
                val updated = notification.copy(isRead = true, syncStatus = NotificationSyncStatus.PENDING.name)
                notificationDao.insertNotification(updated)
                // In a real app, this would also enqueue WorkManager to sync with Firestore
            }
        }
    }

    fun markAllAsRead() {
        scope.launch {
            val unread = _notifications.value.filter { !it.isRead }.map { it.copy(isRead = true, syncStatus = NotificationSyncStatus.PENDING.name) }
            if (unread.isNotEmpty()) {
                notificationDao.insertNotifications(unread)
                // Enqueue WorkManager
            }
        }
    }
    
    fun deleteNotification(id: String) {
        scope.launch {
            val notification = _notifications.value.find { it.id == id }
            if (notification != null) {
                val updated = notification.copy(isArchived = true, syncStatus = NotificationSyncStatus.PENDING.name)
                notificationDao.insertNotification(updated)
            }
        }
    }
}
