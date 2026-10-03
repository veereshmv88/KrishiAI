package com.krishiai.app.buyer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.krishiai.app.buyer.ai.AIRecommendation
import com.krishiai.app.buyer.ai.BuyerAIOrchestrator
import com.krishiai.app.buyer.ai.BuyingDecision
import com.krishiai.app.buyer.ai.MarketInsight
import com.krishiai.app.buyer.ai.PricePrediction
import com.krishiai.app.buyer.data.model.*
import com.krishiai.app.buyer.domain.repository.*
import com.krishiai.app.data.local.dao.APMCPriceDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

data class BuyerUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val allProducts: List<BuyerProduct> = emptyList(),
    val searchResults: List<BuyerProduct> = emptyList(),
    val cartSummary: CartSummary = CartSummary(),
    val cartCount: Int = 0,
    val orders: List<BuyerOrder> = emptyList(),
    val wishlistItems: List<WishlistItem> = emptyList(),
    val wishlistCount: Int = 0,
    val recommendations: List<AIRecommendation> = emptyList(),
    val bestDeal: AIRecommendation? = null,
    val marketInsights: List<MarketInsight> = emptyList(),
    val pricePredictions: Map<String, PricePrediction> = emptyMap(),
    val buyingDecision: BuyingDecision? = null,
    val nearbySellersList: List<NearbySeller> = emptyList(),
    val searchQuery: String = "",
    val searchFilters: BuyerSearchFilters = BuyerSearchFilters(),
    val selectedProduct: BuyerProduct? = null,
    val checkoutResult: CheckoutResult? = null,
    val selectedOrderTab: Int = 0,
    val notifications: List<BuyerNotification> = emptyList(),
    val unreadNotificationCount: Int = 0
)

@HiltViewModel
class BuyerMainViewModel @Inject constructor(
    private val productRepository: BuyerProductRepository,
    private val cartRepository: BuyerCartRepository,
    private val orderRepository: BuyerOrderRepository,
    private val wishlistRepository: BuyerWishlistRepository,
    private val searchRepository: BuyerSearchRepository,
    private val marketRepository: BuyerMarketRepository,
    private val notificationRepository: BuyerNotificationRepository,
    private val aiOrchestrator: BuyerAIOrchestrator,
    private val apmcPriceDao: APMCPriceDao,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(BuyerUiState())
    val uiState: StateFlow<BuyerUiState> = _uiState.asStateFlow()

    val currentUserId: String get() = auth.currentUser?.uid ?: ""

    init {
        loadAllData()
    }

    private fun loadAllData() {
        observeProducts()
        observeCart()
        observeOrders()
        observeWishlist()
        observeNotifications()
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            combine(
                notificationRepository.getNotifications(),
                notificationRepository.getUnreadCount()
            ) { notifications, count -> notifications to count }.collect { (notifications, count) ->
                _uiState.update { it.copy(notifications = notifications, unreadNotificationCount = count) }
            }
        }
    }

    private fun observeProducts() {
        viewModelScope.launch {
            productRepository.getAllProducts()
                .distinctUntilChanged()
                .debounce(500L)
                .collect { products ->
                    _uiState.update { it.copy(allProducts = products) }
                    runAIAnalysis(products)
                }
        }
    }

    private fun observeCart() {
        viewModelScope.launch {
            cartRepository.getCartSummary()
                .distinctUntilChanged()
                .collect { summary ->
                    _uiState.update { it.copy(cartSummary = summary, cartCount = summary.itemCount) }
                }
        }
    }

    private fun observeOrders() {
        viewModelScope.launch {
            val uid = currentUserId
            if (uid.isNotEmpty()) {
                orderRepository.getOrdersByBuyer(uid)
                    .distinctUntilChanged()
                    .collect { orders ->
                        _uiState.update { it.copy(orders = orders) }
                    }
            }
        }
    }

    private fun observeWishlist() {
        viewModelScope.launch {
            combine(
                wishlistRepository.getWishlistItems(),
                wishlistRepository.getWishlistCount()
            ) { items, count -> items to count }
                .distinctUntilChanged()
                .collect { (items, count) ->
                    _uiState.update { it.copy(wishlistItems = items, wishlistCount = count) }
                }
        }
    }

    private fun runAIAnalysis(products: List<BuyerProduct>) {
        viewModelScope.launch(Dispatchers.Default) {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val apmcPrices = apmcPriceDao.getAllPrices().first()

                // Best Deal
                val bestDeal = aiOrchestrator.getBestDeal(products).dataOrNull
                _uiState.update { it.copy(bestDeal = bestDeal) }

                // Recommendations
                val recs = aiOrchestrator.getRecommendations(
                    userId = currentUserId,
                    allProducts = products,
                    apmcPrices = apmcPrices
                ).dataOrNull ?: emptyList()
                _uiState.update { it.copy(recommendations = recs) }

                // Market Insights for top crops
                val topCrops = products.map { it.cropName }.distinct().take(5)
                val insights = topCrops.mapNotNull { crop ->
                    aiOrchestrator.getMarketInsight(crop, "", apmcPrices).dataOrNull
                }
                _uiState.update { it.copy(marketInsights = insights) }

            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // ─── Cart ───────────────────────────────────
    fun addToCart(product: BuyerProduct, quantityKg: Double = 1.0) {
        viewModelScope.launch {
            cartRepository.addToCart(CartItem(
                productId = product.productId,
                farmerId = product.farmerId,
                farmerName = product.farmerName,
                cropName = product.cropName,
                cropCategory = product.cropCategory,
                quality = product.quality,
                grade = product.grade,
                imageUrl = product.imageUrl,
                pricePerKg = product.pricePerKg,
                quantityKg = quantityKg,
                totalAmount = product.pricePerKg * quantityKg,
                district = product.district
            ))
        }
    }

    fun updateCartQuantity(cartItemId: String, qty: Double) {
        viewModelScope.launch { cartRepository.updateCartQuantity(cartItemId, qty) }
    }

    fun removeFromCart(cartItemId: String) {
        viewModelScope.launch { cartRepository.removeFromCart(cartItemId) }
    }

    // ─── Wishlist ───────────────────────────────
    fun toggleWishlist(product: BuyerProduct) {
        viewModelScope.launch {
            val inWishlist = wishlistRepository.isInWishlist(product.productId)
            if (inWishlist) wishlistRepository.removeFromWishlist(product.productId)
            else wishlistRepository.addToWishlist(product)
        }
    }

    // ─── Search ────────────────────────────────
    fun search(query: String, filters: BuyerSearchFilters = _uiState.value.searchFilters) {
        _uiState.update { it.copy(searchQuery = query, searchFilters = filters) }
        viewModelScope.launch {
            productRepository.searchProducts(query, filters).collect { results ->
                _uiState.update { it.copy(searchResults = results) }
            }
            searchRepository.saveSearch(query, "TEXT", _uiState.value.searchResults.size)
        }
    }

    fun updateFilters(filters: BuyerSearchFilters) {
        _uiState.update { it.copy(searchFilters = filters) }
        if (_uiState.value.searchQuery.isNotEmpty()) {
            search(_uiState.value.searchQuery, filters)
        }
    }

    // ─── Checkout ──────────────────────────────
    fun placeOrder(deliveryAddress: DeliveryAddress, paymentMethod: PaymentMethod, couponCode: String = "") {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val cartItems = _uiState.value.cartSummary.items
            if (cartItems.isEmpty()) {
                _uiState.update { it.copy(isLoading = false, error = "Cart is empty") }
                return@launch
            }
            val result = orderRepository.placeOrder(CheckoutRequest(
                buyerId = currentUserId,
                cartItems = cartItems,
                deliveryAddress = deliveryAddress,
                paymentMethod = paymentMethod,
                couponCode = couponCode
            ))
            result.fold(
                onSuccess = { checkoutResult ->
                    _uiState.update { it.copy(isLoading = false, checkoutResult = checkoutResult) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    // ─── Price Prediction ──────────────────────
    fun loadPricePrediction(cropName: String, district: String = "") {
        viewModelScope.launch {
            val prices = apmcPriceDao.getAllPrices().first()
            val prediction = aiOrchestrator.predictPrice(cropName, district, prices).dataOrNull
            if (prediction != null) {
                _uiState.update { it.copy(pricePredictions = it.pricePredictions + (cropName to prediction)) }
            }
        }
    }

    // ─── Nearby Sellers ────────────────────────
    fun loadNearbySellers(lat: Double, lon: Double, radiusKm: Double = 50.0) {
        viewModelScope.launch {
            marketRepository.getNearbySellersByLocation(lat, lon, radiusKm).fold(
                onSuccess = { sellers -> _uiState.update { it.copy(nearbySellersList = sellers) } },
                onFailure = { e -> _uiState.update { it.copy(error = e.message) } }
            )
        }
    }

    // ─── Buying Decision ───────────────────────
    fun analyzeBuyingDecision(cropName: String, currentPrice: Double) {
        viewModelScope.launch {
            val prediction = _uiState.value.pricePredictions[cropName]
            val products = _uiState.value.allProducts
            val decision = aiOrchestrator.getBuyingDecision(cropName, currentPrice, prediction, products).dataOrNull
            _uiState.update { it.copy(buyingDecision = decision) }
        }
    }

    fun selectProduct(product: BuyerProduct) {
        _uiState.update { it.copy(selectedProduct = product) }
        loadPricePrediction(product.cropName, product.district)
        analyzeBuyingDecision(product.cropName, product.pricePerKg)
    }

    fun setOrderTab(tab: Int) { _uiState.update { it.copy(selectedOrderTab = tab) } }
    // ─── Notifications ───────────────────────────
    fun markNotificationAsRead(id: String) {
        viewModelScope.launch { notificationRepository.markAsRead(id) }
    }
    
    fun markAllNotificationsAsRead() {
        viewModelScope.launch { notificationRepository.markAllAsRead() }
    }

    fun clearError() { _uiState.update { it.copy(error = null) } }
    fun clearCheckoutResult() { _uiState.update { it.copy(checkoutResult = null) } }
}
