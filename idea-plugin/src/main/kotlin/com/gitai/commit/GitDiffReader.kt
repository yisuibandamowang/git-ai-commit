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

        return runGit(root, "diff")
    }

    private fun runGit(root: File, vararg args: String): String {
        val process = ProcessBuilder(listOf("git", "-C", root.absolutePath) + args)
            .redirectErrorStream(true)
            .start()
        return process.inputStream.bufferedReader().use { it.readText() }.trim()
    }
}
