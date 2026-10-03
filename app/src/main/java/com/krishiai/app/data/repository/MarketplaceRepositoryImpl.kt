package com.krishiai.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.krishiai.app.data.local.dao.*
import com.krishiai.app.data.local.entity.*
import com.krishiai.app.data.model.*
import com.krishiai.app.domain.repository.MarketplaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.*
import java.util.UUID
import javax.inject.Inject
import kotlin.math.roundToInt
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Data
import androidx.work.Constraints
import androidx.work.NetworkType
import com.krishiai.app.services.CropUploadWorker
import com.krishiai.app.data.model.SyncStatus

class MarketplaceRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val cropListingDao: CropListingDao,
    private val favouriteProductDao: FavouriteProductDao,
    private val notificationDao: NotificationDao,
    private val apmcPriceDao: APMCPriceDao,
    private val marketIntelligenceDao: MarketIntelligenceDao,
    private val transactionDao: TransactionDao,
    private val marketPriceRepository: com.krishiai.app.domain.repository.MarketPriceRepository,
    @ApplicationContext private val context: Context
) : MarketplaceRepository {

    // ---- Products ----
    override fun getProducts(): Flow<List<Product>> {
        // Listener is now handled by MarketplaceSyncManager
        return cropListingDao.getAllListings().map { it.map { entity -> entity.toProduct() } }
    }

    override fun getProductsByFarmer(farmerId: String): Flow<List<Product>> {
        return cropListingDao.getListingsByFarmer(farmerId).map { it.map { e -> e.toProduct() } }
    }

    override fun getProductsByDistrict(district: String): Flow<List<Product>> {
        return cropListingDao.getListingsByDistrict(district).map { it.map { e -> e.toProduct() } }
    }

    override fun getProductsByCategory(category: String): Flow<List<Product>> {
        return cropListingDao.getListingsByCategory(category).map { it.map { e -> e.toProduct() } }
    }

    override fun searchProducts(query: String): Flow<List<Product>> {
        return cropListingDao.searchListings(query).map { it.map { e -> e.toProduct() } }
    }

    override suspend fun getProductById(id: String): Result<Product> = runCatching {
        val local = cropListingDao.getListingById(id)
        if (local != null) return@runCatching local.toProduct()
        val doc = firestore.collection("products").document(id).get().await()
        doc.toProduct() ?: throw Exception("Product not found")
    }

    override suspend fun uploadProductImage(bytes: ByteArray): Result<String> = runCatching {
        val ref = storage.reference.child("products/${UUID.randomUUID()}.jpg")
        ref.putBytes(bytes).await()
        ref.downloadUrl.await().toString()
    }

    override suspend fun createProduct(product: Product): Result<Unit> = runCatching {
        // 1. Insert into local DB with PENDING sync status
        val entity = product.toEntity().copy(syncStatus = SyncStatus.PENDING.name)
        cropListingDao.insertListing(entity)

        // 2. Enqueue WorkManager job for background upload
        val workData = Data.Builder()
            .putString(CropUploadWorker.KEY_LISTING_ID, entity.listingId)
            .build()
            
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val uploadWork = OneTimeWorkRequestBuilder<CropUploadWorker>()
            .setInputData(workData)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(uploadWork)
    }

    override suspend fun updateProductStatus(productId: String, status: String): Result<Unit> = runCatching {
        firestore.collection("products").document(productId).update("status", status).await()
    }

    override suspend fun deleteProduct(productId: String): Result<Unit> = runCatching {
        firestore.collection("products").document(productId).delete().await()
        cropListingDao.deleteListingById(productId)
    }

    override suspend fun getBaseMarketPrices(): Result<List<MarketPrice>> = runCatching {
        getSimulatedMarketPrices()
    }

    override suspend fun initializeMarketPrices(): Result<Unit> = runCatching {
        val prices = getSimulatedMarketPrices()
        val entities = prices.map { mp ->
            APMCPriceEntity(
                marketName = "Simulated APMC",
                district = "",
                cropName = mp.cropName,
                cropCategory = mp.category,
                minPrice = mp.basePricePerKg * 0.85,
                maxPrice = mp.basePricePerKg * 1.15,
                modalPrice = mp.basePricePerKg,
                dateFetched = System.currentTimeMillis()
            )
        }
        apmcPriceDao.insertPrices(entities)
    }

    // ---- Favourites ----
    override fun getFavouriteProductIds(): Flow<List<String>> {
        return favouriteProductDao.getFavouriteIds()
    }

    override suspend fun toggleFavourite(productId: String): Result<Boolean> = runCatching {
        val isFav = favouriteProductDao.isFavourite(productId) > 0
        if (isFav) {
            favouriteProductDao.removeFavourite(productId)
            false
        } else {
            favouriteProductDao.addFavourite(FavouriteProductEntity(productId, System.currentTimeMillis()))
            true
        }
    }

    // ---- APMC Prices ----
    override fun getAPMCPrices(district: String): Flow<List<APMCPrice>> {
        val normalizedDistrict = when (district.trim().lowercase()) {
            "bagalkote" -> "Bagalkot"
            "bengaluru urban", "bangalore urban", "bangalore" -> "Bengaluru"
            "belagavi" -> "Belgaum"
            "chamarajanagara" -> "Chamarajanagar"
            "chikkamagaluru" -> "Chikmagalur"
            "mysuru" -> "Mysore"
            "tumakuru" -> "Tumkur"
            "shivamogga" -> "Shimoga"
            "kalaburagi" -> "Gulbarga"
            "vijayapura" -> "Bijapur"
            "ballari" -> "Bellary"
            else -> district
        }
        
        return marketPriceRepository.getPricesByLocation("Karnataka", normalizedDistrict).map { result ->
            result.getOrDefault(emptyList()).map { entity ->
                APMCPrice(
                    marketName = entity.market,
                    district = entity.district,
                    cropName = entity.commodity,
                    cropCategory = "", // API doesn't provide category
                    minPrice = entity.minPrice,
                    maxPrice = entity.maxPrice,
                    modalPrice = entity.modalPrice,
                    trend = com.krishiai.app.data.model.PriceMovement.STABLE,
                    trendPercent = 0.0,
                    dateFetched = entity.lastUpdated
                )
            }
        }
    }

    override suspend fun refreshAPMCPrices(district: String): Result<Unit> = runCatching {
        val normalizedDistrict = when (district.trim().lowercase()) {
            "bagalkote" -> "Bagalkot"
            "bengaluru urban", "bangalore urban", "bangalore" -> "Bengaluru"
            "belagavi" -> "Belgaum"
            "chamarajanagara" -> "Chamarajanagar"
            "chikkamagaluru" -> "Chikmagalur"
            "mysuru" -> "Mysore"
            "tumakuru" -> "Tumkur"
            "shivamogga" -> "Shimoga"
            "kalaburagi" -> "Gulbarga"
            "vijayapura" -> "Bijapur"
            "ballari" -> "Bellary"
            else -> district
        }
        
        // Let MarketPriceRepository handle the actual network call and fallback to MarketPriceDao.
        // We will keep APMCPriceDao in sync for existing consumers.
        val result = marketPriceRepository.refreshPricesByLocation("Karnataka", normalizedDistrict)
        if (result.isSuccess) {
            // No longer generating fake data here! Data is sourced from the API.
        } else {
            throw result.exceptionOrNull() ?: Exception("Failed to fetch market prices")
        }
    }

    // ---- Market Intelligence ----
    override fun getMarketIntelligence(district: String): Flow<List<MarketIntelligence>> {
        return marketIntelligenceDao.getIntelligenceByDistrict(district).map { it.map { e -> e.toModel() } }
    }

    override suspend fun refreshMarketIntelligence(district: String): Result<Unit> = runCatching {
        val intelligence = generateMarketIntelligence(district)
        marketIntelligenceDao.insertIntelligence(intelligence)
    }

    // ---- Notifications ----
    override fun getNotifications(): Flow<List<KrishiNotification>> {
        return notificationDao.getAllNotifications().map { it.map { e -> e.toModel() } }
    }

    override fun getUnreadNotificationCount(): Flow<Int> {
        return notificationDao.getUnreadCount()
    }

    override suspend fun markNotificationRead(id: String) {
        notificationDao.markAsRead(id)
    }

    override suspend fun markAllNotificationsRead() {
        notificationDao.markAllAsRead()
    }

    // ---- Transactions ----
    override fun getTransactions(userId: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByUser(userId).map { it.map { e -> e.toModel() } }
    }

    override suspend fun createTransaction(transaction: Transaction): Result<Unit> = runCatching {
        transactionDao.insertTransaction(transaction.toEntity())
        firestore.collection("transactions").document(transaction.transactionId).set(transaction).await()
    }

    override suspend fun getRevenueStats(farmerId: String): RevenueStats {
        val totalRevenue = transactionDao.getTotalRevenue(farmerId) ?: 0.0
        val totalDeals = transactionDao.getCompletedDealsCount(farmerId)
        val thisMonthRevenue = totalRevenue * 0.25 // Simulated
        return RevenueStats(
            totalRevenue = totalRevenue,
            thisMonthRevenue = thisMonthRevenue,
            lastMonthRevenue = thisMonthRevenue * 0.85,
            totalDeals = totalDeals,
            thisMonthDeals = (totalDeals * 0.3).roundToInt(),
            pendingAmount = totalRevenue * 0.05,
            monthlyTrend = listOf(0.7, 0.85, 0.9, 1.0, 0.95, 1.1).map { it * thisMonthRevenue }
        )
    }

    // ---- Private Helpers ----
    private fun generateMarketIntelligence(district: String): List<MarketIntelligenceEntity> {
        val now = System.currentTimeMillis()
        val crops = listOf(
            "Tomato" to 28.0,
            "Onion" to 22.0,
            "Chilli" to 95.0,
            "Potato" to 18.0,
            "Mango" to 65.0
        )
        val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

        return crops.mapIndexed { index, (crop, price) ->
            val demandScore = 45 + (index * 13) % 55
            val demandLevel = when {
                demandScore > 75 -> "VERY_HIGH"
                demandScore > 55 -> "HIGH"
                demandScore > 35 -> "MEDIUM"
                else -> "LOW"
            }
            val movement = when (index % 3) { 0 -> "UP"; 1 -> "DOWN"; else -> "STABLE" }
            val movementPct = (index % 4 + 1) * 2.5
            val weeklyTrend = (0..6).map { d ->
                price * (1 + (d - 3) * 0.02 + (index % 2 - 1) * 0.01)
            }
            val insight = when (demandLevel) {
                "VERY_HIGH" -> "Very high demand for $crop in $district. Best time to sell."
                "HIGH" -> "Strong demand for $crop. Prices trending ${movement.lowercase()}."
                "MEDIUM" -> "$crop market is stable. Good time for regular sales."
                else -> "Low demand for $crop currently. Consider waiting for better prices."
            }
            MarketIntelligenceEntity(
                id = "${crop}_${district}",
                cropName = crop,
                district = district,
                demandLevel = demandLevel,
                demandScore = demandScore,
                predictedPrice7Days = price * (1 + (if (movement == "UP") 0.05 else if (movement == "DOWN") -0.03 else 0.01)),
                bestSellingDay = days[(index * 2) % days.size],
                bestNearbyMarket = "$district APMC",
                expectedMovement = movement,
                expectedMovementPercent = movementPct,
                weeklyTrendJson = com.google.gson.Gson().toJson(weeklyTrend),
                insight = insight,
                updatedAt = now
            )
        }
    }

    private fun getSimulatedMarketPrices(): List<MarketPrice> {
        return listOf(
            MarketPrice("Tomato", 28.0, "Vegetables", "Summer"),
            MarketPrice("Onion", 22.0, "Vegetables", "Winter"),
            MarketPrice("Potato", 18.0, "Vegetables", "Winter"),
            MarketPrice("Chilli", 95.0, "Spices", "Summer"),
            MarketPrice("Brinjal", 15.0, "Vegetables", "Summer"),
            MarketPrice("Cabbage", 12.0, "Vegetables", "Winter"),
            MarketPrice("Cauliflower", 20.0, "Vegetables", "Winter"),
            MarketPrice("Mango", 65.0, "Fruits", "Summer"),
            MarketPrice("Banana", 25.0, "Fruits", "All Season"),
            MarketPrice("Pomegranate", 110.0, "Fruits", "Winter"),
            MarketPrice("Groundnut", 55.0, "Oilseeds", "Kharif"),
            MarketPrice("Maize", 22.0, "Grains", "Kharif"),
            MarketPrice("Rice", 35.0, "Grains", "Kharif"),
            MarketPrice("Wheat", 28.0, "Grains", "Rabi")
        )
    }

    // ---- Mapper Extensions ----
    private fun CropListingEntity.toProduct() = Product(
        id = listingId,
        farmerId = farmerId,
        farmerName = farmerName,
        farmerPhone = farmerPhone,
        cropCategory = cropCategory,
        cropName = cropName,
        quantity = "${quantityKg.toInt()} kg",
        quality = quality,
        harvestDate = harvestDate,
        description = description,
        district = district,
        taluk = taluk,
        imageUrl = images.split(",").firstOrNull() ?: "",
        basePrice = pricePerKg,
        aiRecommendedPrice = aiRecommendedPrice,
        finalPrice = pricePerKg,
        status = if (isSold) "SOLD" else "AVAILABLE",
        createdAt = createdAt,
        aiExplanation = AiPriceExplanation(),
        syncStatus = try { SyncStatus.valueOf(syncStatus) } catch (e: Exception) { SyncStatus.SYNCED },
        uploadProgress = uploadProgress,
        lastSyncTime = lastSyncTime,
        serverTimestamp = serverTimestamp,
        conflictVersion = conflictVersion
    )

    private fun Product.toEntity() = CropListingEntity(
        listingId = id.ifEmpty { UUID.randomUUID().toString() },
        farmerId = farmerId,
        farmerName = farmerName,
        farmerPhone = farmerPhone,
        cropName = cropName,
        cropCategory = cropCategory,
        quantityKg = quantity.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 0.0,
        pricePerKg = finalPrice,
        aiRecommendedPrice = aiRecommendedPrice,
        quality = quality,
        harvestDate = harvestDate,
        images = imageUrl,
        isSold = status == "SOLD",
        district = district,
        taluk = taluk,
        description = description,
        createdAt = createdAt,
        syncStatus = syncStatus.name,
        uploadProgress = uploadProgress,
        lastSyncTime = lastSyncTime,
        serverTimestamp = serverTimestamp,
        conflictVersion = conflictVersion
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toProduct(): Product? {
        return try {
            Product(
                id = id,
                farmerId = getString("farmerId") ?: "",
                farmerName = getString("farmerName") ?: "",
                farmerPhone = getString("farmerPhone") ?: "",
                cropCategory = getString("cropCategory") ?: "",
                cropName = getString("cropName") ?: "",
                quantity = (getDouble("quantityKg")?.toString() ?: getString("quantity")?.filter { it.isDigit() || it == '.' } ?: "") + " kg",
                quality = getString("quality") ?: "Good",
                harvestDate = getLong("harvestDate") ?: System.currentTimeMillis(),
                description = getString("description") ?: "",
                district = getString("district") ?: "",
                taluk = getString("taluk") ?: "",
                imageUrl = getString("imageUrl") ?: "",
                basePrice = getDouble("pricePerKg") ?: getDouble("basePrice") ?: 0.0,
                aiRecommendedPrice = getDouble("aiRecommendedPrice") ?: 0.0,
                finalPrice = getDouble("pricePerKg") ?: getDouble("finalPrice") ?: 0.0,
                status = getString("status") ?: "AVAILABLE",
                createdAt = getLong("createdAt") ?: System.currentTimeMillis()
            )
        } catch (e: Exception) { null }
    }

    private fun Product.toFirestoreMap(): Map<String, Any> = mapOf(
        "farmerId" to farmerId,
        "farmerName" to farmerName,
        "farmerPhone" to farmerPhone,
        "cropCategory" to cropCategory,
        "cropName" to cropName,
        "quantityKg" to (quantity.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 0.0),
        "quality" to quality,
        "harvestDate" to harvestDate,
        "description" to description,
        "district" to district,
        "taluk" to taluk,
        "imageUrl" to imageUrl,
        "pricePerKg" to finalPrice,
        "aiRecommendedPrice" to aiRecommendedPrice,
        "status" to status,
        "createdAt" to createdAt
    )

    private fun APMCPriceEntity.toAPMCPrice() = APMCPrice(
        marketName = marketName,
        district = district,
        cropName = cropName,
        cropCategory = cropCategory,
        minPrice = minPrice,
        maxPrice = maxPrice,
        modalPrice = modalPrice,
        trend = when (trend) { "UP" -> PriceMovement.UP; "DOWN" -> PriceMovement.DOWN; else -> PriceMovement.STABLE },
        trendPercent = trendPercent,
        dateFetched = dateFetched
    )

    private fun MarketIntelligenceEntity.toModel() = MarketIntelligence(
        cropName = cropName,
        district = district,
        demandLevel = when (demandLevel) {
            "VERY_HIGH" -> DemandLevel.VERY_HIGH
            "HIGH" -> DemandLevel.HIGH
            "LOW" -> DemandLevel.LOW
            else -> DemandLevel.MEDIUM
        },
        demandScore = demandScore,
        predictedPrice7Days = predictedPrice7Days,
        bestSellingDay = bestSellingDay,
        bestNearbyMarket = bestNearbyMarket,
        expectedMovement = when (expectedMovement) { "UP" -> PriceMovement.UP; "DOWN" -> PriceMovement.DOWN; else -> PriceMovement.STABLE },
        expectedMovementPercent = expectedMovementPercent,
        weeklyTrend = try {
            com.google.gson.Gson().fromJson(weeklyTrendJson, Array<Double>::class.java).toList()
        } catch (e: Exception) { emptyList() },
        insight = insight,
        updatedAt = updatedAt
    )

    private fun NotificationEntity.toModel() = KrishiNotification(
        id = id,
        title = title,
        message = message,
        type = try { NotificationType.valueOf(type) } catch (e: Exception) { NotificationType.AI_TIP },
        relatedId = relatedId,
        timestamp = timestamp,
        isRead = isRead
    )

    private fun TransactionEntity.toModel() = Transaction(
        transactionId = transactionId,
        productId = productId,
        farmerId = farmerId,
        buyerId = buyerId,
        cropName = cropName,
        quantityKg = quantityKg,
        pricePerKg = pricePerKg,
        totalAmount = totalAmount,
        status = try { TransactionStatus.valueOf(status) } catch (e: Exception) { TransactionStatus.PENDING },
        completedAt = completedAt,
        farmerRating = farmerRating,
        buyerRating = buyerRating,
        review = review
    )

    private fun Transaction.toEntity() = TransactionEntity(
        transactionId = transactionId,
        productId = productId,
        farmerId = farmerId,
        buyerId = buyerId,
        cropName = cropName,
        quantityKg = quantityKg,
        pricePerKg = pricePerKg,
        totalAmount = totalAmount,
        status = status.name,
        completedAt = completedAt,
        farmerRating = farmerRating,
        buyerRating = buyerRating,
        review = review
    )
}
