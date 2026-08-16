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
                                    assertContains(prompt, "只输出一句中文")
                                    assertContains(prompt, "结构化变更摘要")
                                    return "feat(core): 添加应用提交信息"
                                }
                            }
                    }
                )
            )
        )

        val result = generator.generate("/repo")
        assertEquals(true, called)
        val success = assertIs<CommitMessageGeneration.Success>(result)
        assertEquals("添加应用提交信息", success.message)
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

    @Test
    fun formatsVerboseJsonResponseIntoSingleChineseSentence() {
        val generator = CommitMessageGenerator(
            diffProvider = { "diff --git a/app.go b/app.go" },
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
                                    return """
                                        ```json
                                        {
                                          "response": "This commit appears to be a significant update to a JetBrains IntelliJ IDEA plugin named \"Git AI Commit\". It introduces several key features and improvements: commit message formatting, diff analysis, Git diff filtering, model provider management, OpenAI-compatible client support, test coverage, and HTML reports."
                                        }
                                        ```
                                    """.trimIndent()
                                }
                            }
                    }
                )
            )
        )

        val result = generator.generate("/repo")
        val success = assertIs<CommitMessageGeneration.Success>(result)
        assertEquals("完善提交信息生成、分析和测试能力", success.message)
    }
}
