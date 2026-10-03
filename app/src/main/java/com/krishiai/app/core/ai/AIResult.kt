package com.krishiai.app.core.ai

sealed class AIResult<out T> {
    object Idle : AIResult<Nothing>()
    object Loading : AIResult<Nothing>()
    data class Success<out T>(val data: T) : AIResult<T>()
    data class Warning<out T>(val data: T, val message: String) : AIResult<T>()
    data class LowConfidence(val message: String = "Unable to confidently identify disease") : AIResult<Nothing>()
    data class Error(val exception: Throwable, val message: String? = null) : AIResult<Nothing>()
}
