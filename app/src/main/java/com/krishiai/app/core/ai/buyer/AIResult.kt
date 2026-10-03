package com.krishiai.app.core.ai.buyer

sealed interface AIResult<out T> {
    data class Success<T>(val data: T) : AIResult<T>
    data class Error(val message: String, val exception: Exception? = null) : AIResult<Nothing>
    object Loading : AIResult<Nothing>
}
