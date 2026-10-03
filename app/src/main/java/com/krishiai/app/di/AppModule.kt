package com.krishiai.app.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.krishiai.app.data.local.KrishiDatabase
import com.krishiai.app.data.local.dao.*
import com.krishiai.app.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

import com.google.gson.Gson

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences {
        return context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KrishiDatabase {
        return Room.databaseBuilder(context, KrishiDatabase::class.java, "krishi_db")
            .addMigrations(
                KrishiDatabase.MIGRATION_1_2, KrishiDatabase.MIGRATION_2_3,
                KrishiDatabase.MIGRATION_3_4, KrishiDatabase.MIGRATION_4_5,
                KrishiDatabase.MIGRATION_5_6, KrishiDatabase.MIGRATION_6_7,
                KrishiDatabase.MIGRATION_7_8, KrishiDatabase.MIGRATION_8_9
            )
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(db: KrishiDatabase): UserDao = db.userDao()

    @Provides
    @Singleton
    fun provideCropListingDao(db: KrishiDatabase): CropListingDao = db.cropListingDao()

    @Provides
    @Singleton
    fun provideWeatherDao(db: KrishiDatabase): WeatherDao = db.weatherDao()

    @Provides
    @Singleton
    fun provideNotificationDao(db: KrishiDatabase): NotificationDao = db.notificationDao()

    @Provides
    @Singleton
    fun provideFavouriteProductDao(db: KrishiDatabase): FavouriteProductDao = db.favouriteProductDao()

    @Provides
    @Singleton
    fun provideAPMCPriceDao(db: KrishiDatabase): APMCPriceDao = db.apmcPriceDao()

    @Provides
    @Singleton
    fun provideTransactionDao(db: KrishiDatabase): TransactionDao = db.transactionDao()

    @Provides
    @Singleton
    fun provideMarketIntelligenceDao(db: KrishiDatabase): MarketIntelligenceDao = db.marketIntelligenceDao()

    @Provides
    @Singleton
    fun provideDiseaseHistoryDao(db: KrishiDatabase): DiseaseHistoryDao = db.diseaseHistoryDao()
    
    @Provides
    @Singleton
    fun provideMarketPriceDao(db: KrishiDatabase): com.krishiai.app.data.local.dao.MarketPriceDao = db.marketPriceDao()
    
    @Provides
    @Singleton
    fun provideOcrHistoryDao(db: KrishiDatabase): OcrHistoryDao = db.ocrHistoryDao()

    @Provides
    @Singleton
    fun provideLocationDao(db: KrishiDatabase): com.krishiai.app.data.local.dao.LocationDao = db.locationDao()

    @Provides
    @Singleton
    fun provideChatHistoryDao(db: KrishiDatabase): ChatHistoryDao = db.chatHistoryDao()
}
