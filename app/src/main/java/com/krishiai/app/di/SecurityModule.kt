package com.krishiai.app.di

import com.krishiai.app.core.security.ApiKeyProvider
import com.krishiai.app.core.security.ApiKeyProviderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SecurityModule {

    @Binds
    @Singleton
    abstract fun bindApiKeyProvider(
        apiKeyProviderImpl: ApiKeyProviderImpl
    ): ApiKeyProvider
}
