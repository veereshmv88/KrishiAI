package com.krishiai.app.domain.ai

import kotlinx.coroutines.flow.StateFlow

data class SpeechRecognitionResult(
    val text: String,
    val language: String? = null,
    val isFinal: Boolean = true
)

interface SpeechToTextEngine {
    val state: StateFlow<SpeechState>
    val recognitionResult: StateFlow<SpeechRecognitionResult?>
    
    fun startListening(language: String)
    fun stopListening()
    fun cancel()
    
    enum class SpeechState {
        IDLE,
        LISTENING,
        PROCESSING,
        SUCCESS,
        ERROR
    }
}
