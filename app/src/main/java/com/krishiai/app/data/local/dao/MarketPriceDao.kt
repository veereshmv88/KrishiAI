package com.krishiai.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.krishiai.app.data.local.entity.MarketPriceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketPriceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(prices: List<MarketPriceEntity>)

    @Query("SELECT * FROM market_prices WHERE state = :state AND district = :district ORDER BY arrivalDate DESC")
    fun getPricesByLocation(state: String, district: String): Flow<List<MarketPriceEntity>>

    @Query("SELECT * FROM market_prices WHERE commodity = :commodity ORDER BY arrivalDate DESC")
    fun getPricesByCommodity(commodity: String): Flow<List<MarketPriceEntity>>

    @Query("DELETE FROM market_prices")
    suspend fun clearAll()
}
