package com.krishiai.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "market_prices")
data class MarketPriceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val state: String,
    val district: String,
    val market: String,
    val commodity: String,
    val variety: String,
    val grade: String,
    val minPrice: Double,
    val maxPrice: Double,
    val modalPrice: Double,
    val arrivalDate: String,
    val lastUpdated: Long // Timestamp of when it was fetched
)
