package com.gitai.commit

import com.intellij.openapi.options.Configurable
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JPasswordField
import javax.swing.JTextField

class GitAiSettingsConfigurable : Configurable {
    private val providerField = JComboBox(arrayOf("ollama", "deepseek", "openai-compatible"))
    private var baseUrlField = JTextField()
    private var deepSeekBaseUrlField = JTextField()
    private var modelField = JTextField()
    private var promptStyleField = JTextField()
    private var openAiBaseUrlField = JTextField()
    private var openAiApiKeyField = JPasswordField()

    override fun getDisplayName(): String = "Git AI Commit"

    override fun createComponent(): JComponent {
        val component = panel {
            row {
                label("Defaults are local-first and work with Ollama on your machine.")
            }
            row("Provider") {
                cell(providerField).align(AlignX.FILL)
            }
            row("Ollama base URL") {
                cell(baseUrlField).align(AlignX.FILL)
            }
            row("DeepSeek base URL") {
                cell(deepSeekBaseUrlField).align(AlignX.FILL)
            }
            row("OpenAI-compatible base URL") {
                cell(openAiBaseUrlField).align(AlignX.FILL)
            }
            row("API key") {
                cell(openAiApiKeyField).align(AlignX.FILL)
            }
            row("Model") {
                cell(modelField).align(AlignX.FILL)
            }
            row("Prompt style") {
                cell(promptStyleField).align(AlignX.FILL)
            }
        }
        providerField.addItemListener { syncFieldsForProvider() }
        syncFieldsForProvider()
        return component
    }

    private fun syncFieldsForProvider() {
        val provider = providerField.selectedItem?.toString().orEmpty()
        baseUrlField.isEnabled = provider == "ollama"
        deepSeekBaseUrlField.isEnabled = provider == "deepseek"
        openAiBaseUrlField.isEnabled = provider == "openai-compatible"
        syncModelForProvider(provider)
    }

    private fun syncModelForProvider(provider: String) {
        val current = modelField.text.trim()
        val knownDefaults = setOf("qwen2.5-coder:7b", "deepseek-v4-flash")
        if (current.isNotEmpty() && current !in knownDefaults) return
        modelField.text = when (provider) {
            "deepseek" -> "deepseek-v4-flash"
            "ollama" -> "qwen2.5-coder:7b"
            else -> ""
        }
    }

    override fun isModified(): Boolean {
        val settings = GitAiSettingsState.instance()
        return providerField.selectedItem != settings.providerId ||
            baseUrlField.text != settings.ollamaBaseUrl ||
            deepSeekBaseUrlField.text != settings.deepSeekBaseUrl ||
            modelField.text != settings.model ||
            promptStyleField.text != settings.promptStyle ||
            openAiBaseUrlField.text != settings.openAiCompatibleBaseUrl ||
            openAiApiKeyField.password.concatToString() != settings.openAiCompatibleApiKey
    }

    override fun apply() {
        val settings = GitAiSettingsState.instance()
        settings.providerId = providerField.selectedItem?.toString().orEmpty()
        settings.ollamaBaseUrl = baseUrlField.text.trim()
        settings.deepSeekBaseUrl = deepSeekBaseUrlField.text.trim()
        settings.model = modelField.text.trim()
        settings.promptStyle = promptStyleField.text.trim()
        settings.openAiCompatibleBaseUrl = openAiBaseUrlField.text.trim()
        settings.openAiCompatibleApiKey = openAiApiKeyField.password.concatToString().trim()
    }

    override fun reset() {
        val settings = GitAiSettingsState.instance()
        providerField.selectedItem = settings.providerId
        baseUrlField.text = settings.ollamaBaseUrl
        deepSeekBaseUrlField.text = settings.deepSeekBaseUrl
        modelField.text = settings.model
        promptStyleField.text = settings.promptStyle
        openAiBaseUrlField.text = settings.openAiCompatibleBaseUrl
        openAiApiKeyField.text = settings.openAiCompatibleApiKey
        syncFieldsForProvider()
    }

    override fun disposeUIResources() {
        providerField.selectedItem = "ollama"
        baseUrlField = JTextField()
        deepSeekBaseUrlField = JTextField()
        modelField = JTextField()
        promptStyleField = JTextField()
        openAiBaseUrlField = JTextField()
        openAiApiKeyField = JPasswordField()
    }
}
