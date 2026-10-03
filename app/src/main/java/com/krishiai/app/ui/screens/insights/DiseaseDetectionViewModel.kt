package com.krishiai.app.ui.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishiai.app.domain.ai.DiseaseDetectionResult
import com.krishiai.app.domain.ai.IAIDiseaseDetector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed class DiseaseDetectionState {
    object Idle : DiseaseDetectionState()
    object Loading : DiseaseDetectionState()
    data class Success(val result: DiseaseDetectionResult) : DiseaseDetectionState()
    data class Error(val message: String) : DiseaseDetectionState()
}

@HiltViewModel
class DiseaseDetectionViewModel @Inject constructor(
    private val aiDiseaseDetector: IAIDiseaseDetector
) : ViewModel() {

    private val _uiState = MutableStateFlow<DiseaseDetectionState>(DiseaseDetectionState.Idle)
    val uiState: StateFlow<DiseaseDetectionState> = _uiState.asStateFlow()

    fun detectDisease(imageFile: File) {
        viewModelScope.launch {
            _uiState.value = DiseaseDetectionState.Loading
            try {
                val result = aiDiseaseDetector.detectDisease(imageFile)
                _uiState.value = DiseaseDetectionState.Success(result)
            } catch (e: Exception) {
                _uiState.value = DiseaseDetectionState.Error(e.message ?: "Failed to detect disease")
            }
        }
    }

    fun reset() {
        _uiState.value = DiseaseDetectionState.Idle
    }
}
