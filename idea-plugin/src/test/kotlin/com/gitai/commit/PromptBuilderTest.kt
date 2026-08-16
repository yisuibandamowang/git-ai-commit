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
        assertContains(prompt, "只输出一句中文")
        assertContains(prompt, "第一行必须直接是最终提交信息")
        assertContains(prompt, "不要使用“核心功能”")
    }
}
