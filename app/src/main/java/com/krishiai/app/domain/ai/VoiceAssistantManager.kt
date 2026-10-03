package com.krishiai.app.domain.ai

import kotlinx.coroutines.flow.StateFlow

interface VoiceAssistantManager {
    val state: StateFlow<AssistantState>
    
    fun startAssistant(language: String)
    fun stopAssistant()
    
    enum class AssistantState {
        IDLE,
        LISTENING,
        PROCESSING_SPEECH,
        THINKING,
        SPEAKING,
        ERROR
    }
}
