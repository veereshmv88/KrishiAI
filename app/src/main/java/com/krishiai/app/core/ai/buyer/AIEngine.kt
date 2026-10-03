    package com.krishiai.app.core.ai.buyer

interface AIEngine<Input, Output> {
    suspend fun process(input: Input): AIResult<Output>
}
