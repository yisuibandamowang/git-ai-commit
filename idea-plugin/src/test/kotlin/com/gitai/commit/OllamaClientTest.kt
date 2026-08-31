package com.gitai.commit

import com.fasterxml.jackson.databind.ObjectMapper
import java.net.Authenticator
import java.net.ProxySelector
import java.net.URI
import java.net.CookieHandler
import java.net.http.HttpClient
import java.net.http.HttpHeaders
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.ByteBuffer
import java.time.Duration
import java.util.Optional
import java.util.concurrent.Executor
import java.util.concurrent.Flow
import java.util.stream.Stream
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLParameters
import javax.net.ssl.SSLSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OllamaClientTest {
    @Test
    fun parsesOllamaResponseText() {
        val client = OllamaClient(
            "http://localhost:11434",
            FakeHttpClient(200, Stream.of("{\"response\":\"feat: add commit message\"}")),
            ObjectMapper()
        )
        assertEquals("feat: add commit message", client.generate("qwen2.5-coder:7b", "prompt"))
    }

    @Test
    fun generateStreamRequestsNativeStreaming() {
        val httpClient = FakeHttpClient(200, Stream.of("{\"response\":\"feat: stream\"}"))
        val client = OllamaClient(
            "http://localhost:11434",
            httpClient,
            ObjectMapper()
        )

        client.generateStream("qwen2.5-coder:7b", "prompt") { }

        assertEquals(true, ObjectMapper().readTree(httpClient.lastRequestBody).path("stream").asBoolean())
    }

    @Test
    fun failsOnHttpError() {
        val client = OllamaClient(
            "http://localhost:11434",
            FakeHttpClient(500, Stream.of("{\"error\":\"boom\"}")),
            ObjectMapper()
        )
        assertFailsWith<java.io.IOException> {
            client.generate("qwen2.5-coder:7b", "prompt")
        }
    }

    private class FakeHttpClient(
        private val code: Int,
        private val body: Stream<String>
    ) : HttpClient() {
        var lastRequestBody: String = ""
            private set

        override fun <T : Any?> send(request: HttpRequest, responseBodyHandler: HttpResponse.BodyHandler<T>): HttpResponse<T> {
            lastRequestBody = request.bodyPublisher()
                .map { publisher -> collectBody(publisher) }
                .orElse("")
            @Suppress("UNCHECKED_CAST")
            return object : HttpResponse<T> {
                override fun statusCode() = code
                override fun request(): HttpRequest = request
                override fun previousResponse(): Optional<HttpResponse<T>> = Optional.empty()
                override fun headers(): HttpHeaders = HttpHeaders.of(emptyMap()) { _, _ -> true }
                override fun body(): T = body as T
                override fun sslSession(): Optional<SSLSession> = Optional.empty()
                override fun uri(): URI = request.uri()
                override fun version(): HttpClient.Version = HttpClient.Version.HTTP_1_1
            }
        }

        override fun <T : Any?> sendAsync(request: HttpRequest, responseBodyHandler: HttpResponse.BodyHandler<T>) =
            throw UnsupportedOperationException()

        override fun <T : Any?> sendAsync(request: HttpRequest, responseBodyHandler: HttpResponse.BodyHandler<T>, pushPromiseHandler: HttpResponse.PushPromiseHandler<T>?) =
            throw UnsupportedOperationException()

        override fun cookieHandler(): Optional<CookieHandler> = Optional.empty()
        override fun connectTimeout(): Optional<Duration> = Optional.empty()
        override fun followRedirects(): HttpClient.Redirect = HttpClient.Redirect.NEVER
        override fun proxy(): Optional<ProxySelector> = Optional.empty()
        override fun sslContext(): SSLContext = throw UnsupportedOperationException()
        override fun sslParameters(): SSLParameters = throw UnsupportedOperationException()
        override fun authenticator(): Optional<Authenticator> = Optional.empty()
        override fun executor(): Optional<Executor> = Optional.empty()
        override fun version(): HttpClient.Version = HttpClient.Version.HTTP_1_1

        private fun collectBody(publisher: HttpRequest.BodyPublisher): String {
            val bytes = mutableListOf<Byte>()
            publisher.subscribe(object : Flow.Subscriber<ByteBuffer> {
                override fun onSubscribe(subscription: Flow.Subscription) {
                    subscription.request(Long.MAX_VALUE)
                }

                override fun onNext(item: ByteBuffer) {
                    while (item.hasRemaining()) {
                        bytes += item.get()
                    }
                }

                override fun onError(throwable: Throwable) {
                    throw throwable
                }

                override fun onComplete() = Unit
            })
            return bytes.toByteArray().decodeToString()
        }
    }
}
