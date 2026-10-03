package com.krishiai.app.core.ai

object AIManager {
    private val engines = mutableMapOf<String, AIEngine<*, *>>()

    fun <I, O> getEngine(engineId: String): AIEngine<I, O>? {
        @Suppress("UNCHECKED_CAST")
        return engines[engineId] as? AIEngine<I, O>
    }

    fun registerEngine(engineId: String, engine: AIEngine<*, *>) {
        if (!engines.containsKey(engineId)) {
            engine.initialize()
            engines[engineId] = engine
        }
    }

    fun releaseEngine(engineId: String) {
        engines[engineId]?.close()
        engines.remove(engineId)
    }

    fun releaseAll() {
        engines.values.forEach { it.close() }
        engines.clear()
    }
}
