package com.gitai.commit.cli

import com.gitai.commit.CommitMessageGeneration
import com.gitai.commit.config.ConfigScope
import com.gitai.commit.config.GitAiConfig
import com.gitai.commit.config.GitAiConfigStore
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class GitAiCommitIntegrationTest {
    @Test
    fun commitCommandPassesMergedConfigToGenerator() {
        val homeDir = Files.createTempDirectory("git-ai-commit-home")
        val repoRoot = Files.createTempDirectory("git-ai-commit-repo")
        val store = GitAiConfigStore(homeDir)
        store.save(ConfigScope.GLOBAL, null, GitAiConfig().apply {
            providerId = "ollama"
            model = "qwen2.5-coder:7b"
        })
        store.save(ConfigScope.PROJECT, repoRoot, GitAiConfig().apply {
            providerId = "deepseek"
            openAiCompatibleApiKey = "sk-test"
        })

        var seenConfig: GitAiConfig? = null
        val stdout = ByteArrayOutputStream()
        val cli = GitAiCommitCli(
            configStore = store,
            generateMessage = { config, _ ->
                seenConfig = config
                CommitMessageGeneration.Success("feat: use project config")
            },
            repoFinder = { _: Path -> repoRoot },
            stdout = PrintStream(stdout),
            stderr = PrintStream(ByteArrayOutputStream())
        )

        assertEquals(0, cli.run(arrayOf("commit")))

        val config = assertNotNull(seenConfig)
        assertEquals("deepseek", config.providerId)
        assertEquals("qwen2.5-coder:7b", config.model)
        assertEquals("sk-test", config.openAiCompatibleApiKey)
        assertContains(stdout.toString(Charsets.UTF_8), "git commit -m \"feat: use project config\"")
    }

    @Test
    fun readmeDocumentsCliAndDeepSeekUsage() {
        val readme = Files.readString(
            Paths.get(System.getProperty("user.dir")).resolve("../README.md").normalize()
        )

        assertContains(readme, "git-ai-commit config set providerId=deepseek")
        assertContains(readme, "git-ai-commit commit")
        assertContains(readme, "DeepSeek")
    }
}
