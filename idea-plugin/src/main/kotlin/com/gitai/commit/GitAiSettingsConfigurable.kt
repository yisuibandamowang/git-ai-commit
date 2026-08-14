package com.gitai.commit

import com.intellij.openapi.options.Configurable
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JPasswordField

class GitAiSettingsConfigurable : Configurable {
    private val providerField = JComboBox(arrayOf("ollama", "openai-compatible"))
    private var baseUrlField = javax.swing.JTextField()
    private var modelField = javax.swing.JTextField()
    private var promptStyleField = javax.swing.JTextField()
    private var openAiBaseUrlField = javax.swing.JTextField()
    private var openAiApiKeyField = JPasswordField()

    override fun getDisplayName(): String = "Git AI Commit"

    override fun createComponent(): JComponent = panel {
        row {
            label("Defaults are local-first and work with Ollama on your machine.")
        }
        row("Provider") {
            cell(providerField).align(AlignX.FILL)
        }
        row("Ollama base URL") {
            cell(baseUrlField).align(AlignX.FILL)
        }
        row("Model") {
            cell(modelField).align(AlignX.FILL)
        }
        row("Prompt style") {
            cell(promptStyleField).align(AlignX.FILL)
        }
        row("OpenAI-compatible base URL") {
            cell(openAiBaseUrlField).align(AlignX.FILL)
        }
        row("OpenAI-compatible API key") {
            cell(openAiApiKeyField).align(AlignX.FILL)
        }
    }

    override fun isModified(): Boolean {
        val settings = GitAiSettingsState.instance()
        return providerField.selectedItem != settings.providerId ||
            baseUrlField.text != settings.ollamaBaseUrl ||
            modelField.text != settings.model ||
            promptStyleField.text != settings.promptStyle ||
            openAiBaseUrlField.text != settings.openAiCompatibleBaseUrl ||
            openAiApiKeyField.password.concatToString() != settings.openAiCompatibleApiKey
    }

    override fun apply() {
        val settings = GitAiSettingsState.instance()
        settings.providerId = providerField.selectedItem?.toString().orEmpty()
        settings.ollamaBaseUrl = baseUrlField.text.trim()
        settings.model = modelField.text.trim()
        settings.promptStyle = promptStyleField.text.trim()
        settings.openAiCompatibleBaseUrl = openAiBaseUrlField.text.trim()
        settings.openAiCompatibleApiKey = openAiApiKeyField.password.concatToString().trim()
    }

    override fun reset() {
        val settings = GitAiSettingsState.instance()
        providerField.selectedItem = settings.providerId
        baseUrlField.text = settings.ollamaBaseUrl
        modelField.text = settings.model
        promptStyleField.text = settings.promptStyle
        openAiBaseUrlField.text = settings.openAiCompatibleBaseUrl
        openAiApiKeyField.text = settings.openAiCompatibleApiKey
    }

    override fun disposeUIResources() {
        providerField.selectedItem = "ollama"
        baseUrlField = javax.swing.JTextField()
        modelField = javax.swing.JTextField()
        promptStyleField = javax.swing.JTextField()
        openAiBaseUrlField = javax.swing.JTextField()
        openAiApiKeyField = JPasswordField()
    }
}
