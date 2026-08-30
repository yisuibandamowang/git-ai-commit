package com.gitai.commit

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class GitDiffFilterTest {
    @Test
    fun removesGeneratedIdeAndBuildArtifacts() {
        val diff = """
            diff --git a/idea-plugin/.intellijPlatform/sandbox/log/idea.log b/idea-plugin/.intellijPlatform/sandbox/log/idea.log
            index 111..222 100644
            --- a/idea-plugin/.intellijPlatform/sandbox/log/idea.log
            +++ b/idea-plugin/.intellijPlatform/sandbox/log/idea.log
            @@ -1 +1 @@
            -old shutdown log
            +new shutdown log
            diff --git a/idea-plugin/build/classes/kotlin/main/App.class b/idea-plugin/build/classes/kotlin/main/App.class
            index 333..444 100644
            --- a/idea-plugin/build/classes/kotlin/main/App.class
            +++ b/idea-plugin/build/classes/kotlin/main/App.class
            @@ -1 +1 @@
            -binary
            +binary
            diff --git a/idea-plugin/src/main/kotlin/com/gitai/commit/PromptBuilder.kt b/idea-plugin/src/main/kotlin/com/gitai/commit/PromptBuilder.kt
            index 555..666 100644
            --- a/idea-plugin/src/main/kotlin/com/gitai/commit/PromptBuilder.kt
            +++ b/idea-plugin/src/main/kotlin/com/gitai/commit/PromptBuilder.kt
            @@ -1 +1 @@
            -old prompt
            +new prompt
        """.trimIndent()

        val filtered = GitDiffFilter().filter(diff)

        assertContains(filtered, "PromptBuilder.kt")
        assertContains(filtered, "+new prompt")
        assertFalse(filtered.contains(".intellijPlatform"))
        assertFalse(filtered.contains("build/classes"))
        assertFalse(filtered.contains("shutdown log"))
    }

    @Test
    fun returnsEmptyWhenDiffContainsOnlyGeneratedArtifacts() {
        val diff = """
            diff --git a/idea-plugin/.gradle/fileHashes.bin b/idea-plugin/.gradle/fileHashes.bin
            index 111..222 100644
            --- a/idea-plugin/.gradle/fileHashes.bin
            +++ b/idea-plugin/.gradle/fileHashes.bin
            @@ -1 +1 @@
            -old
            +new
        """.trimIndent()

        assertEquals("", GitDiffFilter().filter(diff))
    }
}
