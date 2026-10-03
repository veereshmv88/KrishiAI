package com.krishiai.app.data.local.dao

import androidx.room.*
import com.krishiai.app.data.local.entity.LocationHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Query("SELECT * FROM location_history ORDER BY lastUsedAt DESC LIMIT 20")
    fun getRecentLocations(): Flow<List<LocationHistoryEntity>>

    @Query("SELECT * FROM location_history WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteLocations(): Flow<List<LocationHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(location: LocationHistoryEntity)

    @Delete
    suspend fun delete(location: LocationHistoryEntity)
    
    @Query("UPDATE location_history SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)
}
