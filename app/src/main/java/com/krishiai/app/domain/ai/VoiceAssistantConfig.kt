package com.krishiai.app.domain.ai

data class VoiceAssistantConfig(
    val speechLanguage: VoiceLanguage = VoiceLanguage.AUTO,
    val responseLanguage: VoiceLanguage = VoiceLanguage.AUTO,
    val autoDetectLanguage: Boolean = true,
    val enableTTS: Boolean = true,
    val enableVoiceInput: Boolean = true
)
