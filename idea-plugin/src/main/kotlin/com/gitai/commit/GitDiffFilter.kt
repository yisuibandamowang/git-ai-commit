package com.gitai.commit

class GitDiffFilter {
    fun filter(diff: String): String {
        val ignoredPathFragments = listOf(
            "/build/",
            "/.gradle/",
            "/.intellijPlatform/",
            "/build/reports/",
            "/build/tmp/"
        )
        val result = StringBuilder()
        var keepCurrentFile = true

        diff.lineSequence().forEach { line ->
            val header = Regex("""^diff --git a/(.*?) b/(.*)$""").find(line)
            if (header != null) {
                val path = header.groupValues[2]
                keepCurrentFile = ignoredPathFragments.none { path.contains(it) }
            }
            if (keepCurrentFile) {
                result.appendLine(line)
            }
        }

        return result.toString().trim()
    }
}
