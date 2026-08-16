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

        assertEquals("新增提交信息格式化、差异分析、Git diff 过滤、模型提供商管理、OpenAI 兼容客户端和测试覆盖", formatted)
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

    @Test
    fun replacesGenericPluginSubjectWithSpecificFeatureSummary() {
        val formatted = CommitMessageFormatter.format("feat: 添加 Git AI Commit 插件的核心功能")

        assertEquals("新增提交信息格式化、差异分析、Git diff 过滤、模型提供商管理和测试覆盖", formatted)
    }
}
