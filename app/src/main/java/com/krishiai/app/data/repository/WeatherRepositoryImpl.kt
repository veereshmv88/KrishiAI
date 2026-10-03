package com.krishiai.app.data.repository

import android.location.Location
import com.krishiai.app.data.local.prefs.WeatherLocalDataSource
import com.krishiai.app.data.model.DailyForecast
import com.krishiai.app.data.model.HourlyForecast
import com.krishiai.app.data.model.WeatherCodeMapper
import com.krishiai.app.data.model.WeatherData
import com.krishiai.app.data.remote.weather.OpenMeteoResponse
import com.krishiai.app.data.remote.weather.WeatherApiService
import com.krishiai.app.domain.hardware.LocationTracker
import com.krishiai.app.domain.repository.AuthRepository
import com.krishiai.app.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import kotlin.math.roundToInt

class WeatherRepositoryImpl @Inject constructor(
    private val weatherApiService: WeatherApiService,
    private val weatherLocalDataSource: WeatherLocalDataSource,
    private val locationTracker: LocationTracker,
    private val authRepository: AuthRepository
) : WeatherRepository {

    override suspend fun getCurrentWeather(forceRefresh: Boolean): Result<WeatherData> {
        if (!forceRefresh) {
            val cached = weatherLocalDataSource.getCachedWeather()
            if (cached != null) {
                // If it's less than 30 mins old, return it
                if (System.currentTimeMillis() - cached.lastUpdated < 30 * 60 * 1000) {
                    return Result.success(cached)
                }
            }
        }

        // Priority 1: Current GPS location
        val location = locationTracker.getCurrentLocation()
        if (location != null) {
            val result = fetchWeather(location.latitude, location.longitude, "Current Location")
            if (result.isSuccess) return result
        }

        // Priority 2 & 3: Registration location (District + Taluk)
        val user = authRepository.getCurrentUser().firstOrNull()
        if (user != null) {
            val locationQuery = buildString {
                if (user.taluk.isNotBlank()) append("${user.taluk}, ")
                if (user.district.isNotBlank()) append("${user.district}, ")
                append("Karnataka, India")
            }
            
            if (locationQuery.isNotBlank() && locationQuery != "Karnataka, India") {
                val geocodeResult = resolveLocation(locationQuery)
                if (geocodeResult != null) {
                    val result = fetchWeather(
                        geocodeResult.first, 
                        geocodeResult.second, 
                        if (user.taluk.isNotBlank()) user.taluk else user.district
                    )
                    if (result.isSuccess) return result
                }
            }
        }

        // Fallback: Cache if everything failed
        val cached = weatherLocalDataSource.getCachedWeather()
        if (cached != null) return Result.success(cached)

        return Result.failure(Exception("Could not determine location. Please select a location manually."))
    }

    override suspend fun getWeatherForLocation(locationQuery: String): Result<WeatherData> {
        val geocodeResult = resolveLocation("$locationQuery, Karnataka, India")
            ?: return Result.failure(Exception("Location not found"))

        return fetchWeather(geocodeResult.first, geocodeResult.second, locationQuery)
    }

    override suspend fun getCachedWeather(): WeatherData? {
        return weatherLocalDataSource.getCachedWeather()
    }

    private suspend fun resolveLocation(query: String): Pair<Double, Double>? {
        return try {
            val response = weatherApiService.searchLocation(name = query)
            val bestMatch = response.results?.firstOrNull { it.countryCode == "IN" }
            if (bestMatch != null) {
                Pair(bestMatch.latitude, bestMatch.longitude)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun fetchWeather(lat: Double, lon: Double, locName: String): Result<WeatherData> {
        return try {
            val response = weatherApiService.getForecast(latitude = lat, longitude = lon)
            val weatherData = mapToWeatherData(response, locName)
            weatherLocalDataSource.saveWeather(weatherData)
            Result.success(weatherData)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception("Weather data unavailable. Please check your connection."))
        }
    }

    private fun mapToWeatherData(dto: OpenMeteoResponse, locName: String): WeatherData {
        val current = dto.current ?: throw Exception("Missing current weather data")
        val daily = dto.daily
        val hourly = dto.hourly
        
        val weatherCode = current.weatherCode ?: 0
        val condition = WeatherCodeMapper.getCondition(weatherCode)

        val dailyForecast = mutableListOf<DailyForecast>()
        if (daily?.time != null) {
            for (i in 0 until minOf(7, daily.time.size)) {
                val dailyCode = daily.weatherCode?.getOrNull(i) ?: 0
                val dailyCond = WeatherCodeMapper.getCondition(dailyCode)
                
                dailyForecast.add(
                    DailyForecast(
                        dateStr = daily.time[i],
                        maxTemp = daily.temperatureMax?.getOrNull(i) ?: 0.0,
                        minTemp = daily.temperatureMin?.getOrNull(i) ?: 0.0,
                        rainChance = daily.precipitationProbabilityMax?.getOrNull(i) ?: 0,
                        weatherCode = dailyCode,
                        condition = dailyCond.english,
                        conditionKannada = dailyCond.kannada,
                        sunrise = daily.sunrise?.getOrNull(i) ?: "",
                        sunset = daily.sunset?.getOrNull(i) ?: ""
                    )
                )
            }
        }

        val hourlyForecast = mutableListOf<HourlyForecast>()
        if (hourly?.time != null) {
            for (i in 0 until minOf(24, hourly.time.size)) {
                val hourlyCode = hourly.weatherCode?.getOrNull(i) ?: 0
                val hourlyCond = WeatherCodeMapper.getCondition(hourlyCode)
                
                hourlyForecast.add(
                    HourlyForecast(
                        timeStr = hourly.time[i],
                        temperature = hourly.temperature?.getOrNull(i) ?: 0.0,
                        rainChance = hourly.precipitationProbability?.getOrNull(i) ?: 0,
                        weatherCode = hourlyCode,
                        condition = hourlyCond.english,
                        conditionKannada = hourlyCond.kannada
                    )
                )
            }
        }

        return WeatherData(
            locationName = locName,
            latitude = dto.latitude,
            longitude = dto.longitude,
            currentTemperature = current.temperature ?: 0.0,
            feelsLike = current.apparentTemperature ?: 0.0,
            humidity = current.humidity ?: 0,
            windSpeed = current.windSpeed ?: 0.0,
            windDirection = current.windDirection ?: 0,
            precipitation = current.precipitation ?: 0.0,
            rainChance = hourly?.precipitationProbability?.firstOrNull() ?: 0,
            condition = condition.english,
            conditionKannada = condition.kannada,
            weatherCode = weatherCode,
            todayHigh = daily?.temperatureMax?.firstOrNull() ?: 0.0,
            todayLow = daily?.temperatureMin?.firstOrNull() ?: 0.0,
            hourlyForecast = hourlyForecast,
            dailyForecast = dailyForecast,
            lastUpdated = System.currentTimeMillis()
        )
    }
}
