package com.krishiai.app.disease.data

import com.krishiai.app.core.ai.AIManager
import com.krishiai.app.core.ai.AIResult
import com.krishiai.app.disease.ai.DiseaseDetectionHelper
import com.krishiai.app.disease.ai.Prediction
import kotlinx.coroutines.flow.Flow
import android.graphics.Bitmap

import javax.inject.Inject

class DiseaseRepository @Inject constructor(
    private val diseaseDao: DiseaseDao
) {
    fun getScanHistory(): Flow<List<DiseaseScanEntity>> {
        return diseaseDao.getAllScans()
    }

    suspend fun saveScanResult(entity: DiseaseScanEntity): Long {
        return diseaseDao.insertScan(entity)
    }

    suspend fun deleteScan(id: Int) {
        diseaseDao.deleteScan(id)
    }

    suspend fun analyzeImage(bitmap: Bitmap): AIResult<List<Prediction>> {
        // Log start time for performance tracking
        val startTime = System.currentTimeMillis()
        
        val engine = AIManager.getEngine<Bitmap, List<Prediction>>("disease_model")
            ?: return AIResult.Error(IllegalStateException("Disease Detection Engine not initialized"))
            
        val result = engine.analyze(bitmap)
        
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        println("Diagnostic: Inference total time = ${duration}ms")
        
        return result
    }
}
