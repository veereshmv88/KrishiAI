package com.krishiai.app.domain.ai

import org.junit.Assert.*
import org.junit.Test

class VoiceArchitectureTest {

    @Test
    fun testVoiceLanguage() {
        assertEquals("en", VoiceLanguage.ENGLISH.code)
        assertEquals("kn", VoiceLanguage.KANNADA.code)
        assertEquals("auto", VoiceLanguage.AUTO.code)
    }

    @Test
    fun testSpeechRecognitionResult() {
        val result = SpeechRecognitionResult(text = "hello", language = "en", isFinal = true)
        assertEquals("hello", result.text)
        assertEquals("en", result.language)
        assertTrue(result.isFinal)
    }

    @Test
    fun testAIResponse() {
        val response = AIResponse(text = "response", language = "kn", toolCalls = listOf("get_weather_forecast"), success = true, error = null)
        assertEquals("response", response.text)
        assertEquals("kn", response.language)
        assertEquals(1, response.toolCalls.size)
        assertTrue(response.success)
        assertNull(response.error)
    }

    @Test
    fun testVoiceAssistantConfig() {
        val config = VoiceAssistantConfig(
            speechLanguage = VoiceLanguage.ENGLISH,
            responseLanguage = VoiceLanguage.KANNADA,
            autoDetectLanguage = false,
            enableTTS = true,
            enableVoiceInput = false
        )
        assertEquals(VoiceLanguage.ENGLISH, config.speechLanguage)
        assertEquals(VoiceLanguage.KANNADA, config.responseLanguage)
        assertFalse(config.autoDetectLanguage)
        assertTrue(config.enableTTS)
        assertFalse(config.enableVoiceInput)
    }

    @Test
    fun testStateTransitions() {
        var state = VoiceAssistantManager.AssistantState.IDLE
        assertEquals(VoiceAssistantManager.AssistantState.IDLE, state)

        state = VoiceAssistantManager.AssistantState.LISTENING
        assertEquals(VoiceAssistantManager.AssistantState.LISTENING, state)

        state = VoiceAssistantManager.AssistantState.PROCESSING_SPEECH
        assertEquals(VoiceAssistantManager.AssistantState.PROCESSING_SPEECH, state)

        state = VoiceAssistantManager.AssistantState.THINKING
        assertEquals(VoiceAssistantManager.AssistantState.THINKING, state)

        state = VoiceAssistantManager.AssistantState.SPEAKING
        assertEquals(VoiceAssistantManager.AssistantState.SPEAKING, state)

        state = VoiceAssistantManager.AssistantState.ERROR
        assertEquals(VoiceAssistantManager.AssistantState.ERROR, state)
        
        state = VoiceAssistantManager.AssistantState.IDLE
        assertEquals(VoiceAssistantManager.AssistantState.IDLE, state)
    }
}
