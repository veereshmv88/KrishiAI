package com.krishiai.app.ui.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishiai.app.domain.ai.IAIProfitEstimator
import com.krishiai.app.domain.ai.ProfitEstimationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfitEstimationState {
    object Idle : ProfitEstimationState()
    object Loading : ProfitEstimationState()
    data class Success(val result: ProfitEstimationResult) : ProfitEstimationState()
    data class Error(val message: String) : ProfitEstimationState()
}

@HiltViewModel
class ProfitEstimationViewModel @Inject constructor(
    private val aiProfitEstimator: IAIProfitEstimator
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfitEstimationState>(ProfitEstimationState.Idle)
    val uiState: StateFlow<ProfitEstimationState> = _uiState.asStateFlow()

    fun estimateProfit(
        expectedRevenue: Double,
        transportationCost: Double,
        labourCost: Double,
        fertilizerCost: Double,
        miscellaneousCost: Double
    ) {
        viewModelScope.launch {
            _uiState.value = ProfitEstimationState.Loading
            try {
                val result = aiProfitEstimator.estimateProfit(
                    expectedRevenue,
                    transportationCost,
                    labourCost,
                    fertilizerCost,
                    miscellaneousCost
                )
                _uiState.value = ProfitEstimationState.Success(result)
            } catch (e: Exception) {
                _uiState.value = ProfitEstimationState.Error(e.message ?: "Estimation failed")
            }
        }
    }

    fun reset() {
        _uiState.value = ProfitEstimationState.Idle
    }
}
