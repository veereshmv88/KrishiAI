package com.krishiai.app.domain.ai

interface AIToolManager {
    fun getAvailableTools(): List<String>
    suspend fun executeTool(toolName: String, args: Map<String, Any>): Any
}
