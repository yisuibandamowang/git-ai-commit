package com.gitai.commit

import kotlin.test.Test
import kotlin.test.assertContains

class PromptBuilderTest {
    @Test
    fun buildsPromptWithDiffAndStyle() {
        val prompt = PromptBuilder().build("diff --git a/x b/x", "conventional-commits")
        assertContains(prompt, "diff --git a/x b/x")
        assertContains(prompt, "conventional-commits")
    }
}
