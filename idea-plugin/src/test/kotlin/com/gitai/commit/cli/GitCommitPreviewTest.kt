package com.gitai.commit.cli

import kotlin.test.Test
import kotlin.test.assertEquals

class GitCommitPreviewTest {
    @Test
    fun formatsMessageAsGitCommitCommand() {
        assertEquals(
            "git commit -m \"feat: add cli support\"",
            GitCommitPreview.command("feat: add cli support")
        )
    }

    @Test
    fun escapesDoubleQuotedShellArgument() {
        assertEquals(
            """git commit -m "feat: add \"cli\" \${'$'}PATH next"""",
            GitCommitPreview.command("""feat: add "cli" ${'$'}PATH
next""")
        )
    }

    @Test
    fun preservesDetailedMessagesWithMultipleCommitParagraphs() {
        assertEquals(
            """git commit -m "feat: 优化提交信息生成" -m ${'$'}'- 保持短格式\n- 支持详细格式'""",
            GitCommitPreview.command(
                """
                feat: 优化提交信息生成

                - 保持短格式
                - 支持详细格式
                """.trimIndent()
            )
        )
    }
}
