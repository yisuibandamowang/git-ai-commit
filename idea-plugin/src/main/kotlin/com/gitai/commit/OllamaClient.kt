package com.gitai.commit

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.stream.Stream

data class CommitMessageRequest(
    val model: String,
    val prompt: String,
    val stream: Boolean = false,
    val options: Map<String, Double> = mapOf("temperature" to 0.0)
)

class OllamaClient(
    private val baseUrl: String,
    private val httpClient: HttpClient = HttpClient.newHttpClient(),
    private val objectMapper: ObjectMapper = ObjectMapper()
) : ModelProvider {
    override val id: String = "ollama"

    override fun generate(model: String, prompt: String): String {
        return generateStream(model, prompt) { }
    }

    override fun generateStream(model: String, prompt: String, onChunk: (String) -> Unit): String {
        val body = objectMapper.writeValueAsString(CommitMessageRequest(model, prompt))
        val request = HttpRequest.newBuilder()
            .uri(URI.create("${baseUrl.trimEnd('/')}/api/generate"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofLines())
        if (response.statusCode() >= 400) {
            throw IOException("Ollama returned HTTP ${response.statusCode()}: ${response.body()}")
        }

        val raw = StringBuilder()
        response.body().use { lines ->
            lines.forEach { line ->
                val delta = extractOllamaDelta(line)
                if (delta.isNotEmpty()) {
                    raw.append(delta)
                    onChunk(delta)
                }
            }
        }
        return raw.toString().trim()
    }
}

private fun extractOllamaDelta(line: String): String {
    val trimmed = line.trim()
    if (trimmed.isEmpty()) {
        return ""
    }

    return try {
        val json = ObjectMapper().readTree(trimmed)
        json.path("response").asText("")
    } catch (_: Exception) {
        ""
    }
}
