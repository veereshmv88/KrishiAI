package com.krishiai.app.data.repository

import com.krishiai.app.core.security.ApiKeyProvider
import com.krishiai.app.data.local.dao.MarketPriceDao
import com.krishiai.app.data.local.entity.MarketPriceEntity
import com.krishiai.app.data.network.AgmarknetApiService
import com.krishiai.app.domain.repository.MarketPriceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarketPriceRepositoryImpl @Inject constructor(
    private val apiService: AgmarknetApiService,
    private val dao: MarketPriceDao,
    private val apiKeyProvider: ApiKeyProvider
) : MarketPriceRepository {

    override fun getPricesByLocation(state: String, district: String): Flow<Result<List<MarketPriceEntity>>> {
        return dao.getPricesByLocation(state, district).map { Result.success(it) }
    }

    override fun getPricesByCommodity(commodity: String): Flow<Result<List<MarketPriceEntity>>> {
        return dao.getPricesByCommodity(commodity).map { Result.success(it) }
    }

    override suspend fun refreshPricesByLocation(state: String, district: String): Result<Unit> {
        return try {
            val response = apiService.getMarketPrices(
                apiKey = apiKeyProvider.getAgmarknetApiKey(),
                state = state,
                district = district
            )
            
            if (response.isSuccessful && response.body() != null) {
                val records = response.body()?.records ?: emptyList()
                val entities = records.mapNotNull { dto ->
                    if (dto.state != null && dto.district != null && dto.market != null && dto.commodity != null) {
                        MarketPriceEntity(
                            state = dto.state,
                            district = dto.district,
                            market = dto.market,
                            commodity = dto.commodity,
                            variety = dto.variety ?: "",
                            grade = dto.grade ?: "",
                            minPrice = dto.minPrice ?: 0.0,
                            maxPrice = dto.maxPrice ?: 0.0,
                            modalPrice = dto.modalPrice ?: 0.0,
                            arrivalDate = dto.arrivalDate ?: "",
                            lastUpdated = System.currentTimeMillis()
                        )
                    } else null
                }
                dao.insertAll(entities)
                Result.success(Unit)
            } else {
                val errorMessage = when (response.code()) {
                    401, 403 -> "Market API Key is invalid or missing. Please configure your API key."
                    404 -> "Market API endpoint not found."
                    429 -> "Too many requests to Market API. Please try again later."
                    500, 502, 503, 504 -> "Market API server is currently down. Please try again later."
                    else -> "Failed to connect to Market API (Code: ${response.code()})"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshPricesByCommodity(commodity: String): Result<Unit> {
        return try {
            val response = apiService.getMarketPrices(
                apiKey = apiKeyProvider.getAgmarknetApiKey(),
                commodity = commodity
            )
            
            if (response.isSuccessful && response.body() != null) {
                val records = response.body()?.records ?: emptyList()
                val entities = records.mapNotNull { dto ->
                    if (dto.state != null && dto.district != null && dto.market != null && dto.commodity != null) {
                        MarketPriceEntity(
                            state = dto.state,
                            district = dto.district,
                            market = dto.market,
                            commodity = dto.commodity,
                            variety = dto.variety ?: "",
                            grade = dto.grade ?: "",
                            minPrice = dto.minPrice ?: 0.0,
                            maxPrice = dto.maxPrice ?: 0.0,
                            modalPrice = dto.modalPrice ?: 0.0,
                            arrivalDate = dto.arrivalDate ?: "",
                            lastUpdated = System.currentTimeMillis()
                        )
                    } else null
                }
                dao.insertAll(entities)
                Result.success(Unit)
            } else {
                Result.failure(Exception("API Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
