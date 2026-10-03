package com.krishiai.app.ui.screens.buyer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishiai.app.data.model.*
import com.krishiai.app.domain.repository.MarketplaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.krishiai.app.core.ai.buyer.BuyerAIManager
import com.krishiai.app.core.ai.buyer.engines.PurchaseRecommendation
import com.krishiai.app.core.ai.buyer.engines.RecommendationInput
import com.krishiai.app.core.ai.buyer.AIResult

@HiltViewModel
class BuyerViewModel @Inject constructor(
    private val marketplaceRepository: MarketplaceRepository,
    private val buyerAIManager: BuyerAIManager
) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _favouriteProductIds = MutableStateFlow<List<String>>(emptyList())
    val favouriteProductIds: StateFlow<List<String>> = _favouriteProductIds.asStateFlow()

    private val _apmcPrices = MutableStateFlow<List<APMCPrice>>(emptyList())
    val apmcPrices: StateFlow<List<APMCPrice>> = _apmcPrices.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedDistrict = MutableStateFlow("")
    val selectedDistrict: StateFlow<String> = _selectedDistrict.asStateFlow()

    private val _maxPriceFilter = MutableStateFlow<Double?>(null)
    val maxPriceFilter: StateFlow<Double?> = _maxPriceFilter.asStateFlow()

    private val _showFavouritesOnly = MutableStateFlow(false)
    val showFavouritesOnly: StateFlow<Boolean> = _showFavouritesOnly.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    // AI States
    private val _bestDeal = MutableStateFlow<PurchaseRecommendation?>(null)
    val bestDeal: StateFlow<PurchaseRecommendation?> = _bestDeal.asStateFlow()

    private val _aiRecommendations = MutableStateFlow<List<PurchaseRecommendation>>(emptyList())
    val aiRecommendations: StateFlow<List<PurchaseRecommendation>> = _aiRecommendations.asStateFlow()

    fun loadDashboardData(district: String) {
        observeAllProducts()
        
        viewModelScope.launch {
            marketplaceRepository.getFavouriteProductIds().collectLatest { ids ->
                _favouriteProductIds.value = ids
            }
        }
        
        viewModelScope.launch {
            marketplaceRepository.getAPMCPrices(district).collectLatest { prices ->
                _apmcPrices.value = prices
            }
        }
    }

    private fun observeAllProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            marketplaceRepository.getProducts().collectLatest { allProducts ->
                _products.value = allProducts
                _isLoading.value = false
                
                // Trigger AI updates when products change
                runAIEngines(allProducts)
            }
        }
    }

    private fun runAIEngines(products: List<Product>) {
        viewModelScope.launch {
            // Map domain Product to DB Entity temporarily for AI Engines
            // Since AI engines were built for CropListingEntity
            val listings = products.map { 
                com.krishiai.app.data.local.entity.CropListingEntity(
                    listingId = it.id,
                    farmerId = it.farmerId,
                    cropName = it.cropName,
                    quantityKg = it.quantity.filter { char -> char.isDigit() || char == '.' }.toDoubleOrNull() ?: 0.0,
                    pricePerKg = it.finalPrice,
                    quality = it.quality,
                    harvestDate = System.currentTimeMillis(),
                    images = it.imageUrl,
                    isSold = it.status != "AVAILABLE",
                    createdAt = System.currentTimeMillis()
                )
            }
            
            val input = RecommendationInput(
                userId = "current_user",
                favoriteCrops = emptyList(), // Can be populated from favs
                userLat = 0.0,
                userLon = 0.0,
                activeCropListings = listings
            )

            // Run Best Deal Engine
            val bestDealResult = buyerAIManager.getBestDeal(input)
            if (bestDealResult is AIResult.Success) {
                _bestDeal.value = bestDealResult.data
            }

            // Run Recommendation Engine
            val recResult = buyerAIManager.getPersonalizedRecommendations(input)
            if (recResult is AIResult.Success) {
                _aiRecommendations.value = recResult.data
            }
        }
    }

    fun toggleFavourite(productId: String) {
        viewModelScope.launch {
            marketplaceRepository.toggleFavourite(productId)
        }
    }

    fun toggleFavouritesOnly() {
        _showFavouritesOnly.value = !_showFavouritesOnly.value
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = if (_selectedCategory.value == category) "" else category
    }

    fun selectDistrict(district: String) {
        _selectedDistrict.value = if (_selectedDistrict.value == district) "" else district
    }

    fun setMaxPrice(price: Double?) {
        _maxPriceFilter.value = price
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedCategory.value = ""
        _selectedDistrict.value = ""
        _maxPriceFilter.value = null
        _showFavouritesOnly.value = false
    }

    fun getSearchSuggestions(query: String): List<String> {
        if (query.isBlank()) return emptyList()
        return _products.value
            .filter { it.status == "AVAILABLE" }
            .map { it.cropName }
            .distinct()
            .filter { it.contains(query, ignoreCase = true) }
            .take(5)
    }

    fun getFilteredProducts(): List<Product> {
        return _products.value.filter { product ->
            val isAvailable = product.status == "AVAILABLE"
            
            val matchesSearch = product.cropName.contains(_searchQuery.value, ignoreCase = true) ||
                    product.description.contains(_searchQuery.value, ignoreCase = true)
            
            val matchesCategory = _selectedCategory.value.isEmpty() || _selectedCategory.value == "All" || 
                    product.cropCategory.equals(_selectedCategory.value, ignoreCase = true)
            
            val matchesDistrict = _selectedDistrict.value.isEmpty() || 
                    product.district.equals(_selectedDistrict.value, ignoreCase = true)
            
            val matchesPrice = _maxPriceFilter.value == null || 
                    product.finalPrice <= (_maxPriceFilter.value ?: Double.MAX_VALUE)
                    
            val matchesFavourite = !_showFavouritesOnly.value || _favouriteProductIds.value.contains(product.id)

            isAvailable && matchesSearch && matchesCategory && matchesDistrict && matchesPrice && matchesFavourite
        }
    }

    fun getProductDetails(productId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            marketplaceRepository.getProductById(productId)
                .onSuccess {
                    _selectedProduct.value = it
                    _isLoading.value = false
                }
                .onFailure {
                    _isLoading.value = false
                }
        }
    }

    fun clearSelectedProduct() {
        _selectedProduct.value = null
    }

    fun refresh(district: String) {
        loadDashboardData(district)
        viewModelScope.launch {
            marketplaceRepository.refreshAPMCPrices(district)
        }
    }
}

