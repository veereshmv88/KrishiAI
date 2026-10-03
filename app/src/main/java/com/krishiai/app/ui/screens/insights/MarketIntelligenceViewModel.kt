package com.krishiai.app.ui.screens.insights

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

sealed class MarketIntelligenceState {
    object Idle : MarketIntelligenceState()
    object Loading : MarketIntelligenceState()
    // data class Success(...) : MarketIntelligenceState()
    data class Error(val message: String) : MarketIntelligenceState()
}

@HiltViewModel
class MarketIntelligenceViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow<MarketIntelligenceState>(MarketIntelligenceState.Idle)
    val uiState: StateFlow<MarketIntelligenceState> = _uiState.asStateFlow()
}
