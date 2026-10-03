package com.krishiai.app.ui.screens.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishiai.app.core.location.LocationDetails
import com.krishiai.app.core.location.LocationHelper
import com.krishiai.app.data.local.entity.MarketPriceEntity
import com.krishiai.app.domain.ai.MarketRecommendation
import com.krishiai.app.domain.ai.MarketRecommendationEngine
import com.krishiai.app.domain.usecase.GetMarketPricesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MarketUiState {
    object Loading : MarketUiState()
    data class Success(
        val prices: List<MarketPriceEntity>,
        val location: LocationDetails?,
        val recommendation: MarketRecommendation?
    ) : MarketUiState()
    data class Error(val message: String) : MarketUiState()
    object Empty : MarketUiState()
}

@HiltViewModel
class LiveMarketPriceViewModel @Inject constructor(
    private val getMarketPricesUseCase: GetMarketPricesUseCase,
    private val locationHelper: LocationHelper,
    private val recommendationEngine: MarketRecommendationEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow<MarketUiState>(MarketUiState.Loading)
    val uiState: StateFlow<MarketUiState> = _uiState.asStateFlow()

    private var currentLocation: LocationDetails? = null

    init {
        fetchLocationAndPrices()
    }

    fun fetchPricesForDistrict(districtName: String) {
        viewModelScope.launch {
            _uiState.value = MarketUiState.Loading
            
            val state = "Karnataka" // Hardcoded for now
            currentLocation = LocationDetails(state = state, district = districtName)

            getMarketPricesUseCase.refresh(state, districtName)

            getMarketPricesUseCase(state, districtName).collect { result ->
                result.fold(
                    onSuccess = { prices ->
                        if (prices.isEmpty()) {
                            _uiState.value = MarketUiState.Empty
                        } else {
                            val topCommodity = prices.groupBy { it.commodity }.maxByOrNull { it.value.size }?.key ?: ""
                            val recommendation = recommendationEngine.generateRecommendation(prices, topCommodity)
                            
                            _uiState.value = MarketUiState.Success(
                                prices = prices,
                                location = currentLocation,
                                recommendation = recommendation
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = MarketUiState.Error(error.message ?: "Failed to fetch market prices.")
                    }
                )
            }
        }
    }

    fun fetchLocationAndPrices() {
        viewModelScope.launch {
            _uiState.value = MarketUiState.Loading
            
            // 1. Get Location
            currentLocation = locationHelper.getCurrentLocation()
            val state = currentLocation?.state ?: "Karnataka" // Fallback
            val district = currentLocation?.district ?: "Bengaluru"

            // 2. Refresh Prices from API (Background)
            getMarketPricesUseCase.refresh(state, district)

            // 3. Observe local DB flow
            getMarketPricesUseCase(state, district).collect { result ->
                result.fold(
                    onSuccess = { prices ->
                        if (prices.isEmpty()) {
                            _uiState.value = MarketUiState.Empty
                        } else {
                            val topCommodity = prices.groupBy { it.commodity }.maxByOrNull { it.value.size }?.key ?: ""
                            val recommendation = recommendationEngine.generateRecommendation(prices, topCommodity)
                            
                            _uiState.value = MarketUiState.Success(
                                prices = prices,
                                location = currentLocation,
                                recommendation = recommendation
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = MarketUiState.Error(error.message ?: "Failed to fetch market prices.")
                    }
                )
            }
        }
    }
}
