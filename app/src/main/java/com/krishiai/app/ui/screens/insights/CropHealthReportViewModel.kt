package com.krishiai.app.ui.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishiai.app.disease.ai.DiseaseKnowledge
import com.krishiai.app.disease.ai.DiseaseKnowledgeManager
import com.krishiai.app.disease.ai.RecommendedVideo
import com.krishiai.app.disease.ai.VideoRecommendationEngine
import com.krishiai.app.data.local.dao.DiseaseHistoryDao
import com.krishiai.app.data.local.entity.DiseaseHistoryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CropHealthReportViewModel @Inject constructor(
    private val diseaseKnowledgeManager: DiseaseKnowledgeManager,
    private val videoRecommendationEngine: VideoRecommendationEngine,
    private val diseaseHistoryDao: DiseaseHistoryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<CropHealthReportState>(CropHealthReportState.Loading)
    val uiState: StateFlow<CropHealthReportState> = _uiState.asStateFlow()

    fun loadReport(diseaseName: String, confidenceScore: Float) {
        viewModelScope.launch {
            // Fuzzy match the disease name if the exact key isn't found
            var knowledge = diseaseKnowledgeManager.getKnowledge(diseaseName)
            if (knowledge == null) {
                // Try finding by just matching the name part
                val allKeys = diseaseKnowledgeManager.getAllKeys() // Assuming we add this, or we just modify getKnowledge
                knowledge = diseaseKnowledgeManager.findKnowledgeFuzzy(diseaseName)
            }

            if (knowledge == null) {
                _uiState.value = CropHealthReportState.Error("Disease details not found in knowledge base for: $diseaseName")
                return@launch
            }

            val healthScore = calculateHealthScore(knowledge.severityLevel)

            val videos = videoRecommendationEngine.getRecommendations(
                cropName = knowledge.cropName,
                diseaseName = knowledge.diseaseName,
                language = "en"
            )

            _uiState.value = CropHealthReportState.Success(
                diseaseKnowledge = knowledge,
                confidenceScore = confidenceScore,
                healthScore = healthScore,
                recommendedVideos = videos
            )

            // Save to Database
            saveToHistory(knowledge, confidenceScore, healthScore)
        }
    }

    private suspend fun saveToHistory(knowledge: DiseaseKnowledge, confidence: Float, healthScore: Int) {
            val entity = DiseaseHistoryEntity(
                id = java.util.UUID.randomUUID().toString(),
                cropName = knowledge.cropName,
                diseaseName = knowledge.diseaseName,
                confidenceScore = confidence,
                healthScore = healthScore,
                severityLevel = knowledge.severityLevel,
                organicTreatments = knowledge.organicTreatment,
                chemicalTreatments = knowledge.chemicalTreatment,
                recommendedFertilizers = knowledge.recommendedFertilizer,
                imagePath = "", // In a real app, save the image locally and put the path here
                detectedAt = System.currentTimeMillis(),
                isHealthy = knowledge.severityLevel.lowercase() == "healthy" || knowledge.severityLevel.lowercase() == "none",
                treatment = knowledge.organicTreatment
            )
        diseaseHistoryDao.insertHistory(entity)
    }

    private fun calculateHealthScore(severity: String): Int {
        return when (severity.lowercase()) {
            "healthy", "none" -> 95
            "mild" -> 70
            "moderate" -> 50
            "severe" -> 20
            else -> 60
        }
    }
}

sealed class CropHealthReportState {
    object Loading : CropHealthReportState()
    data class Success(
        val diseaseKnowledge: DiseaseKnowledge,
        val confidenceScore: Float,
        val healthScore: Int,
        val recommendedVideos: List<RecommendedVideo>
    ) : CropHealthReportState()
    data class Error(val message: String) : CropHealthReportState()
}
