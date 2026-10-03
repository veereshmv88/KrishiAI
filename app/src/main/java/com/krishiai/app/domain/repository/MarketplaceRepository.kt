package com.krishiai.app.domain.repository

import com.krishiai.app.data.model.*
import kotlinx.coroutines.flow.Flow

interface MarketplaceRepository {
    // Products
    fun getProducts(): Flow<List<Product>>
    fun getProductsByFarmer(farmerId: String): Flow<List<Product>>
    fun getProductsByDistrict(district: String): Flow<List<Product>>
    fun getProductsByCategory(category: String): Flow<List<Product>>
    fun searchProducts(query: String): Flow<List<Product>>
    suspend fun getProductById(id: String): Result<Product>
    suspend fun uploadProductImage(bytes: ByteArray): Result<String>
    suspend fun createProduct(product: Product): Result<Unit>
    suspend fun updateProductStatus(productId: String, status: String): Result<Unit>
    suspend fun deleteProduct(productId: String): Result<Unit>
    suspend fun getBaseMarketPrices(): Result<List<MarketPrice>>
    suspend fun initializeMarketPrices(): Result<Unit>

    // Favourites
    fun getFavouriteProductIds(): Flow<List<String>>
    suspend fun toggleFavourite(productId: String): Result<Boolean>

    // APMC Prices
    fun getAPMCPrices(district: String): Flow<List<APMCPrice>>
    suspend fun refreshAPMCPrices(district: String): Result<Unit>

    // Market Intelligence
    fun getMarketIntelligence(district: String): Flow<List<MarketIntelligence>>
    suspend fun refreshMarketIntelligence(district: String): Result<Unit>

    // Notifications
    fun getNotifications(): Flow<List<KrishiNotification>>
    fun getUnreadNotificationCount(): Flow<Int>
    suspend fun markNotificationRead(id: String)
    suspend fun markAllNotificationsRead()

    // Transactions
    fun getTransactions(userId: String): Flow<List<Transaction>>
    suspend fun createTransaction(transaction: Transaction): Result<Unit>
    suspend fun getRevenueStats(farmerId: String): RevenueStats
}

