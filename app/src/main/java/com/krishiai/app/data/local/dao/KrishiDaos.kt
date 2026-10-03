package com.krishiai.app.data.local.dao

import androidx.room.*
import com.krishiai.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE uid = :uid")
    fun getUserFlow(uid: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE uid = :uid")
    suspend fun getUser(uid: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)
}

@Dao
interface CropListingDao {
    @Query("SELECT * FROM crop_listings WHERE isSold = 0 ORDER BY createdAt DESC")
    fun getAllListings(): Flow<List<CropListingEntity>>

    @Query("SELECT * FROM crop_listings WHERE farmerId = :farmerId ORDER BY createdAt DESC")
    fun getListingsByFarmer(farmerId: String): Flow<List<CropListingEntity>>

    @Query("SELECT * FROM crop_listings WHERE district = :district AND isSold = 0 ORDER BY createdAt DESC")
    fun getListingsByDistrict(district: String): Flow<List<CropListingEntity>>

    @Query("SELECT * FROM crop_listings WHERE cropCategory = :category AND isSold = 0 ORDER BY createdAt DESC")
    fun getListingsByCategory(category: String): Flow<List<CropListingEntity>>

    @Query("SELECT * FROM crop_listings WHERE cropName LIKE '%' || :query || '%' AND isSold = 0 ORDER BY createdAt DESC")
    fun searchListings(query: String): Flow<List<CropListingEntity>>

    @Query("SELECT * FROM crop_listings WHERE listingId = :id")
    suspend fun getListingById(id: String): CropListingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListings(listings: List<CropListingEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(listing: CropListingEntity)

    @Delete
    suspend fun deleteListing(listing: CropListingEntity)

    @Query("DELETE FROM crop_listings WHERE listingId = :id")
    suspend fun deleteListingById(id: String)

    @Query("UPDATE crop_listings SET syncStatus = :status WHERE listingId = :id")
    suspend fun updateSyncStatus(id: String, status: String)
}

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather_cache WHERE locationKey = :key")
    fun getWeather(key: String): Flow<WeatherCacheEntity?>

    @Query("SELECT * FROM weather_cache WHERE locationKey = :key")
    suspend fun getWeatherOnce(key: String): WeatherCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherCacheEntity)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM notifications WHERE timestamp < :olderThan")
    suspend fun deleteOldNotifications(olderThan: Long)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: String)
}

@Dao
interface FavouriteProductDao {
    @Query("SELECT * FROM favourite_products ORDER BY addedAt DESC")
    fun getAllFavourites(): Flow<List<FavouriteProductEntity>>

    @Query("SELECT productId FROM favourite_products")
    fun getFavouriteIds(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM favourite_products WHERE productId = :productId")
    suspend fun isFavourite(productId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavourite(favourite: FavouriteProductEntity)

    @Query("DELETE FROM favourite_products WHERE productId = :productId")
    suspend fun removeFavourite(productId: String)
}

@Dao
interface APMCPriceDao {
    @Query("SELECT * FROM apmc_prices ORDER BY dateFetched DESC")
    fun getAllPrices(): Flow<List<APMCPriceEntity>>

    @Query("SELECT * FROM apmc_prices WHERE district = :district ORDER BY dateFetched DESC")
    fun getPricesByDistrict(district: String): Flow<List<APMCPriceEntity>>

    @Query("SELECT * FROM apmc_prices WHERE cropName = :cropName ORDER BY dateFetched DESC LIMIT 10")
    suspend fun getPricesForCrop(cropName: String): List<APMCPriceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrices(prices: List<APMCPriceEntity>)

    @Query("DELETE FROM apmc_prices WHERE dateFetched < :olderThan")
    suspend fun deleteOldPrices(olderThan: Long)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE farmerId = :userId OR buyerId = :userId ORDER BY completedAt DESC")
    fun getTransactionsByUser(userId: String): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(totalAmount) FROM transactions WHERE farmerId = :farmerId AND status = 'COMPLETED'")
    suspend fun getTotalRevenue(farmerId: String): Double?

    @Query("SELECT COUNT(*) FROM transactions WHERE (farmerId = :userId OR buyerId = :userId) AND status = 'COMPLETED'")
    suspend fun getCompletedDealsCount(userId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)
}

@Dao
interface MarketIntelligenceDao {
    @Query("SELECT * FROM market_intelligence ORDER BY demandScore DESC")
    fun getAllIntelligence(): Flow<List<MarketIntelligenceEntity>>

    @Query("SELECT * FROM market_intelligence WHERE district = :district ORDER BY demandScore DESC")
    fun getIntelligenceByDistrict(district: String): Flow<List<MarketIntelligenceEntity>>

    @Query("SELECT * FROM market_intelligence WHERE cropName = :cropName AND district = :district LIMIT 1")
    suspend fun getIntelligenceForCrop(cropName: String, district: String): MarketIntelligenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIntelligence(intelligence: List<MarketIntelligenceEntity>)

    @Query("DELETE FROM market_intelligence WHERE updatedAt < :olderThan")
    suspend fun deleteOldIntelligence(olderThan: Long)
}

@Dao
interface DiseaseHistoryDao {
    @Query("SELECT * FROM disease_history ORDER BY detectedAt DESC LIMIT 20")
    fun getHistory(): Flow<List<DiseaseHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: DiseaseHistoryEntity)

    @Query("DELETE FROM disease_history WHERE detectedAt < :olderThan")
    suspend fun deleteOldHistory(olderThan: Long)
}

@Dao
interface OcrHistoryDao {
    @Query("SELECT * FROM ocr_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<OcrHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: OcrHistoryEntity)

    @Query("DELETE FROM ocr_history WHERE id = :id")
    suspend fun deleteHistoryById(id: String)
    
    @Query("DELETE FROM ocr_history WHERE timestamp < :olderThan")
    suspend fun deleteOldHistory(olderThan: Long)
}

@Dao
interface ChatHistoryDao {
    @Query("SELECT * FROM chat_history ORDER BY timestamp ASC")
    fun getChatHistory(): Flow<List<ChatHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatHistoryEntity>)

    @Query("DELETE FROM chat_history")
    suspend fun clearHistory()
}
