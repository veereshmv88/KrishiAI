package com.krishiai.app.domain.ai

import kotlinx.coroutines.flow.StateFlow

enum class VoiceAssistantState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING,
    ERROR
}

data class ChatMessage(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long
)

interface IAIVoiceAssistant {
    val state: StateFlow<VoiceAssistantState>
    val partialText: StateFlow<String>
    
    fun startListening(languageCode: String)
    fun stopListening()
    fun speak(text: String, languageCode: String)
    fun stopSpeaking()
    suspend fun processQuery(query: String, languageCode: String): String
    fun destroy()
}
