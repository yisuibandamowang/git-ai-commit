package com.gitai.commit

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import java.awt.Component
import java.awt.Container
import javax.swing.JComboBox

class ModelProviderRegistryTest {
    @Test
    fun deepseekProviderFallsBackToDeepSeekDefaultModelWhenModelIsBlank() {
        val registry = ModelProviderRegistry()

        val selection = registry.select(
            GitAiSettingsStateData(
                providerId = "deepseek",
                ollamaBaseUrl = "http://localhost:11434",
                model = "",
                promptStyle = "conventional-commits",
                openAiCompatibleBaseUrl = "https://api.openai.com/v1",
                openAiCompatibleApiKey = "sk-test"
            )
        )

        assertEquals("deepseek-v4-flash", selection.model)
        assertEquals("openai-compatible", selection.provider.id)
    }

    @Test
    fun settingsConfigurableIncludesDeepSeekInProviderSelector() {
        val component = GitAiSettingsConfigurable().createComponent()
        val comboBox = findFirstComboBox(component)

        assertContains(comboBox.items(), "deepseek")
    }

    private fun findFirstComboBox(component: Component): JComboBox<*> {
        if (component is JComboBox<*>) return component
        if (component is Container) {
            for (child in component.components) {
                runCatching { return findFirstComboBox(child) }
            }
        }
        error("Provider combo box not found")
    }

    private fun JComboBox<*>.items(): List<String> =
        (0 until itemCount).map { getItemAt(it).toString() }
}
