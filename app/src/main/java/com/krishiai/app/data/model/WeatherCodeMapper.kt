package com.krishiai.app.data.model

object WeatherCodeMapper {

    data class WeatherCondition(
        val english: String,
        val kannada: String
    )

    fun getCondition(weatherCode: Int): WeatherCondition {
        return when (weatherCode) {
            0 -> WeatherCondition("Clear sky", "ಸ್ವಚ್ಛ ಆಕಾಶ")
            1 -> WeatherCondition("Mainly clear", "ಭಾಗಶಃ ಮೋಡ")
            2 -> WeatherCondition("Partly cloudy", "ಭಾಗಶಃ ಮೋಡ")
            3 -> WeatherCondition("Overcast", "ಮೋಡ ಕವಿದಿದೆ")
            45, 48 -> WeatherCondition("Fog", "ಮಂಜು")
            51, 53, 55 -> WeatherCondition("Drizzle", "ತುಂತುರು ಮಳೆ")
            56, 57 -> WeatherCondition("Freezing Drizzle", "ತುಂತುರು ಮಳೆ")
            61, 63, 65 -> WeatherCondition("Rain", "ಮಳೆ")
            66, 67 -> WeatherCondition("Freezing Rain", "ಮಳೆ")
            71, 73, 75, 77 -> WeatherCondition("Snow", "ಹಿಮ")
            80, 81, 82 -> WeatherCondition("Rain showers", "ಮಳೆ")
            85, 86 -> WeatherCondition("Snow showers", "ಹಿಮ")
            95 -> WeatherCondition("Thunderstorm", "ಗುಡುಗು ಸಹಿತ ಮಳೆ")
            96, 99 -> WeatherCondition("Thunderstorm with hail", "ಗುಡುಗು ಸಹಿತ ಮಳೆ")
            else -> WeatherCondition("Unknown", "ತಿಳಿದಿಲ್ಲ")
        }
    }
}
