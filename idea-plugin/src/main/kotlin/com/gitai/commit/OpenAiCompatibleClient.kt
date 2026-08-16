package com.gitai.commit

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

data class OpenAiChatMessage(
    val role: String,
    val content: String
)

data class OpenAiChatCompletionRequest(
    val model: String,
    val messages: List<OpenAiChatMessage>,
    val temperature: Double = 0.0
)

class OpenAiCompatibleClient(
    private val baseUrl: String,
    private val apiKey: String = "",
    private val httpClient: HttpClient = HttpClient.newHttpClient(),
    private val objectMapper: ObjectMapper = ObjectMapper()
) : ModelProvider {
    override val id: String = "openai-compatible"

    override fun generate(model: String, prompt: String): String {
        val body = objectMapper.writeValueAsString(
            OpenAiChatCompletionRequest(
                model = model,
                messages = listOf(
                    OpenAiChatMessage("system", "只输出一句中文提交信息，不要输出 JSON、不要输出英文提交信息、不要解释、不要正文。"),
                    OpenAiChatMessage("user", prompt)
                )
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
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() >= 400) {
            throw IOException("OpenAI-compatible provider returned HTTP ${response.statusCode()}: ${response.body()}")
        }
        val json: JsonNode = objectMapper.readTree(response.body())
        return json.path("choices")
            .path(0)
            .path("message")
            .path("content")
            .asText("")
            .trim()
    }
}
