package com.krishiai.app.services

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.krishiai.app.domain.repository.MarketplaceRepository
import com.krishiai.app.domain.repository.WeatherRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val marketplaceRepository: MarketplaceRepository,
    private val weatherRepository: WeatherRepository,
    private val auth: FirebaseAuth
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.success()

            // Sync APMC Market Prices for Karnataka districts
            val districts = listOf("Bangalore Urban", "Mysuru", "Hubli", "Belagavi", "Bagalkot", "Kalaburagi")
            districts.forEach { district ->
                marketplaceRepository.refreshAPMCPrices(district)
                marketplaceRepository.refreshMarketIntelligence(district)
            }

            // Sync weather for user's district (use Bangalore as fallback)
            weatherRepository.getCurrentWeather(forceRefresh = true)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
