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
}
