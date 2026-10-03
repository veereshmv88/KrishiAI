package com.krishiai.app.data.remote

import com.krishiai.app.data.local.entity.APMCPriceEntity
import kotlinx.coroutines.delay
import java.util.*
import javax.inject.Inject

interface ApmcApi {
    suspend fun getLiveMarketPrices(district: String): List<APMCPriceEntity>
    suspend fun getGlobalMarketPrices(): List<APMCPriceEntity>
}

class MockApmcApiImpl @Inject constructor() : ApmcApi {
    override suspend fun getLiveMarketPrices(district: String): List<APMCPriceEntity> {
        delay(800) // Simulate network latency
        return generateMockData().filter { it.district.equals(district, ignoreCase = true) }
    }

    override suspend fun getGlobalMarketPrices(): List<APMCPriceEntity> {
        delay(1200)
        return generateMockData()
    }

    private fun generateMockData(): List<APMCPriceEntity> {
        val currentTime = System.currentTimeMillis()
        return listOf(
            APMCPriceEntity(0, "Yeshwanthpur APMC", "Bangalore Urban", "Tomato", "Vegetable", 20.0, 35.0, 28.0, "UP", 12.5, currentTime),
            APMCPriceEntity(0, "Yeshwanthpur APMC", "Bangalore Urban", "Onion", "Vegetable", 18.0, 25.0, 22.0, "STABLE", 0.0, currentTime),
            APMCPriceEntity(0, "K R Market", "Bangalore Urban", "Chilli", "Spice", 80.0, 110.0, 95.0, "UP", 5.2, currentTime),
            APMCPriceEntity(0, "Mysuru APMC", "Mysuru", "Tomato", "Vegetable", 18.0, 30.0, 24.0, "DOWN", -8.0, currentTime),
            APMCPriceEntity(0, "Mysuru APMC", "Mysuru", "Banana", "Fruit", 20.0, 30.0, 25.0, "STABLE", 0.0, currentTime),
            APMCPriceEntity(0, "Hubli APMC", "Hubli", "Groundnut", "Oilseed", 50.0, 65.0, 58.0, "UP", 3.4, currentTime)
        )
    }
}
