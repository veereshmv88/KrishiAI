package com.krishiai.app.disease.ui

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishiai.app.core.ai.AIResult
import com.krishiai.app.disease.ai.DiseaseKnowledge
import com.krishiai.app.disease.ai.DiseaseKnowledgeManager
import com.krishiai.app.disease.ai.HealthScoreCalculator
import com.krishiai.app.disease.data.DiseaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScannerUiState(
    val status: AIResult<Unit> = AIResult.Idle,
    val capturedImage: Bitmap? = null,
    val topPrediction: String? = null,
    val confidence: Float? = null,
    val healthScore: Int? = null,
    val knowledge: DiseaseKnowledge? = null
)

@HiltViewModel
class DiseaseDetectionViewModel @Inject constructor(
    private val repository: DiseaseRepository,
    private val knowledgeManager: DiseaseKnowledgeManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState

    fun processImage(bitmap: Bitmap) {
        _uiState.value = _uiState.value.copy(status = AIResult.Loading, capturedImage = bitmap)
        
        viewModelScope.launch {
            val result = repository.analyzeImage(bitmap)
            
            if (result is AIResult.Success) {
                val topPred = result.data.firstOrNull()
                if (topPred != null) {
                    val knowledge = knowledgeManager.getKnowledge(topPred.label)
                    val isHealthy = topPred.label.contains("healthy", ignoreCase = true)
                    val severity = knowledge?.severityLevel ?: "Moderate"
                    val score = HealthScoreCalculator.calculate(severity, topPred.confidence, isHealthy)
                    
                    _uiState.value = _uiState.value.copy(
                        status = AIResult.Success(Unit),
                        topPrediction = topPred.label,
                        confidence = topPred.confidence,
                        healthScore = score,
                        knowledge = knowledge
                    )
                } else {
                    _uiState.value = _uiState.value.copy(status = AIResult.LowConfidence())
                }
            } else {
                _uiState.value = _uiState.value.copy(status = result as AIResult<Unit>)
            }
        }
    }
    
    fun reset() {
        _uiState.value = ScannerUiState()
    }
}
