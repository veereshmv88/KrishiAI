package com.krishiai.app.domain.ai

import kotlinx.coroutines.flow.StateFlow

interface TextToSpeechEngine {
    val state: StateFlow<TtsState>
    
    fun speak(text: String, language: String)
    fun stop()
    fun pause()
    fun resume()
    
    enum class TtsState {
        IDLE,
        LOADING,
        SPEAKING,
        COMPLETED,
        ERROR
    }
}
