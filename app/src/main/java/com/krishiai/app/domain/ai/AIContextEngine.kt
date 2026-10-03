package com.krishiai.app.domain.ai

data class AIContext(
    val userId: String?,
    val role: String,
    val language: String,
    // Farmer-specific context
    val farmerName: String? = null,
    val district: String? = null,
    val taluk: String? = null,
    val farmSizeAcres: Double? = null,
    // Buyer-specific context
    val businessName: String? = null
)

interface AIContextEngine {
    suspend fun buildContext(language: String): AIContext
    fun buildSystemInstruction(context: AIContext): String
}
