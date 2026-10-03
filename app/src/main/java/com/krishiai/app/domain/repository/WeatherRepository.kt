package com.krishiai.app.domain.repository

import com.krishiai.app.data.model.WeatherData
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    
    /**
     * Attempts to resolve weather using the Location Priority System:
     * 1. GPS
     * 2. Registration coordinates
     * 3. Registration District/Taluk geocoding
     * 4. Cache
     */
    suspend fun getCurrentWeather(forceRefresh: Boolean = false): Result<WeatherData>
    
    /**
     * Fetches weather for a manually provided location string (e.g. "Mysuru", "Bagalkot")
     * Resolves the string to coordinates and fetches weather.
     */
    suspend fun getWeatherForLocation(locationQuery: String): Result<WeatherData>

    /**
     * Gets the latest cached weather, returning null if nothing is cached.
     */
    suspend fun getCachedWeather(): WeatherData?
}
