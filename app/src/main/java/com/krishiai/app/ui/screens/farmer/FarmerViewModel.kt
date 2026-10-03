package com.krishiai.app.ui.screens.farmer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishiai.app.data.model.*
import com.krishiai.app.domain.repository.MarketplaceRepository
import com.krishiai.app.domain.repository.WeatherRepository
import com.krishiai.app.ui.screens.weather.WeatherUiState
import com.krishiai.app.domain.usecase.CalculateAiPriceUseCase
import com.krishiai.app.domain.hardware.LocationTracker
import com.krishiai.app.domain.hardware.AddressResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.app.Application
import com.krishiai.app.utils.LocalStorageHelper

sealed interface UiState {
    object Idle : UiState
    object Loading : UiState
    object Success : UiState
    data class Error(val message: String) : UiState
}

@HiltViewModel
class FarmerViewModel @Inject constructor(
    private val marketplaceRepository: MarketplaceRepository,
    private val weatherRepository: WeatherRepository,
    private val calculateAiPriceUseCase: CalculateAiPriceUseCase,
    private val locationTracker: LocationTracker,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _myProducts = MutableStateFlow<List<Product>>(emptyList())
    val myProducts: StateFlow<List<Product>> = _myProducts.asStateFlow()

    private val _basePrices = MutableStateFlow<List<APMCPrice>>(emptyList())
    val basePrices: StateFlow<List<APMCPrice>> = _basePrices.asStateFlow()

    private val _isMarketPricesLoading = MutableStateFlow(true)
    val isMarketPricesLoading: StateFlow<Boolean> = _isMarketPricesLoading.asStateFlow()

    private val _weatherUiState = MutableStateFlow<WeatherUiState>(WeatherUiState.NoData)
    val weatherUiState: StateFlow<WeatherUiState> = _weatherUiState.asStateFlow()

    private val _calculatedAiPrice = MutableStateFlow<AiPriceExplanation?>(null)
    val calculatedAiPrice: StateFlow<AiPriceExplanation?> = _calculatedAiPrice.asStateFlow()

    private val _marketIntelligence = MutableStateFlow<List<MarketIntelligence>>(emptyList())
    val marketIntelligence: StateFlow<List<MarketIntelligence>> = _marketIntelligence.asStateFlow()

    private val _revenueStats = MutableStateFlow(RevenueStats())
    val revenueStats: StateFlow<RevenueStats> = _revenueStats.asStateFlow()

    fun loadDashboardData(farmerId: String, district: String) {
        viewModelScope.launch {
            // Load products
            marketplaceRepository.getProductsByFarmer(farmerId).collectLatest { products ->
                _myProducts.value = products
            }
        }
        
        viewModelScope.launch {
            _isMarketPricesLoading.value = true
            // Refresh first to fetch latest from network
            marketplaceRepository.refreshAPMCPrices(district)
            _isMarketPricesLoading.value = false
            
            // Load APMC prices for the district
            marketplaceRepository.getAPMCPrices(district).collectLatest { prices ->
                _basePrices.value = prices
            }
        }

        viewModelScope.launch {
            // Load Market Intelligence
            marketplaceRepository.getMarketIntelligence(district).collectLatest { intelligence ->
                _marketIntelligence.value = intelligence
            }
        }

        viewModelScope.launch {
            // Load Revenue Stats
            val stats = marketplaceRepository.getRevenueStats(farmerId)
            _revenueStats.value = stats
        }
    }

    fun loadWeatherForDashboard(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (_weatherUiState.value is WeatherUiState.Loading) return@launch
            _weatherUiState.value = WeatherUiState.Loading
            
            val result = weatherRepository.getCurrentWeather(forceRefresh)
            
            result.onSuccess { weatherData ->
                _weatherUiState.value = WeatherUiState.Success(
                    weatherData = weatherData,
                    isCached = weatherData.isCached
                )
            }.onFailure { exception ->
                _weatherUiState.value = WeatherUiState.Error(
                    message = exception.message ?: "Weather data unavailable.",
                    canRetry = true,
                    canSelectLocation = true
                )
            }
        }
    }
    
    fun setWeatherForLocation(locationQuery: String) {
        viewModelScope.launch {
            _weatherUiState.value = WeatherUiState.Loading
            
            val result = weatherRepository.getWeatherForLocation(locationQuery)
            
            result.onSuccess { weatherData ->
                _weatherUiState.value = WeatherUiState.Success(weatherData)
            }.onFailure { exception ->
                _weatherUiState.value = WeatherUiState.Error(
                    message = exception.message ?: "Weather data unavailable for this location.",
                    canRetry = true,
                    canSelectLocation = true
                )
            }
        }
    }
    
    fun clearWeatherError() {
        if (_weatherUiState.value is WeatherUiState.Error) {
            _weatherUiState.value = WeatherUiState.NoData
        }
    }

    private val _locationResult = MutableStateFlow<AddressResult?>(null)
    val locationResult: StateFlow<AddressResult?> = _locationResult.asStateFlow()

    fun fetchCurrentLocation() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val loc = locationTracker.getCurrentLocation()
            if (loc != null) {
                val address = locationTracker.getLocationAddress(loc)
                if (address != null) {
                    _locationResult.value = address
                }
            }
            _uiState.value = UiState.Idle
        }
    }

    fun calculateRecommendedPrice(
        cropName: String,
        district: String,
        quality: String,
        quantityKg: Double,
        harvestDate: Long
    ) {
        viewModelScope.launch {
            val matchedBase = _basePrices.value.firstOrNull { it.cropName.equals(cropName, ignoreCase = true) }
            val basePrice = matchedBase?.modalPrice ?: 20.0
            
            val weatherCondition = (_weatherUiState.value as? WeatherUiState.Success)?.weatherData?.condition ?: "clear"

            val explanation = calculateAiPriceUseCase(
                cropName = cropName,
                district = district,
                quality = quality,
                quantityKg = quantityKg,
                harvestDate = harvestDate,
                historicalApmcPrice = basePrice,
                weatherCondition = weatherCondition
            )
            _calculatedAiPrice.value = explanation
        }
    }

    fun uploadCropListing(
        farmerId: String,
        farmerName: String,
        farmerPhone: String,
        cropCategory: String,
        cropName: String,
        quantity: String,
        quality: String,
        harvestDate: Long,
        description: String,
        district: String,
        taluk: String,
        imageBytes: ByteArray?,
        customPrice: Double
    ) {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                var localImagePath = ""
                if (imageBytes != null) {
                    localImagePath = "file://" + LocalStorageHelper.saveImageToCache(application, imageBytes)
                }

                val aiExp = _calculatedAiPrice.value ?: AiPriceExplanation(
                    basePrice = customPrice,
                    finalPrice = customPrice,
                    confidencePercentage = 80,
                    recommendation = "Sell Today"
                )

                val newProduct = Product(
                    farmerId = farmerId,
                    farmerName = farmerName,
                    farmerPhone = farmerPhone,
                    cropCategory = cropCategory,
                    cropName = cropName,
                    quantity = quantity,
                    quality = quality,
                    harvestDate = harvestDate,
                    description = description,
                    district = district,
                    taluk = taluk,
                    imageUrl = localImagePath,
                    basePrice = aiExp.basePrice,
                    aiRecommendedPrice = aiExp.finalPrice,
                    finalPrice = customPrice,
                    status = ProductStatus.AVAILABLE.name,
                    aiExplanation = aiExp
                )

                marketplaceRepository.createProduct(newProduct)
                    .onSuccess {
                        _calculatedAiPrice.value = null
                        _uiState.value = UiState.Success
                    }
                    .onFailure {
                        _uiState.value = UiState.Error(it.localizedMessage ?: "Failed to list product")
                    }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.localizedMessage ?: "Listing creation failed")
            }
        }
    }

    fun updateStatus(productId: String, status: ProductStatus) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            marketplaceRepository.updateProductStatus(productId, status.name)
                .onSuccess { _uiState.value = UiState.Idle }
                .onFailure { _uiState.value = UiState.Error(it.localizedMessage ?: "Status update failed") }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            marketplaceRepository.deleteProduct(productId)
                .onSuccess { _uiState.value = UiState.Idle }
                .onFailure { _uiState.value = UiState.Error(it.localizedMessage ?: "Delete failed") }
        }
    }



    fun resetUiState() {
        _uiState.value = UiState.Idle
    }

    fun observeMyProducts(farmerId: String) {
        viewModelScope.launch {
            marketplaceRepository.getProductsByFarmer(farmerId).collectLatest { products ->
                _myProducts.value = products
            }
        }
    }

    fun refresh(farmerId: String, district: String) {
        loadDashboardData(farmerId, district)
        viewModelScope.launch {
            marketplaceRepository.refreshAPMCPrices(district)
            marketplaceRepository.refreshMarketIntelligence(district)
        }
    }
}

