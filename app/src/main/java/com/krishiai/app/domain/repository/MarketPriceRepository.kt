package com.krishiai.app.domain.repository

import com.krishiai.app.data.local.entity.MarketPriceEntity
import kotlinx.coroutines.flow.Flow

interface MarketPriceRepository {
    fun getPricesByLocation(state: String, district: String): Flow<Result<List<MarketPriceEntity>>>
    fun getPricesByCommodity(commodity: String): Flow<Result<List<MarketPriceEntity>>>
    suspend fun refreshPricesByLocation(state: String, district: String): Result<Unit>
    suspend fun refreshPricesByCommodity(commodity: String): Result<Unit>
}
