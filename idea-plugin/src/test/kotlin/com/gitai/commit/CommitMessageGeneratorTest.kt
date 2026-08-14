package com.gitai.commit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertContains
import kotlin.test.assertIs

class CommitMessageGeneratorTest {
    @Test
    fun generatesCommitMessageFromDiff() {
        var called = false
        val generator = CommitMessageGenerator(
            diffProvider = { path ->
                called = true
                assertEquals("/repo", path)
                "diff --git a/app.go b/app.go"
            },
            settingsProvider = {
                GitAiSettingsStateData(
                    providerId = "ollama",
                    ollamaBaseUrl = "http://localhost:11434",
                    model = "qwen2.5-coder:7b",
                    promptStyle = "conventional-commits",
                    openAiCompatibleBaseUrl = "https://api.openai.com/v1",
                    openAiCompatibleApiKey = ""
                )
            },
            providerRegistry = ModelProviderRegistry(
                listOf(
                    object : ModelProviderFactory {
                        override val id: String = "ollama"
                        override fun create(settings: GitAiSettingsStateData): ModelProvider =
                            object : ModelProvider {
                                override val id: String = "ollama"
                                override fun generate(model: String, prompt: String): String {
                                    assertEquals("qwen2.5-coder:7b", model)
                                    assertContains(prompt, "diff --git a/app.go b/app.go")
                                    assertContains(prompt, "conventional-commits")
                                    assertContains(prompt, "结构化变更摘要")
                                    return "feat: add app commit"
                                }
                            }
                    }
                )
            )
        )

        val result = generator.generate("/repo")
        assertEquals(true, called)
        val success = assertIs<CommitMessageGeneration.Success>(result)
        assertEquals("feat: add app commit", success.message)
    }

    @Test
    fun returnsEmptyDiffWhenNoChanges() {
        var clientCalled = false
        val generator = CommitMessageGenerator(
            diffProvider = { "" },
            settingsProvider = {
                GitAiSettingsStateData(
                    providerId = "ollama",
                    ollamaBaseUrl = "http://localhost:11434",
                    model = "qwen2.5-coder:7b",
                    promptStyle = "conventional-commits",
                    openAiCompatibleBaseUrl = "https://api.openai.com/v1",
                    openAiCompatibleApiKey = ""
                )
            },
            providerRegistry = ModelProviderRegistry(
                listOf(
                    object : ModelProviderFactory {
                        override val id: String = "ollama"
                        override fun create(settings: GitAiSettingsStateData): ModelProvider =
                            object : ModelProvider {
                                override val id: String = "ollama"
                                override fun generate(model: String, prompt: String): String {
                                    clientCalled = true
                                    error("should not be called")
                                }
                            }
                    }
                )
            )
        )

        val result = generator.generate("/repo")
        assertIs<CommitMessageGeneration.EmptyDiff>(result)
        assertEquals(false, clientCalled)
    }
}
