package com.krishiai.app.data.model

data class WeatherData(
    val locationName: String,
    val state: String = "Karnataka",
    val district: String = "",
    val taluk: String = "",
    val latitude: Double,
    val longitude: Double,
    
    val currentTemperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val windSpeed: Double,
    val windDirection: Int,
    val precipitation: Double,
    val rainChance: Int,
    
    val condition: String, // E.g., "Partly Cloudy"
    val conditionKannada: String, // E.g., "ಭಾಗಶಃ ಮೋಡ"
    val weatherCode: Int,
    
    val todayHigh: Double,
    val todayLow: Double,
    
    val hourlyForecast: List<HourlyForecast> = emptyList(),
    val dailyForecast: List<DailyForecast> = emptyList(),
    
    val lastUpdated: Long,
    val source: String = "Open-Meteo",
    val isCached: Boolean = false
)

data class HourlyForecast(
    val timeStr: String,
    val temperature: Double,
    val rainChance: Int,
    val weatherCode: Int,
    val condition: String,
    val conditionKannada: String
)

data class DailyForecast(
    val dateStr: String,
    val maxTemp: Double,
    val minTemp: Double,
    val rainChance: Int,
    val weatherCode: Int,
    val condition: String,
    val conditionKannada: String,
    val sunrise: String = "",
    val sunset: String = ""
)
