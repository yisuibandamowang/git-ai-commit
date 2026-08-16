package com.gitai.commit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertContains
import kotlin.test.assertFalse
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
                                    assertContains(prompt, "Conventional Commit 格式")
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
        assertEquals("feat(core): 添加应用提交信息", success.message)
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
    fun filtersGeneratedArtifactsBeforeBuildingPrompt() {
        var clientCalled = false
        val generator = CommitMessageGenerator(
            diffProvider = {
                """
                diff --git a/idea-plugin/.intellijPlatform/sandbox/log/idea.log b/idea-plugin/.intellijPlatform/sandbox/log/idea.log
                index 111..222 100644
                --- a/idea-plugin/.intellijPlatform/sandbox/log/idea.log
                +++ b/idea-plugin/.intellijPlatform/sandbox/log/idea.log
                @@ -1 +1 @@
                -old shutdown log
                +new shutdown log
                diff --git a/idea-plugin/src/main/kotlin/com/gitai/commit/PromptBuilder.kt b/idea-plugin/src/main/kotlin/com/gitai/commit/PromptBuilder.kt
                index 333..444 100644
                --- a/idea-plugin/src/main/kotlin/com/gitai/commit/PromptBuilder.kt
                +++ b/idea-plugin/src/main/kotlin/com/gitai/commit/PromptBuilder.kt
                @@ -1 +1 @@
                -old prompt
                +new prompt
                """.trimIndent()
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
                                    clientCalled = true
                                    assertContains(prompt, "PromptBuilder.kt")
                                    assertContains(prompt, "+new prompt")
                                    assertFalse(prompt.contains(".intellijPlatform"))
                                    assertFalse(prompt.contains("shutdown log"))
                                    return "优化提交信息提示词"
                                }
                            }
                    }
                )
            )
        )

        val result = generator.generate("/repo")
        val success = assertIs<CommitMessageGeneration.Success>(result)
        assertEquals(true, clientCalled)
        assertEquals("feat: 优化提交信息提示词", success.message)
    }

    @Test
    fun returnsEmptyDiffWhenOnlyGeneratedArtifactsChanged() {
        var clientCalled = false
        val generator = CommitMessageGenerator(
            diffProvider = {
                """
                diff --git a/idea-plugin/.intellijPlatform/sandbox/log/idea.log b/idea-plugin/.intellijPlatform/sandbox/log/idea.log
                index 111..222 100644
                --- a/idea-plugin/.intellijPlatform/sandbox/log/idea.log
                +++ b/idea-plugin/.intellijPlatform/sandbox/log/idea.log
                @@ -1 +1 @@
                -old shutdown log
                +new shutdown log
                """.trimIndent()
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
        assertEquals("feat: 新增提交信息格式化、差异分析、Git diff 过滤、模型提供商管理、OpenAI 兼容客户端和测试覆盖", success.message)
    }
}
