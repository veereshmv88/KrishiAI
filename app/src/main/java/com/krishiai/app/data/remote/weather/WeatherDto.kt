package com.krishiai.app.data.remote.weather

import com.google.gson.annotations.SerializedName

data class OpenMeteoResponse(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("timezone") val timezone: String,
    @SerializedName("current") val current: CurrentWeatherDto?,
    @SerializedName("hourly") val hourly: HourlyWeatherDto?,
    @SerializedName("daily") val daily: DailyWeatherDto?
)

data class CurrentWeatherDto(
    @SerializedName("temperature_2m") val temperature: Double?,
    @SerializedName("relative_humidity_2m") val humidity: Int?,
    @SerializedName("apparent_temperature") val apparentTemperature: Double?,
    @SerializedName("precipitation") val precipitation: Double?,
    @SerializedName("rain") val rain: Double?,
    @SerializedName("weather_code") val weatherCode: Int?,
    @SerializedName("wind_speed_10m") val windSpeed: Double?,
    @SerializedName("wind_direction_10m") val windDirection: Int?
)

data class HourlyWeatherDto(
    @SerializedName("time") val time: List<String>?,
    @SerializedName("temperature_2m") val temperature: List<Double>?,
    @SerializedName("relative_humidity_2m") val humidity: List<Int>?,
    @SerializedName("precipitation_probability") val precipitationProbability: List<Int>?,
    @SerializedName("precipitation") val precipitation: List<Double>?,
    @SerializedName("rain") val rain: List<Double>?,
    @SerializedName("weather_code") val weatherCode: List<Int>?,
    @SerializedName("wind_speed_10m") val windSpeed: List<Double>?
)

data class DailyWeatherDto(
    @SerializedName("time") val time: List<String>?,
    @SerializedName("temperature_2m_max") val temperatureMax: List<Double>?,
    @SerializedName("temperature_2m_min") val temperatureMin: List<Double>?,
    @SerializedName("precipitation_sum") val precipitationSum: List<Double>?,
    @SerializedName("rain_sum") val rainSum: List<Double>?,
    @SerializedName("precipitation_probability_max") val precipitationProbabilityMax: List<Int>?,
    @SerializedName("weather_code") val weatherCode: List<Int>?,
    @SerializedName("sunrise") val sunrise: List<String>?,
    @SerializedName("sunset") val sunset: List<String>?
)

data class GeocodingResponse(
    @SerializedName("results") val results: List<GeocodingResultDto>?
)

data class GeocodingResultDto(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("country_code") val countryCode: String?,
    @SerializedName("admin1") val admin1: String?, // State
    @SerializedName("admin2") val admin2: String?  // District
)
