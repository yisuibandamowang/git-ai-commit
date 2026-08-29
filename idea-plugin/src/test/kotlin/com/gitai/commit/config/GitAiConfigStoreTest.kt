package com.gitai.commit.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import java.nio.file.Files

class GitAiConfigStoreTest {
    @Test
    fun saveAndLoadGlobalConfigRoundTripsValues() {
        val homeDir = Files.createTempDirectory("git-ai-commit-home")
        val store = GitAiConfigStore(homeDir)
        val config = GitAiConfig().apply {
            providerId = "deepseek"
            model = "deepseek-v4-flash"
            promptStyle = "conventional-commits"
            messageStyle = "detailed"
            openAiCompatibleApiKey = "sk-test"
        }

        store.save(ConfigScope.GLOBAL, null, config)

        val loaded = store.load(ConfigScope.GLOBAL, null)
        assertNotNull(loaded)
        assertEquals("deepseek", loaded.providerId)
        assertEquals("deepseek-v4-flash", loaded.model)
        assertEquals("conventional-commits", loaded.promptStyle)
        assertEquals("detailed", loaded.messageStyle)
        assertEquals("sk-test", loaded.openAiCompatibleApiKey)
    }

    @Test
    fun projectConfigOverridesGlobalConfig() {
        val homeDir = Files.createTempDirectory("git-ai-commit-home")
        val repoRoot = Files.createTempDirectory("git-ai-commit-repo")
        Files.createDirectories(repoRoot.resolve(".git-ai-commit"))

        val store = GitAiConfigStore(homeDir)
        store.save(ConfigScope.GLOBAL, null, GitAiConfig().apply {
            providerId = "ollama"
            model = "qwen2.5-coder:7b"
        })
        store.save(ConfigScope.PROJECT, repoRoot, GitAiConfig().apply {
            providerId = "deepseek"
            messageStyle = "detailed"
            openAiCompatibleApiKey = "sk-test"
        })

        val merged = GitAiConfigResolver(store).loadMerged(repoRoot)
        assertEquals("deepseek", merged.providerId)
        assertEquals("qwen2.5-coder:7b", merged.model)
        assertEquals("detailed", merged.messageStyle)
        assertEquals("sk-test", merged.openAiCompatibleApiKey)
    }

    @Test
    fun pathForUsesGlobalHomeAndProjectRepoRoot() {
        val homeDir = Files.createTempDirectory("git-ai-commit-home")
        val repoRoot = Files.createTempDirectory("git-ai-commit-repo")
        val store = GitAiConfigStore(homeDir)

        assertEquals(
            homeDir.resolve("git-ai-commit/config.json"),
            store.pathFor(ConfigScope.GLOBAL, null)
        )
        assertEquals(
            repoRoot.resolve(".git-ai-commit/config.json"),
            store.pathFor(ConfigScope.PROJECT, repoRoot)
        )
    }
}
