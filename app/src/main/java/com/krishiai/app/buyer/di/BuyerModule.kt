package com.krishiai.app.buyer.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.krishiai.app.buyer.data.dao.*
import com.krishiai.app.buyer.domain.repository.*
import com.krishiai.app.buyer.data.repository.*

import com.krishiai.app.data.local.KrishiDatabase
import com.krishiai.app.data.local.dao.APMCPriceDao
import com.krishiai.app.data.local.dao.CropListingDao
import com.krishiai.app.data.local.dao.MarketIntelligenceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import android.content.Context
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BuyerModule {

    // ─── DAOs ───────────────────────────────────────
    @Provides @Singleton
    fun provideCartItemDao(db: KrishiDatabase): CartItemDao = db.cartItemDao()

    @Provides @Singleton
    fun provideBuyerOrderDao(db: KrishiDatabase): BuyerOrderDao = db.buyerOrderDao()

    @Provides @Singleton
    fun provideWishlistDao(db: KrishiDatabase): WishlistDao = db.wishlistDao()

    @Provides @Singleton
    fun provideBuyerSearchHistoryDao(db: KrishiDatabase): BuyerSearchHistoryDao = db.buyerSearchHistoryDao()

    @Provides @Singleton
    fun providePriceAlertDao(db: KrishiDatabase): PriceAlertDao = db.priceAlertDao()

    @Provides @Singleton
    fun provideComparedProductDao(db: KrishiDatabase): ComparedProductDao = db.comparedProductDao()

    @Provides @Singleton
    fun provideBuyerProductRepository(
        cropListingDao: CropListingDao
    ): com.krishiai.app.buyer.domain.repository.BuyerProductRepository = 
        com.krishiai.app.buyer.data.repository.BuyerProductRepositoryImpl(cropListingDao)

    @Provides @Singleton
    fun provideBuyerCartRepository(
        cartItemDao: CartItemDao,
        @ApplicationContext context: Context
    ): com.krishiai.app.buyer.domain.repository.BuyerCartRepository = 
        com.krishiai.app.buyer.data.repository.BuyerCartRepositoryImpl(cartItemDao, context)

    @Provides @Singleton
    fun provideBuyerOrderRepository(
        buyerOrderDao: BuyerOrderDao,
        cartItemDao: CartItemDao,
        firestore: FirebaseFirestore,
        @ApplicationContext context: Context
    ): com.krishiai.app.buyer.domain.repository.BuyerOrderRepository = 
        com.krishiai.app.buyer.data.repository.BuyerOrderRepositoryImpl(buyerOrderDao, cartItemDao, firestore, context)

    @Provides @Singleton
    fun provideBuyerWishlistRepository(
        wishlistDao: WishlistDao,
        priceAlertDao: PriceAlertDao,
        @ApplicationContext context: Context
    ): com.krishiai.app.buyer.domain.repository.BuyerWishlistRepository = 
        com.krishiai.app.buyer.data.repository.BuyerWishlistRepositoryImpl(wishlistDao, priceAlertDao, context)

    @Provides @Singleton
    fun provideBuyerSearchRepository(
        searchHistoryDao: BuyerSearchHistoryDao
    ): com.krishiai.app.buyer.domain.repository.BuyerSearchRepository = 
        com.krishiai.app.buyer.data.repository.BuyerSearchRepositoryImpl(searchHistoryDao)

    @Provides @Singleton
    fun provideBuyerMarketRepository(
        firestore: FirebaseFirestore,
        apmcPriceDao: APMCPriceDao,
        marketIntelligenceDao: MarketIntelligenceDao
    ): BuyerMarketRepository = 
        BuyerMarketRepositoryImpl(firestore, apmcPriceDao, marketIntelligenceDao)

    @Provides @Singleton
    fun provideBuyerNotificationRepository(
        notificationDao: com.krishiai.app.data.local.dao.NotificationDao
    ): BuyerNotificationRepository = 
        BuyerNotificationRepositoryImpl(notificationDao)

    @Provides @Singleton
    fun provideBuyerAnalyticsRepository(
        buyerOrderDao: BuyerOrderDao
    ): BuyerAnalyticsRepository = 
        BuyerAnalyticsRepositoryImpl(buyerOrderDao)
}
