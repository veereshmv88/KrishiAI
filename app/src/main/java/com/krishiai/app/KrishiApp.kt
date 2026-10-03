package com.krishiai.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.krishiai.app.services.SyncWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class KrishiApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var marketplaceSyncManager: com.krishiai.app.core.sync.MarketplaceSyncManager

    @Inject
    lateinit var buyerSyncManager: com.krishiai.app.core.sync.BuyerSyncManager

    @Inject
    lateinit var farmerSyncManager: com.krishiai.app.core.sync.FarmerSyncManager

    @Inject
    lateinit var orderSyncManager: com.krishiai.app.core.sync.OrderSyncManager

    @Inject
    lateinit var notificationSyncManager: com.krishiai.app.core.sync.NotificationSyncManager

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        
        marketplaceSyncManager.startSync()
        buyerSyncManager.startSync()
        farmerSyncManager.startSync()
        orderSyncManager.startSync()
        notificationSyncManager.startSync()

        // Explicitly configure Firestore Offline Persistence
        try {
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
            FirebaseFirestore.getInstance().firestoreSettings = settings
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Schedule periodic background sync every 6 hours
        schedulePeriodicSync()
    }

    private fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(6, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "krishi_data_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}
