package com.krishiai.app.ui.screens.insights

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishiai.app.domain.ai.IAIDiseaseDetector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CropScannerViewModel @Inject constructor(
    private val aiDiseaseDetector: IAIDiseaseDetector
) : ViewModel() {

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    fun analyzeImage(bitmap: Bitmap) {
        if (_scanState.value is ScanState.Analyzing) return
        
        _scanState.value = ScanState.Analyzing
        viewModelScope.launch {
            try {
                // Offload inference to Default dispatcher
                val result = withContext(Dispatchers.Default) {
                    aiDiseaseDetector.detectDisease(bitmap)
                }
                
                if (result.isHealthy) {
                    _scanState.value = ScanState.Result(
                        diseaseName = "Healthy Crop",
                        confidence = result.confidenceScore,
                        recommendation = "Your crop looks healthy! Continue your current maintenance schedule."
                    )
                } else {
                    _scanState.value = ScanState.Result(
                        diseaseName = result.diseaseName,
                        confidence = result.confidenceScore,
                        recommendation = result.suggestedTreatments.firstOrNull() ?: "Consult an expert."
                    )
                }
            } catch (e: Exception) {
                _scanState.value = ScanState.Error(e.message ?: "Failed to analyze image")
            }
        }
    }

    fun resetScanner() {
        _scanState.value = ScanState.Idle
    }
}

sealed class ScanState {
    object Idle : ScanState()
    object Analyzing : ScanState()
    data class Result(
        val diseaseName: String,
        val confidence: Float,
        val recommendation: String
    ) : ScanState()
    data class Error(val message: String) : ScanState()
}
