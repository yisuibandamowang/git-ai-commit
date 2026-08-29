package com.gitai.commit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertContains

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

        assertEquals("feat: 新增提交信息格式化、差异分析、Git diff 过滤、模型提供商管理、OpenAI 兼容客户端和测试覆盖", formatted)
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

        assertEquals("feat: 新增提交信息格式化、差异分析、Git diff 过滤、模型提供商管理和测试覆盖", formatted)
    }

    @Test
    fun translatesSpecificEnglishConventionalCommitSubjectIntoChinese() {
        val formatted = CommitMessageFormatter.format(
            "feat(idea-plugin): add log files for indexing diagnostics and open-telemetry metrics"
        )

        assertEquals("feat(idea-plugin): 添加索引诊断日志和OpenTelemetry指标文件", formatted)
    }

    @Test
    fun addsDefaultConventionalPrefixWhenModelReturnsChineseOnly() {
        val formatted = CommitMessageFormatter.format("优化提交信息提示词")

        assertEquals("feat: 优化提交信息提示词", formatted)
    }

    @Test
    fun preservesExistingConventionalPrefix() {
        val formatted = CommitMessageFormatter.format("fix(core): 修复提交信息为空的问题")

        assertEquals("fix(core): 修复提交信息为空的问题", formatted)
    }

    @Test
    fun formatsDetailedOutputAsSubjectPlusBullets() {
        val formatted = CommitMessageFormatter.format(
            "feat: 优化提交信息生成\n\n- 保持短格式\n- 支持详细格式",
            "detailed"
        )

        assertContains(formatted, "\n\n- ")
    }
}
