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

class GitAiCommitCliTest {
    @Test
    fun commitCommandPrintsPreviewLine() {
        val repoRoot = Files.createTempDirectory("git-ai-commit-repo")
        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()
        val cli = testCli(
            repoRoot = repoRoot,
            stdout = stdout,
            stderr = stderr,
            generateMessage = { _, _ -> CommitMessageGeneration.Success("feat: add cli support") }
        )

        val exitCode = cli.run(arrayOf("commit"))

        assertEquals(0, exitCode)
        assertEquals("git commit -m \"feat: add cli support\"\n", stdout.text())
        assertEquals("", stderr.text())
    }

    @Test
    fun configSetAndGetUseGlobalScopeByDefault() {
        val repoRoot = Files.createTempDirectory("git-ai-commit-repo")
        val homeDir = Files.createTempDirectory("git-ai-commit-home")
        val store = GitAiConfigStore(homeDir)
        val stdout = ByteArrayOutputStream()
        val cli = testCli(repoRoot = repoRoot, store = store, stdout = stdout)

        assertEquals(0, cli.run(arrayOf("config", "set", "providerId=deepseek")))
        assertEquals(0, cli.run(arrayOf("config", "get", "providerId")))

        val global = store.load(ConfigScope.GLOBAL, null)
        assertNotNull(global)
        assertEquals("deepseek", global.providerId)
        assertContains(stdout.text(), "deepseek\n")
    }

    @Test
    fun configSetCanWriteProjectScope() {
        val repoRoot = Files.createTempDirectory("git-ai-commit-repo")
        val homeDir = Files.createTempDirectory("git-ai-commit-home")
        val store = GitAiConfigStore(homeDir)
        val cli = testCli(repoRoot = repoRoot, store = store)

        assertEquals(0, cli.run(arrayOf("config", "set", "model=deepseek-v4-flash", "--scope", "project")))

        val project = store.load(ConfigScope.PROJECT, repoRoot)
        assertNotNull(project)
        assertEquals("deepseek-v4-flash", project.model)
    }

    private fun testCli(
        repoRoot: Path,
        store: GitAiConfigStore = GitAiConfigStore(Files.createTempDirectory("git-ai-commit-home")),
        stdout: ByteArrayOutputStream = ByteArrayOutputStream(),
        stderr: ByteArrayOutputStream = ByteArrayOutputStream(),
        generateMessage: (GitAiConfig, Path) -> CommitMessageGeneration = { _, _ ->
            CommitMessageGeneration.Success("feat: add cli support")
        }
    ): GitAiCommitCli =
        GitAiCommitCli(
            configStore = store,
            generateMessage = generateMessage,
            repoFinder = { _: Path -> repoRoot },
            stdout = PrintStream(stdout),
            stderr = PrintStream(stderr)
        )

    private fun ByteArrayOutputStream.text(): String = toString(Charsets.UTF_8)
}
