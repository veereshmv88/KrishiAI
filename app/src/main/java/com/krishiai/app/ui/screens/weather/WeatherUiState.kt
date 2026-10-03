package com.krishiai.app.ui.screens.weather

import com.krishiai.app.data.model.WeatherData

sealed interface WeatherUiState {
    object Loading : WeatherUiState
    
    data class Success(
        val weatherData: WeatherData,
        val isCached: Boolean = false
    ) : WeatherUiState
    
    data class Error(
        val message: String,
        val canRetry: Boolean = true,
        val canSelectLocation: Boolean = true
    ) : WeatherUiState
    
    object NoData : WeatherUiState
}
