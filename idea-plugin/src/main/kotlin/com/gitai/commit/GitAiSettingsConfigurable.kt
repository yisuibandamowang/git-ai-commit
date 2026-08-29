package com.gitai.commit

import com.intellij.openapi.options.Configurable
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JPasswordField
import javax.swing.JTextField

class GitAiSettingsConfigurable : Configurable {
    private val providerRegistry = ModelProviderRegistry()
    private val providerField = JComboBox(providerRegistry.providerIds().toTypedArray())
    private var baseUrlField = JTextField()
    private var deepSeekBaseUrlField = JTextField()
    private var aliyunBaseUrlField = JTextField()
    private var miniMaxBaseUrlField = JTextField()
    private var kimiBaseUrlField = JTextField()
    private var glmBaseUrlField = JTextField()
    private var openAiBaseUrlField = JTextField()
    private var modelField = JTextField()
    private var promptStyleField = JTextField()
    private val messageStyleField = JComboBox(arrayOf("short", "detailed"))
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
            row("Aliyun base URL") {
                cell(aliyunBaseUrlField).align(AlignX.FILL)
            }
            row("MiniMax base URL") {
                cell(miniMaxBaseUrlField).align(AlignX.FILL)
            }
            row("Kimi base URL") {
                cell(kimiBaseUrlField).align(AlignX.FILL)
            }
            row("GLM base URL") {
                cell(glmBaseUrlField).align(AlignX.FILL)
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
            row("Message style") {
                cell(messageStyleField).align(AlignX.FILL)
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
        aliyunBaseUrlField.isEnabled = provider == "aliyun"
        miniMaxBaseUrlField.isEnabled = provider == "minimax"
        kimiBaseUrlField.isEnabled = provider == "kimi"
        glmBaseUrlField.isEnabled = provider == "glm"
        openAiBaseUrlField.isEnabled = provider == "openai-compatible"
        syncModelForProvider(provider)
    }

    private fun syncModelForProvider(provider: String) {
        val current = modelField.text.trim()
        if (current.isNotEmpty() && !providerRegistry.isDefaultModel(current)) return
        modelField.text = providerRegistry.defaultModelFor(provider)
    }

    override fun isModified(): Boolean {
        val settings = GitAiSettingsState.instance()
        return providerField.selectedItem != settings.providerId ||
            baseUrlField.text != settings.ollamaBaseUrl ||
            deepSeekBaseUrlField.text != settings.deepSeekBaseUrl ||
            aliyunBaseUrlField.text != settings.aliyunBaseUrl ||
            miniMaxBaseUrlField.text != settings.miniMaxBaseUrl ||
            kimiBaseUrlField.text != settings.kimiBaseUrl ||
            glmBaseUrlField.text != settings.glmBaseUrl ||
        modelField.text != settings.model ||
        promptStyleField.text != settings.promptStyle ||
            messageStyleField.selectedItem != settings.messageStyle ||
            openAiBaseUrlField.text != settings.openAiCompatibleBaseUrl ||
            openAiApiKeyField.password.concatToString() != settings.openAiCompatibleApiKey
    }

    override fun apply() {
        val settings = GitAiSettingsState.instance()
        settings.providerId = providerField.selectedItem?.toString().orEmpty()
        settings.ollamaBaseUrl = baseUrlField.text.trim()
        settings.deepSeekBaseUrl = deepSeekBaseUrlField.text.trim()
        settings.aliyunBaseUrl = aliyunBaseUrlField.text.trim()
        settings.miniMaxBaseUrl = miniMaxBaseUrlField.text.trim()
        settings.kimiBaseUrl = kimiBaseUrlField.text.trim()
        settings.glmBaseUrl = glmBaseUrlField.text.trim()
        settings.model = modelField.text.trim()
        settings.promptStyle = promptStyleField.text.trim()
        settings.messageStyle = messageStyleField.selectedItem?.toString().orEmpty()
        settings.openAiCompatibleBaseUrl = openAiBaseUrlField.text.trim()
        settings.openAiCompatibleApiKey = openAiApiKeyField.password.concatToString().trim()
    }

    override fun reset() {
        val settings = GitAiSettingsState.instance()
        providerField.selectedItem = settings.providerId
        baseUrlField.text = settings.ollamaBaseUrl
        deepSeekBaseUrlField.text = settings.deepSeekBaseUrl
        aliyunBaseUrlField.text = settings.aliyunBaseUrl
        miniMaxBaseUrlField.text = settings.miniMaxBaseUrl
        kimiBaseUrlField.text = settings.kimiBaseUrl
        glmBaseUrlField.text = settings.glmBaseUrl
        modelField.text = settings.model
        promptStyleField.text = settings.promptStyle
        messageStyleField.selectedItem = settings.messageStyle
        openAiBaseUrlField.text = settings.openAiCompatibleBaseUrl
        openAiApiKeyField.text = settings.openAiCompatibleApiKey
        syncFieldsForProvider()
    }

    override fun disposeUIResources() {
        providerField.selectedItem = "ollama"
        baseUrlField = JTextField()
        deepSeekBaseUrlField = JTextField()
        aliyunBaseUrlField = JTextField()
        miniMaxBaseUrlField = JTextField()
        kimiBaseUrlField = JTextField()
        glmBaseUrlField = JTextField()
        openAiBaseUrlField = JTextField()
        modelField = JTextField()
        promptStyleField = JTextField()
        messageStyleField.removeAllItems()
        messageStyleField.addItem("short")
        messageStyleField.addItem("detailed")
        openAiApiKeyField = JPasswordField()
    }
}
