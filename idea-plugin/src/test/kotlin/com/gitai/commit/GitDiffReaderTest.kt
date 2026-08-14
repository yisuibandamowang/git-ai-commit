package com.gitai.commit

import java.io.File
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class GitDiffReaderTest {
    @Test
    fun usesWorkspaceDiffWhenNothingIsStaged() {
        val repo = createTempDir(prefix = "git-ai-commit-test")
        runCommand(repo, "git", "init")
        runCommand(repo, "git", "config", "user.email", "test@example.com")
        runCommand(repo, "git", "config", "user.name", "Test User")
        File(repo, "file.txt").writeText("hello\n")
        runCommand(repo, "git", "add", "file.txt")
        runCommand(repo, "git", "commit", "-m", "init")
        File(repo, "file.txt").writeText("hello world\n")

        val diff = GitDiffReader().readDiff(repo.absolutePath)
        assertContains(diff, "diff --git")
        assertContains(diff, "hello world")
    }

    @Test
    fun prefersStagedDiffWhenAvailable() {
        val repo = createTempDir(prefix = "git-ai-commit-staged")
        runCommand(repo, "git", "init")
        runCommand(repo, "git", "config", "user.email", "test@example.com")
        runCommand(repo, "git", "config", "user.name", "Test User")
        File(repo, "file.txt").writeText("hello\n")
        runCommand(repo, "git", "add", "file.txt")

        val diff = GitDiffReader().readDiff(repo.absolutePath)
        assertContains(diff, "diff --git")
        assertContains(diff, "file.txt")
    }

    private fun runCommand(dir: File, vararg command: String) {
        val process = ProcessBuilder(*command)
            .directory(dir)
            .redirectErrorStream(true)
            .start()
        val exit = process.waitFor()
        assertEquals(0, exit)
    }
}
