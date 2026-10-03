package com.krishiai.app.disease.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DiseaseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: DiseaseScanEntity): Long

    @Query("SELECT * FROM disease_scans ORDER BY timestamp DESC")
    fun getAllScans(): Flow<List<DiseaseScanEntity>>

    @Query("SELECT * FROM disease_scans WHERE cropName LIKE '%' || :query || '%' OR diseaseName LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchScans(query: String): Flow<List<DiseaseScanEntity>>
    
    @Query("DELETE FROM disease_scans WHERE id = :id")
    suspend fun deleteScan(id: Int)
}
