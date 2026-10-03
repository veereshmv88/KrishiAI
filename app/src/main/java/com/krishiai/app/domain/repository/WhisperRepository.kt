package com.krishiai.app.domain.repository

import com.krishiai.app.domain.ai.SpeechRecognitionResult

interface WhisperRepository {
    suspend fun transcribeAudio(pcmData: ByteArray, language: String): Result<SpeechRecognitionResult>
}
