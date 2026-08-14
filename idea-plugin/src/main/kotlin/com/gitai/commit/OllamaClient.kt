package com.gitai.commit

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

data class CommitMessageRequest(
    val model: String,
    val prompt: String,
    val stream: Boolean = false
)

class OllamaClient(
    private val baseUrl: String,
    private val httpClient: HttpClient = HttpClient.newHttpClient(),
    private val objectMapper: ObjectMapper = ObjectMapper()
) : ModelProvider {
    override val id: String = "ollama"

    override fun generate(model: String, prompt: String): String {
        val body = objectMapper.writeValueAsString(CommitMessageRequest(model, prompt))
        val request = HttpRequest.newBuilder()
            .uri(URI.create("${baseUrl.trimEnd('/')}/api/generate"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() >= 400) {
            throw IOException("Ollama returned HTTP ${response.statusCode()}: ${response.body()}")
        }
        val json: JsonNode = objectMapper.readTree(response.body())
        return json.path("response").asText("").trim()
    }
}
