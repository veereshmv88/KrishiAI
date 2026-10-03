package com.krishiai.app.ui.screens.insights

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.krishiai.app.data.local.dao.OcrHistoryDao
import com.krishiai.app.data.local.entity.OcrHistoryEntity
import com.krishiai.app.utils.OcrImageProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class OcrScannerViewModel @Inject constructor(
    private val ocrHistoryDao: OcrHistoryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<OcrScanState>(OcrScanState.Idle)
    val uiState: StateFlow<OcrScanState> = _uiState.asStateFlow()

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun processImage(bitmap: Bitmap, rotationDegrees: Float) {
        if (_uiState.value is OcrScanState.Processing) return
        
        _uiState.value = OcrScanState.Processing
        
        viewModelScope.launch {
            try {
                // Preprocess the image natively
                val processedBitmap = withContext(Dispatchers.Default) {
                    OcrImageProcessor.preprocessForOcr(bitmap, rotationDegrees)
                }

                // Run ML Kit OCR
                val image = InputImage.fromBitmap(processedBitmap, 0)
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val text = visionText.text
                        if (text.isNotBlank()) {
                            val parsedData = parseOcrData(text)
                            _uiState.value = OcrScanState.Success(text, parsedData)
                            saveToHistory(text, parsedData)
                        } else {
                            _uiState.value = OcrScanState.Error("No text found. Please ensure the document is clear and well-lit.")
                        }
                    }
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                        _uiState.value = OcrScanState.Error(e.message ?: "OCR Failed")
                    }

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = OcrScanState.Error("Error processing image")
            }
        }
    }

    private fun parseOcrData(rawText: String): OcrParsedData {
        // Simple heuristic extraction for agricultural products
        val lowerText = rawText.lowercase()
        val isFertilizer = lowerText.contains("fertilizer") || lowerText.contains("npk") || lowerText.contains("urea") || lowerText.contains("dap")
        val isPesticide = lowerText.contains("pesticide") || lowerText.contains("insecticide") || lowerText.contains("fungicide") || lowerText.contains("herbicide")
        
        val category = when {
            isFertilizer -> "Fertilizer"
            isPesticide -> "Pesticide"
            lowerText.contains("seed") -> "Seed"
            lowerText.contains("invoice") || lowerText.contains("receipt") -> "Invoice"
            else -> "Other"
        }

        // Try extracting NPK
        val npkRegex = Regex("""(\d{1,2})[-:](\d{1,2})[-:](\d{1,2})""")
        val npkMatch = npkRegex.find(rawText)
        val npk = npkMatch?.value ?: ""

        return OcrParsedData(
            category = category,
            npkRatio = npk,
            confidence = 0.95f // ML Kit confidence usually high if text is found
        )
    }

    private fun saveToHistory(rawText: String, parsedData: OcrParsedData) {
        viewModelScope.launch {
            val entity = OcrHistoryEntity(
                id = java.util.UUID.randomUUID().toString(),
                extractedText = rawText,
                category = parsedData.category,
                npkRatio = parsedData.npkRatio,
                imagePath = "", // Save local image path in real app
                confidence = parsedData.confidence,
                timestamp = System.currentTimeMillis(),
                translatedText = "",
                scanDurationMs = 0L,
                detectedLanguage = "en",
                documentType = parsedData.category
            )
            ocrHistoryDao.insertHistory(entity)
        }
    }

    fun reset() {
        _uiState.value = OcrScanState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        recognizer.close()
    }
}

data class OcrParsedData(
    val category: String,
    val npkRatio: String,
    val confidence: Float
)

sealed class OcrScanState {
    object Idle : OcrScanState()
    object Processing : OcrScanState()
    data class Success(val rawText: String, val parsedData: OcrParsedData) : OcrScanState()
    data class Error(val message: String) : OcrScanState()
}
