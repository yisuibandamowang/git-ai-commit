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
                    ollamaBaseUrl = "http://localhost:11434",
                    model = "qwen2.5-coder:7b",
                    promptStyle = "conventional-commits"
                )
            },
            clientFactory = {
                fun interfaceMarker(): LanguageModelClient = LanguageModelClient { _, prompt ->
                    assertContains(prompt, "diff --git a/app.go b/app.go")
                    assertContains(prompt, "conventional-commits")
                    "feat: add app commit"
                }
                interfaceMarker()
            }
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
                    ollamaBaseUrl = "http://localhost:11434",
                    model = "qwen2.5-coder:7b",
                    promptStyle = "conventional-commits"
                )
            },
            clientFactory = {
                clientCalled = true
                LanguageModelClient { _, _ -> error("should not be called") }
            }
        )

        val result = generator.generate("/repo")
        assertIs<CommitMessageGeneration.EmptyDiff>(result)
        assertEquals(false, clientCalled)
    }
}
