package com.krishiai.app.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<T>(
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<T>>(UiState.Idle)
    val uiState: StateFlow<UiState<T>> = _uiState.asStateFlow()

    protected fun executeSafeCall(
        dispatcher: CoroutineDispatcher = defaultDispatcher,
        call: suspend () -> T
    ) {
        viewModelScope.launch(dispatcher) {
            _uiState.value = UiState.Loading
            try {
                val result = call()
                _uiState.value = UiState.Success(result)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    protected fun emitState(state: UiState<T>) {
        _uiState.value = state
    }
}
