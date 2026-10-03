package com.krishiai.app.di

import com.krishiai.app.data.repository.AuthRepositoryImpl
import com.krishiai.app.data.repository.MarketplaceRepositoryImpl
import com.krishiai.app.data.repository.WeatherRepositoryImpl
import com.krishiai.app.domain.repository.AuthRepository
import com.krishiai.app.domain.repository.MarketplaceRepository
import com.krishiai.app.domain.repository.WeatherRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindMarketplaceRepository(
        marketplaceRepositoryImpl: MarketplaceRepositoryImpl
    ): MarketplaceRepository

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(
        weatherRepositoryImpl: WeatherRepositoryImpl
    ): WeatherRepository

    @Binds
    @Singleton
    abstract fun bindMarketPriceRepository(
        marketPriceRepositoryImpl: com.krishiai.app.data.repository.MarketPriceRepositoryImpl
    ): com.krishiai.app.domain.repository.MarketPriceRepository

    @Binds
    @Singleton
    abstract fun bindWhisperRepository(
        whisperRepositoryImpl: com.krishiai.app.data.repository.WhisperRepositoryImpl
    ): com.krishiai.app.domain.repository.WhisperRepository
}
