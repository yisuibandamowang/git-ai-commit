package com.gitai.commit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeProvider(
    override val id: String = "fake",
    private val chunks: List<String>
) : ModelProvider {
    override fun generate(model: String, prompt: String): String = chunks.joinToString("")

    override fun generateStream(model: String, prompt: String, onChunk: (String) -> Unit): String {
        val raw = StringBuilder()
        chunks.forEach { chunk ->
            raw.append(chunk)
            onChunk(chunk)
        }
        return raw.toString()
    }
}

private class FakeFactory(
    private val provider: ModelProvider
) : ModelProviderFactory {
    override val id: String = "fake"
    override val defaultModel: String = "fake-model"

    override fun create(settings: GitAiSettingsStateData): ModelProvider = provider
}

class CommitMessageGeneratorTest {
    @Test
    fun `generate forwards streamed chunks before returning the final message`() {
        val provider = FakeProvider(chunks = listOf("fix", "(plugin)", ": ", "支持", "流式"))
        val registry = ModelProviderRegistry(listOf(FakeFactory(provider)))
        val generator = CommitMessageGenerator(
            diffProvider = { "diff" },
            diffFilter = GitDiffFilter(),
            promptBuilder = PromptBuilder(),
            settingsProvider = {
                GitAiSettingsStateData(
                    providerId = "fake",
                    model = "",
                    promptStyle = "conventional-commits",
                    messageStyle = "short"
                )
            },
            providerRegistry = registry
        )

        val updates = mutableListOf<String>()
        val result = generator.generate("repo") { updates += it }

        assertEquals(
            listOf(
                "fix",
                "fix(plugin)",
                "fix(plugin): ",
                "fix(plugin): 支持",
                "fix(plugin): 支持流式",
                "fix(plugin): 支持流式"
            ),
            updates
        )
        assertTrue(result is CommitMessageGeneration.Success)
        if (result is CommitMessageGeneration.Success) {
            assertTrue(result.message.contains("支持流式"))
        }
    }
}
