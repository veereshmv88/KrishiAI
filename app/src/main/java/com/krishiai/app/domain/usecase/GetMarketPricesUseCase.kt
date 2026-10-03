package com.krishiai.app.domain.usecase

import com.krishiai.app.domain.repository.MarketPriceRepository
import kotlinx.coroutines.flow.Flow
import com.krishiai.app.data.local.entity.MarketPriceEntity
import javax.inject.Inject

class GetMarketPricesUseCase @Inject constructor(
    private val repository: MarketPriceRepository
) {
    operator fun invoke(state: String, district: String): Flow<Result<List<MarketPriceEntity>>> {
        val normalizedDistrict = normalizeDistrict(district)
        return repository.getPricesByLocation(state, normalizedDistrict)
    }
    
    suspend fun refresh(state: String, district: String): Result<Unit> {
        val normalizedDistrict = normalizeDistrict(district)
        return repository.refreshPricesByLocation(state, normalizedDistrict)
    }

    private fun normalizeDistrict(district: String): String {
        return when (district.trim().lowercase()) {
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
    }
}
