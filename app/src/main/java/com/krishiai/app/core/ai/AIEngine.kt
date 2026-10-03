package com.krishiai.app.core.ai

interface AIEngine<Input, Output> {
    fun initialize()
    suspend fun analyze(input: Input): AIResult<Output>
    fun close()
}
