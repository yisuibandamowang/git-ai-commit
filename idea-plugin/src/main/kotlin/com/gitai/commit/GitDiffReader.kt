package com.gitai.commit

import com.intellij.openapi.project.Project
import java.io.File

class GitDiffReader {
    fun readDiff(project: Project): String = readDiff(project.basePath)

    fun readDiff(basePath: String?): String {
        val base = basePath ?: return ""
        val root = File(base)
        if (!root.exists()) return ""

        val staged = runGit(root, "diff", "--cached")
        if (staged.isNotBlank()) return staged

        val tracked = runGit(root, "diff")
        if (tracked.isNotBlank()) return tracked

        return runUntrackedDiff(root)
    }

    private fun runGit(root: File, vararg args: String): String {
        val process = ProcessBuilder(listOf("git", "-C", root.absolutePath) + args)
            .redirectErrorStream(true)
            .start()
        return process.inputStream.bufferedReader().use { it.readText() }.trim()
    }

    private fun runUntrackedDiff(root: File): String {
        val status = runGit(root, "status", "--porcelain=v1", "--untracked-files=all", "-z")
        if (status.isBlank()) return ""

        return status
            .split('\u0000')
            .asSequence()
            .mapNotNull { entry ->
                val trimmed = entry.trim()
                if (!trimmed.startsWith("?? ")) return@mapNotNull null
                trimmed.removePrefix("?? ").takeIf { it.isNotBlank() }
            }
            .map { path ->
                runGit(root, "diff", "--no-index", "--", "/dev/null", path)
            }
            .filter { it.isNotBlank() }
            .joinToString("\n")
            .trim()
    }
}
