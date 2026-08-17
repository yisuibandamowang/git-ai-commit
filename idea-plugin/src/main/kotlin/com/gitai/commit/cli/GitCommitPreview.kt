package com.gitai.commit.cli

object GitCommitPreview {
    fun command(message: String): String = "git commit -m ${quote(message)}"

    fun quote(message: String): String {
        val normalized = message.replace(Regex("""[\r\n]+"""), " ").trim()
        val escaped = normalized
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("$", "\\$")
            .replace("`", "\\`")
        return "\"$escaped\""
    }
}
