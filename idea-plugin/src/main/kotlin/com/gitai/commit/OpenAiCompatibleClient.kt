package com.gitai.commit

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.stream.Stream

data class OpenAiChatMessage(
    val role: String,
    val content: String
)

data class OpenAiChatCompletionRequest(
    val model: String,
    val messages: List<OpenAiChatMessage>,
    val temperature: Double = 0.0,
    val stream: Boolean = false
)

class OpenAiCompatibleClient(
    private val baseUrl: String,
    private val apiKey: String = "",
    private val httpClient: HttpClient = HttpClient.newHttpClient(),
    private val objectMapper: ObjectMapper = ObjectMapper()
) : ModelProvider {
    override val id: String = "openai-compatible"

    override fun generate(model: String, prompt: String): String {
        return generateStream(model, prompt) { }
    }

    override fun generateStream(model: String, prompt: String, onChunk: (String) -> Unit): String {
        val body = objectMapper.writeValueAsString(
            OpenAiChatCompletionRequest(
                model = model,
                messages = listOf(
                    OpenAiChatMessage("system", "只输出一句中文提交信息，不要输出 JSON、不要输出英文提交信息、不要解释、不要正文。"),
                    OpenAiChatMessage("user", prompt)
                ),
                temperature = 0.0,
                stream = true
            )
        )
        val requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create("${baseUrl.trimEnd('/')}/chat/completions"))
            .header("Content-Type", "application/json")
        if (apiKey.isNotBlank()) {
            requestBuilder.header("Authorization", "Bearer $apiKey")
        }
        val request = requestBuilder
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofLines())
        if (response.statusCode() >= 400) {
            throw IOException("OpenAI-compatible provider returned HTTP ${response.statusCode()}: ${response.body()}")
        }

        val raw = StringBuilder()
        response.body().use { lines ->
            lines.forEach { line ->
                val delta = extractOpenAiCompatibleDelta(line)
                if (delta.isNotEmpty()) {
                    raw.append(delta)
                    onChunk(delta)
                }
            }
        }
        return raw.toString().trim()
    }
}

private fun extractOpenAiCompatibleDelta(line: String): String {
    val trimmed = line.trim()
    if (!trimmed.startsWith("data:")) {
        return ""
    }

    val payload = trimmed.removePrefix("data:").trim()
    if (payload.isEmpty() || payload == "[DONE]") {
        return ""
    }

    return try {
        val json = ObjectMapper().readTree(payload)
        val delta = json.path("choices")
            .path(0)
            .path("delta")
            .path("content")
            .asText("")
        if (delta.isNotEmpty()) {
            delta
        } else {
            json.path("choices")
                .path(0)
                .path("message")
                .path("content")
                .asText("")
        }
    } catch (_: Exception) {
        ""
    }
}
