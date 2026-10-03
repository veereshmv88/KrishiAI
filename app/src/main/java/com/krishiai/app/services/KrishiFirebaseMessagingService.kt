package com.krishiai.app.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.krishiai.app.MainActivity
import com.krishiai.app.core.notifications.NotificationManagerImpl
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class KrishiFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationManagerImpl: NotificationManagerImpl

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Route all FCM messages to the central NotificationManagerImpl
        notificationManagerImpl.handleFCMMessage(remoteMessage)
    }

    override fun onNewToken(token: String) {
        // Send token to server
    }
}
