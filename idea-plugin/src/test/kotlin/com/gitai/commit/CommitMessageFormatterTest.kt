package com.gitai.commit

import kotlin.test.Test
import kotlin.test.assertEquals

class CommitMessageFormatterTest {
    @Test
    fun formatsVerboseJsonResponseIntoChineseSentence() {
        val formatted = CommitMessageFormatter.format(
            """
            ```json
            {
              "response": "This commit appears to be a significant update to a JetBrains IntelliJ IDEA plugin named \"Git AI Commit\". It introduces several key features and improvements: commit message formatting, diff analysis, Git diff filtering, model provider management, OpenAI-compatible client support, test coverage, and HTML reports."
            }
            ```
            """.trimIndent()
        )

        assertEquals("完善提交信息生成、分析和测试能力", formatted)
    }

    @Test
    fun ignoresStatusOnlyJsonResponse() {
        val formatted = CommitMessageFormatter.format(
            """
            ```json
            {
              "response": "success"
            }
            ```
            """.trimIndent()
        )

        assertEquals("", formatted)
    }
}
