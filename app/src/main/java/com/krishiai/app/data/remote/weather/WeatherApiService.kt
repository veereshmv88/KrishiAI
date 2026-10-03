package com.krishiai.app.data.remote.weather

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("https://api.open-meteo.com/v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,rain,weather_code,wind_speed_10m,wind_direction_10m",
        @Query("hourly") hourly: String = "temperature_2m,relative_humidity_2m,precipitation_probability,precipitation,rain,weather_code,wind_speed_10m",
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min,precipitation_sum,rain_sum,precipitation_probability_max,weather_code,sunrise,sunset",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = 7
    ): OpenMeteoResponse

    @GET("https://geocoding-api.open-meteo.com/v1/search")
    suspend fun searchLocation(
        @Query("name") name: String,
        @Query("count") count: Int = 10,
        @Query("language") language: String = "en",
        @Query("format") format: String = "json"
    ): GeocodingResponse
}
