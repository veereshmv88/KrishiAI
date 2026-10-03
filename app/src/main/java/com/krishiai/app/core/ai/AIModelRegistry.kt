package com.krishiai.app.core.ai

data class AIModelMetadata(
    val modelId: String,
    val name: String,
    val version: String,
    val inputSize: Int,
    val requiredAssets: List<String>
)

object AIModelRegistry {
    private val models = mutableMapOf<String, AIModelMetadata>()

    fun registerModel(metadata: AIModelMetadata) {
        models[metadata.modelId] = metadata
    }

    fun getModel(modelId: String): AIModelMetadata? {
        return models[modelId]
    }
    
    fun getAllModels(): List<AIModelMetadata> {
        return models.values.toList()
    }
}
