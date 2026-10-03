package com.krishiai.app.core.security

import com.krishiai.app.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

interface ApiKeyProvider {
    fun getAgmarknetApiKey(): String
}

@Singleton
class ApiKeyProviderImpl @Inject constructor() : ApiKeyProvider {
    override fun getAgmarknetApiKey(): String {
        return BuildConfig.AGMARKNET_API_KEY
    }
}
