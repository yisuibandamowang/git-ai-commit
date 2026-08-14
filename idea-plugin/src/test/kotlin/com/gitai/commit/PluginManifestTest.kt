package com.gitai.commit

import kotlin.test.Test
import kotlin.test.assertContains

class PluginManifestTest {
    @Test
    fun registersCommitPanelAction() {
        val xml = requireNotNull(javaClass.classLoader.getResource("META-INF/plugin.xml"))
            .readText()
        assertContains(xml, "Vcs.MessageActionGroup")
        assertContains(xml, "CommitMessageAssistantAction")
    }
}
