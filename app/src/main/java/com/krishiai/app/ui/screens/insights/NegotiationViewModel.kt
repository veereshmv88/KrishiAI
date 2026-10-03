package com.krishiai.app.ui.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishiai.app.domain.ai.IAINegotiationAssistant
import com.krishiai.app.domain.ai.NegotiationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class NegotiationState {
    object Idle : NegotiationState()
    object Loading : NegotiationState()
    data class Success(val result: NegotiationResult) : NegotiationState()
    data class Error(val message: String) : NegotiationState()
}

@HiltViewModel
class NegotiationViewModel @Inject constructor(
    private val aiNegotiationAssistant: IAINegotiationAssistant
) : ViewModel() {

    private val _uiState = MutableStateFlow<NegotiationState>(NegotiationState.Idle)
    val uiState: StateFlow<NegotiationState> = _uiState.asStateFlow()

    fun analyzeOffer(
        cropName: String,
        buyerOfferPrice: Double,
        predictedFairPrice: Double,
        currentApmcPrice: Double
    ) {
        viewModelScope.launch {
            _uiState.value = NegotiationState.Loading
            try {
                val result = aiNegotiationAssistant.analyzeOffer(
                    cropName,
                    buyerOfferPrice,
                    predictedFairPrice,
                    currentApmcPrice
                )
                _uiState.value = NegotiationState.Success(result)
            } catch (e: Exception) {
                _uiState.value = NegotiationState.Error(e.message ?: "Analysis failed")
            }
        }
    }

    fun reset() {
        _uiState.value = NegotiationState.Idle
    }
}
