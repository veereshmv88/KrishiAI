package com.krishiai.app.domain.hardware

import kotlinx.coroutines.flow.Flow

interface VoiceRecognizer {
    fun startListening(): Flow<VoiceResult>
    fun stopListening()
}

sealed class VoiceResult {
    object Listening : VoiceResult()
    data class Success(val text: String) : VoiceResult()
    data class Error(val message: String) : VoiceResult()
}
