package com.gitai.commit

import kotlin.test.Test
import kotlin.test.assertEquals

class ModelRequestDefaultsTest {
    @Test
    fun ollamaRequestsUseDeterministicTemperature() {
        val request = CommitMessageRequest(
            model = "qwen2.5-coder:7b",
            prompt = "prompt"
        )

        assertEquals(mapOf("temperature" to 0.0), request.options)
    }

    @Test
    fun openAiCompatibleRequestsUseDeterministicTemperature() {
        val request = OpenAiChatCompletionRequest(
            model = "model",
            messages = listOf(OpenAiChatMessage("user", "prompt"))
        )

        assertEquals(0.0, request.temperature)
    }
}
