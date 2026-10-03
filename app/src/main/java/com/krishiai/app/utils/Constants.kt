package com.krishiai.app.utils

object Constants {
    // OpenWeather Configuration
    // Replace with a valid API key (e.g. from https://openweathermap.org/api)
    // If kept blank or default, the app automatically switches to Simulated Weather fallbacks

    const val WEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5/"
    
    // Whisper Voice AI Configuration
    const val WHISPER_BASE_URL = "http://10.0.2.2:8000/" // Use 10.0.2.2 for Android Emulator, or PC LAN IP for physical device

    // Firebase Collections
    const val COLLECTION_USERS = "users"
    const val COLLECTION_PRODUCTS = "products"
    const val COLLECTION_MARKET_PRICES = "market_prices"

    // SharedPreferences Keys
    const val PREFS_NAME = "krishiai_prefs"
    const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
}
