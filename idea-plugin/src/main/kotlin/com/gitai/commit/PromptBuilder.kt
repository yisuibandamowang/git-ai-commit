package com.gitai.commit

class PromptBuilder {
    fun build(diff: String, style: String): String = """
        You are a senior developer who writes concise git commit messages.
        Generate one conventional commit message in Chinese.
        Style: $style

        Rules:
        - Output only the final commit message.
        - Mention the most important change first.
        - Use feat, fix, refactor, perf, test, docs, or chore when appropriate.

        Diff:
        $diff
    """.trimIndent()
}
