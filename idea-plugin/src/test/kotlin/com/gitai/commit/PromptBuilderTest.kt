package com.gitai.commit

import kotlin.test.Test
import kotlin.test.assertContains

class PromptBuilderTest {
    @Test
    fun buildsPromptWithStructuredDiffSummaryAndStyle() {
        val prompt = PromptBuilder().build("diff --git a/x b/x", "conventional-commits")
        assertContains(prompt, "diff --git a/x b/x")
        assertContains(prompt, "conventional-commits")
        assertContains(prompt, "结构化变更摘要")
        assertContains(prompt, "先阅读结构化变更摘要")
        assertContains(prompt, "提交正文建议")
    }
}
