package com.krishiai.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.krishiai.app.buyer.data.dao.*
import com.krishiai.app.buyer.data.entity.*
import com.krishiai.app.data.local.dao.*
import com.krishiai.app.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        FarmerProfileEntity::class,
        BuyerProfileEntity::class,
        CropListingEntity::class,
        WeatherCacheEntity::class,
        APMCPriceEntity::class,
        NotificationEntity::class,
        FavouriteCropEntity::class,
        FavouriteProductEntity::class,
        TransactionEntity::class,
        MarketIntelligenceEntity::class,
        DiseaseHistoryEntity::class,
        MarketPriceEntity::class,
        OcrHistoryEntity::class,
        LocationHistoryEntity::class,
        // Buyer module entities
        CartItemEntity::class,
        BuyerOrderEntity::class,
        WishlistItemEntity::class,
        BuyerSearchHistoryEntity::class,
        PriceAlertEntity::class,
        ComparedProductEntity::class,
        ChatHistoryEntity::class
    ],
    version = 9,
    exportSchema = false
)
abstract class KrishiDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun cropListingDao(): CropListingDao
    abstract fun weatherDao(): WeatherDao
    abstract fun notificationDao(): NotificationDao
    abstract fun favouriteProductDao(): FavouriteProductDao
    abstract fun apmcPriceDao(): APMCPriceDao
    abstract fun transactionDao(): TransactionDao
    abstract fun marketIntelligenceDao(): MarketIntelligenceDao
    abstract fun diseaseHistoryDao(): DiseaseHistoryDao
    abstract fun marketPriceDao(): MarketPriceDao
    abstract fun ocrHistoryDao(): OcrHistoryDao
    abstract fun locationDao(): LocationDao
    // Buyer Module DAOs
    abstract fun cartItemDao(): CartItemDao
    abstract fun buyerOrderDao(): BuyerOrderDao
    abstract fun wishlistDao(): WishlistDao
    abstract fun buyerSearchHistoryDao(): BuyerSearchHistoryDao
    abstract fun priceAlertDao(): PriceAlertDao
    abstract fun comparedProductDao(): ComparedProductDao
    abstract fun chatHistoryDao(): ChatHistoryDao

    companion object {
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS chat_history (
                        messageId TEXT PRIMARY KEY NOT NULL,
                        role TEXT NOT NULL,
                        content TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        language TEXT NOT NULL DEFAULT 'en',
                        isToolCall INTEGER NOT NULL DEFAULT 0,
                        toolName TEXT,
                        toolResult TEXT
                    )
                """)
            }
        }
        
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Buyer Cart Items
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS buyer_cart_items (
                        cartItemId TEXT PRIMARY KEY NOT NULL,
                        productId TEXT NOT NULL,
                        farmerId TEXT NOT NULL,
                        farmerName TEXT NOT NULL DEFAULT '',
                        cropName TEXT NOT NULL,
                        cropCategory TEXT NOT NULL DEFAULT '',
                        quality TEXT NOT NULL DEFAULT 'Good',
                        grade TEXT NOT NULL DEFAULT 'A',
                        imageUrl TEXT NOT NULL DEFAULT '',
                        pricePerKg REAL NOT NULL,
                        quantityKg REAL NOT NULL,
                        totalAmount REAL NOT NULL,
                        district TEXT NOT NULL DEFAULT '',
                        taluk TEXT NOT NULL DEFAULT '',
                        isAvailable INTEGER NOT NULL DEFAULT 1,
                        addedAt INTEGER NOT NULL
                    )
                """)
                // Buyer Orders
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS buyer_orders (
                        orderId TEXT PRIMARY KEY NOT NULL,
                        productId TEXT NOT NULL,
                        farmerId TEXT NOT NULL,
                        farmerName TEXT NOT NULL DEFAULT '',
                        buyerId TEXT NOT NULL,
                        cropName TEXT NOT NULL,
                        cropCategory TEXT NOT NULL DEFAULT '',
                        quality TEXT NOT NULL DEFAULT 'Good',
                        grade TEXT NOT NULL DEFAULT 'A',
                        imageUrl TEXT NOT NULL DEFAULT '',
                        quantityKg REAL NOT NULL,
                        pricePerKg REAL NOT NULL,
                        subtotal REAL NOT NULL,
                        deliveryCharge REAL NOT NULL DEFAULT 0.0,
                        gst REAL NOT NULL DEFAULT 0.0,
                        discount REAL NOT NULL DEFAULT 0.0,
                        totalAmount REAL NOT NULL,
                        status TEXT NOT NULL DEFAULT 'PROCESSING',
                        deliveryAddress TEXT NOT NULL DEFAULT '',
                        paymentMethod TEXT NOT NULL DEFAULT 'CASH_ON_DELIVERY',
                        paymentStatus TEXT NOT NULL DEFAULT 'PENDING',
                        estimatedDelivery INTEGER NOT NULL DEFAULT 0,
                        deliveredAt INTEGER NOT NULL DEFAULT 0,
                        cancellationReason TEXT NOT NULL DEFAULT '',
                        farmerRating REAL NOT NULL DEFAULT 0.0,
                        review TEXT NOT NULL DEFAULT '',
                        trackingEvents TEXT NOT NULL DEFAULT '[]',
                        couponCode TEXT NOT NULL DEFAULT '',
                        couponDiscount REAL NOT NULL DEFAULT 0.0,
                        invoiceUrl TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL,
                        lastUpdated INTEGER NOT NULL,
                        syncStatus TEXT NOT NULL DEFAULT 'PENDING'
                    )
                """)
                // Wishlist
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS buyer_wishlist (
                        wishlistId TEXT PRIMARY KEY NOT NULL,
                        productId TEXT NOT NULL,
                        cropName TEXT NOT NULL,
                        cropCategory TEXT NOT NULL DEFAULT '',
                        farmerId TEXT NOT NULL,
                        farmerName TEXT NOT NULL DEFAULT '',
                        imageUrl TEXT NOT NULL DEFAULT '',
                        priceAtAdd REAL NOT NULL,
                        currentPrice REAL NOT NULL DEFAULT 0.0,
                        targetPrice REAL NOT NULL DEFAULT 0.0,
                        quality TEXT NOT NULL DEFAULT 'Good',
                        district TEXT NOT NULL DEFAULT '',
                        hasPriceAlert INTEGER NOT NULL DEFAULT 0,
                        alertTriggered INTEGER NOT NULL DEFAULT 0,
                        addedAt INTEGER NOT NULL
                    )
                """)
                // Search History
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS buyer_search_history (
                        searchId TEXT PRIMARY KEY NOT NULL,
                        query TEXT NOT NULL,
                        queryType TEXT NOT NULL DEFAULT 'TEXT',
                        resultCount INTEGER NOT NULL DEFAULT 0,
                        filtersApplied TEXT NOT NULL DEFAULT '{}',
                        searchedAt INTEGER NOT NULL
                    )
                """)
                // Price Alerts
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS buyer_price_alerts (
                        alertId TEXT PRIMARY KEY NOT NULL,
                        cropName TEXT NOT NULL,
                        cropCategory TEXT NOT NULL DEFAULT '',
                        targetPrice REAL NOT NULL,
                        currentPrice REAL NOT NULL DEFAULT 0.0,
                        district TEXT NOT NULL DEFAULT '',
                        isActive INTEGER NOT NULL DEFAULT 1,
                        isTriggered INTEGER NOT NULL DEFAULT 0,
                        triggeredAt INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL
                    )
                """)
                // Compared Products
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS buyer_compared_products (
                        comparisonId TEXT PRIMARY KEY NOT NULL,
                        productIds TEXT NOT NULL,
                        cropName TEXT NOT NULL,
                        bestProductId TEXT NOT NULL DEFAULT '',
                        bestReason TEXT NOT NULL DEFAULT '',
                        savedAt INTEGER NOT NULL
                    )
                """)
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE notifications ADD COLUMN category TEXT NOT NULL DEFAULT 'GENERAL'")
                database.execSQL("ALTER TABLE notifications ADD COLUMN priority TEXT NOT NULL DEFAULT 'NORMAL'")
                database.execSQL("ALTER TABLE notifications ADD COLUMN senderId TEXT")
                database.execSQL("ALTER TABLE notifications ADD COLUMN senderName TEXT")
                database.execSQL("ALTER TABLE notifications ADD COLUMN actionPayload TEXT")
                database.execSQL("ALTER TABLE notifications ADD COLUMN deepLink TEXT")
                database.execSQL("ALTER TABLE notifications ADD COLUMN imageUrl TEXT")
                database.execSQL("ALTER TABLE notifications ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE notifications ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE notifications ADD COLUMN syncStatus TEXT NOT NULL DEFAULT 'SYNCED'")
            }
        }
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE crop_listings ADD COLUMN syncStatus TEXT NOT NULL DEFAULT 'SYNCED'")
                database.execSQL("ALTER TABLE crop_listings ADD COLUMN uploadProgress INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE crop_listings ADD COLUMN lastSyncTime INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE crop_listings ADD COLUMN serverTimestamp INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE crop_listings ADD COLUMN conflictVersion INTEGER NOT NULL DEFAULT 1")
            }
        }
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS location_history (
                        id TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        type TEXT NOT NULL,
                        parentId TEXT,
                        breadcrumb TEXT NOT NULL,
                        isFavorite INTEGER NOT NULL DEFAULT 0,
                        lastUsedAt INTEGER NOT NULL
                    )
                """)
            }
        }
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns to disease_history
                database.execSQL("ALTER TABLE disease_history ADD COLUMN severityLevel TEXT NOT NULL DEFAULT 'UNKNOWN'")
                database.execSQL("ALTER TABLE disease_history ADD COLUMN healthScore INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE disease_history ADD COLUMN scientificName TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE disease_history ADD COLUMN organicTreatments TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE disease_history ADD COLUMN chemicalTreatments TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE disease_history ADD COLUMN recommendedFertilizers TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE disease_history ADD COLUMN recommendedPesticides TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE disease_history ADD COLUMN youtubeRecommendationsJson TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE disease_history ADD COLUMN gpsLocation TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE disease_history ADD COLUMN aiModelVersion TEXT NOT NULL DEFAULT '1.0'")
                database.execSQL("ALTER TABLE disease_history ADD COLUMN appVersion TEXT NOT NULL DEFAULT '1.0'")

                // Create ocr_history table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS ocr_history (
                        id TEXT PRIMARY KEY NOT NULL,
                        imagePath TEXT NOT NULL,
                        extractedText TEXT NOT NULL,
                        translatedText TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        scanDurationMs INTEGER NOT NULL,
                        confidence REAL NOT NULL,
                        detectedLanguage TEXT NOT NULL,
                        documentType TEXT NOT NULL DEFAULT 'UNKNOWN'
                    )
                """)
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS market_prices (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        state TEXT NOT NULL,
                        district TEXT NOT NULL,
                        market TEXT NOT NULL,
                        commodity TEXT NOT NULL,
                        variety TEXT NOT NULL,
                        grade TEXT NOT NULL,
                        minPrice REAL NOT NULL,
                        maxPrice REAL NOT NULL,
                        modalPrice REAL NOT NULL,
                        arrivalDate TEXT NOT NULL,
                        lastUpdated INTEGER NOT NULL
                    )
                """)
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns to existing tables
                database.execSQL("ALTER TABLE users ADD COLUMN profilePhotoUrl TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE users ADD COLUMN district TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE users ADD COLUMN taluk TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE users ADD COLUMN farmSizeAcres REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE users ADD COLUMN businessName TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE users ADD COLUMN isVerified INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE users ADD COLUMN rating REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE users ADD COLUMN totalDeals INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE users ADD COLUMN totalRevenue REAL NOT NULL DEFAULT 0.0")

                database.execSQL("ALTER TABLE farmer_profiles ADD COLUMN totalRevenue REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE farmer_profiles ADD COLUMN isVerified INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE buyer_profiles ADD COLUMN isVerified INTEGER NOT NULL DEFAULT 0")

                database.execSQL("ALTER TABLE crop_listings ADD COLUMN cropCategory TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE crop_listings ADD COLUMN aiRecommendedPrice REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE crop_listings ADD COLUMN district TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE crop_listings ADD COLUMN taluk TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE crop_listings ADD COLUMN farmerName TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE crop_listings ADD COLUMN farmerPhone TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE crop_listings ADD COLUMN description TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE crop_listings ADD COLUMN isFeatured INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE crop_listings ADD COLUMN viewCount INTEGER NOT NULL DEFAULT 0")

                database.execSQL("ALTER TABLE weather_cache ADD COLUMN feelsLike REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE weather_cache ADD COLUMN windSpeed REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE weather_cache ADD COLUMN conditionIcon TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE weather_cache ADD COLUMN uvIndex INTEGER NOT NULL DEFAULT 0")

                database.execSQL("ALTER TABLE apmc_prices ADD COLUMN district TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE apmc_prices ADD COLUMN cropCategory TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE apmc_prices ADD COLUMN trend TEXT NOT NULL DEFAULT 'STABLE'")
                database.execSQL("ALTER TABLE apmc_prices ADD COLUMN trendPercent REAL NOT NULL DEFAULT 0.0")

                database.execSQL("ALTER TABLE notifications ADD COLUMN relatedId TEXT NOT NULL DEFAULT ''")

                // Create new tables
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS favourite_products (
                        productId TEXT PRIMARY KEY NOT NULL,
                        addedAt INTEGER NOT NULL
                    )
                """)
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS transactions (
                        transactionId TEXT PRIMARY KEY NOT NULL,
                        productId TEXT NOT NULL,
                        farmerId TEXT NOT NULL,
                        buyerId TEXT NOT NULL,
                        cropName TEXT NOT NULL,
                        quantityKg REAL NOT NULL,
                        pricePerKg REAL NOT NULL,
                        totalAmount REAL NOT NULL,
                        status TEXT NOT NULL,
                        completedAt INTEGER NOT NULL,
                        farmerRating REAL NOT NULL DEFAULT 0.0,
                        buyerRating REAL NOT NULL DEFAULT 0.0,
                        review TEXT NOT NULL DEFAULT ''
                    )
                """)
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS market_intelligence (
                        id TEXT PRIMARY KEY NOT NULL,
                        cropName TEXT NOT NULL,
                        district TEXT NOT NULL,
                        demandLevel TEXT NOT NULL,
                        demandScore INTEGER NOT NULL,
                        predictedPrice7Days REAL NOT NULL,
                        bestSellingDay TEXT NOT NULL,
                        bestNearbyMarket TEXT NOT NULL,
                        expectedMovement TEXT NOT NULL,
                        expectedMovementPercent REAL NOT NULL,
                        weeklyTrendJson TEXT NOT NULL,
                        insight TEXT NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """)
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS disease_history (
                        id TEXT PRIMARY KEY NOT NULL,
                        cropName TEXT NOT NULL,
                        imagePath TEXT NOT NULL,
                        diseaseName TEXT NOT NULL,
                        confidenceScore REAL NOT NULL,
                        isHealthy INTEGER NOT NULL,
                        treatment TEXT NOT NULL,
                        detectedAt INTEGER NOT NULL
                    )
                """)
            }
        }
    }
}

