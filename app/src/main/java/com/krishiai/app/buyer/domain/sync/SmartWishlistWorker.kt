package com.krishiai.app.buyer.domain.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.krishiai.app.buyer.data.model.BuyerNotification
import com.krishiai.app.buyer.data.model.NotificationCategory
import com.krishiai.app.buyer.domain.repository.BuyerNotificationRepository
import com.krishiai.app.buyer.domain.repository.BuyerWishlistRepository
import com.krishiai.app.data.local.dao.APMCPriceDao
import com.krishiai.app.data.local.entity.NotificationEntity
import com.krishiai.app.core.notifications.NotificationManagerImpl
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.UUID

@HiltWorker
class SmartWishlistWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val wishlistRepository: BuyerWishlistRepository,
    private val apmcPriceDao: APMCPriceDao,
    private val notificationDao: com.krishiai.app.data.local.dao.NotificationDao
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val alerts = wishlistRepository.getActivePriceAlerts().first()
            val apmcPrices = apmcPriceDao.getAllPrices().first()

            var notificationsGenerated = 0

            for (alert in alerts) {
                val currentPrice = apmcPrices.firstOrNull { it.cropName.equals(alert.cropName, ignoreCase = true) }?.modalPrice
                
                if (currentPrice != null && currentPrice <= alert.targetPrice) {
                    val notif = NotificationEntity(
                        id = UUID.randomUUID().toString(),
                        title = "Price Alert Triggered!",
                        message = "${alert.cropName} price has dropped to ?$currentPrice/kg in ${alert.district} (Target: ?${alert.targetPrice})",
                        type = NotificationCategory.PRICE_ALERTS.name,
                        category = NotificationCategory.PRICE_ALERTS.name,
                        priority = "HIGH",
                        timestamp = System.currentTimeMillis(),
                        isRead = false,
                        syncStatus = "LOCAL"
                    )
                    
                    notificationDao.insertNotification(notif)
                    notificationsGenerated++
                    
                    // Mark alert as triggered
                    wishlistRepository.deletePriceAlert(alert.alertId)
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

