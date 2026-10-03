package com.krishiai.app.ui.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

sealed class CropRecommendationState {
    object Idle : CropRecommendationState()
    object Loading : CropRecommendationState()
    data class Success(val crops: List<CropRecommendation>) : CropRecommendationState()
    data class Error(val message: String) : CropRecommendationState()
}

data class CropRecommendation(val name: String, val matchScore: Int, val expectedYield: String)

@HiltViewModel
class CropRecommendationViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow<CropRecommendationState>(CropRecommendationState.Idle)
    val uiState: StateFlow<CropRecommendationState> = _uiState.asStateFlow()

    fun getRecommendations(soilType: String, season: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            _uiState.value = CropRecommendationState.Loading
            kotlinx.coroutines.delay(1200) // Simulate processing delay
            try {
                val recommendations = if (season.equals("Kharif", true)) {
                    listOf(
                        CropRecommendation("Rice", 92, "High"),
                        CropRecommendation("Maize", 85, "Medium"),
                        CropRecommendation("Groundnut", 78, "Medium")
                    )
                } else {
                    listOf(
                        CropRecommendation("Wheat", 95, "High"),
                        CropRecommendation("Tomato", 88, "High"),
                        CropRecommendation("Onion", 82, "Medium")
                    )
                }
                _uiState.value = CropRecommendationState.Success(recommendations)
            } catch (e: Exception) {
                _uiState.value = CropRecommendationState.Error("Failed to fetch recommendations")
            }
        }
    }
}
