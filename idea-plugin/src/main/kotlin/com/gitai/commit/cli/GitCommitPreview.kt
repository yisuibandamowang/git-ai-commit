package com.gitai.commit.cli

object GitCommitPreview {
    fun command(message: String): String {
        val paragraphs = message
            .trim()
            .split(Regex("""\r?\n\s*\r?\n"""))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (paragraphs.size <= 1) {
            return "git commit -m ${quoteSingleLine(paragraphs.firstOrNull().orEmpty())}"
        }

        val args = paragraphs.joinToString(" ") { paragraph -> "-m ${quoteParagraph(paragraph)}" }
        return "git commit $args"
    }

    fun quoteSingleLine(message: String): String {
        val normalized = message.replace(Regex("""[\r\n]+"""), " ").trim()
        val escaped = normalized
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("$", "\\$")
            .replace("`", "\\`")
        return "\"$escaped\""
    }

    private fun quoteParagraph(message: String): String {
        return if (message.contains('\n')) {
            val escaped = message
                .replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\n", "\\n")
            "\$'$escaped'"
        } else {
            quoteSingleLine(message)
        }
    }
}
