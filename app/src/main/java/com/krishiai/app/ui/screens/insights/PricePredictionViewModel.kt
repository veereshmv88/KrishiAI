package com.krishiai.app.ui.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishiai.app.domain.ai.IAIPricePredictor
import com.krishiai.app.domain.ai.PricePredictionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PricePredictionState {
    object Idle : PricePredictionState()
    object Loading : PricePredictionState()
    data class Success(val result: PricePredictionResult) : PricePredictionState()
    data class Error(val message: String) : PricePredictionState()
}

@HiltViewModel
class PricePredictionViewModel @Inject constructor(
    private val aiPricePredictor: IAIPricePredictor
) : ViewModel() {

    private val _uiState = MutableStateFlow<PricePredictionState>(PricePredictionState.Idle)
    val uiState: StateFlow<PricePredictionState> = _uiState.asStateFlow()

    fun predictPrice(
        cropName: String,
        district: String,
        quality: String,
        quantityKg: Double,
        harvestDate: Long,
        historicalApmcPrice: Double,
        weatherCondition: String
    ) {
        viewModelScope.launch {
            _uiState.value = PricePredictionState.Loading
            try {
                val result = aiPricePredictor.predictPrice(
                    cropName,
                    district,
                    quality,
                    quantityKg,
                    harvestDate,
                    historicalApmcPrice,
                    weatherCondition
                )
                _uiState.value = PricePredictionState.Success(result)
            } catch (e: Exception) {
                _uiState.value = PricePredictionState.Error(e.message ?: "Failed to predict price")
            }
        }
    }

    fun reset() {
        _uiState.value = PricePredictionState.Idle
    }
}
