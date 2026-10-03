package com.krishiai.app.domain.ai

data class AIResponse(
    val text: String,
    val language: String,
    val toolCalls: List<String> = emptyList(),
    val success: Boolean = true,
    val error: String? = null
)

interface AIReasoningEngine {
    suspend fun processQuery(query: String, language: String): AIResponse
}
