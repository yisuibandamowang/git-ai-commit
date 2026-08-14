package com.gitai.commit

import com.intellij.openapi.options.Configurable
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

class GitAiSettingsConfigurable : Configurable {
    private var baseUrlField = javax.swing.JTextField()
    private var modelField = javax.swing.JTextField()
    private var promptStyleField = javax.swing.JTextField()

    override fun getDisplayName(): String = "Git AI Commit"

    override fun createComponent(): JComponent = panel {
        row {
            label("Defaults are local-first and work with Ollama on your machine.")
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
    }

    override fun isModified(): Boolean {
        val settings = GitAiSettingsState.instance()
        return baseUrlField.text != settings.ollamaBaseUrl ||
            modelField.text != settings.model ||
            promptStyleField.text != settings.promptStyle
    }

    override fun apply() {
        val settings = GitAiSettingsState.instance()
        settings.ollamaBaseUrl = baseUrlField.text.trim()
        settings.model = modelField.text.trim()
        settings.promptStyle = promptStyleField.text.trim()
    }

    override fun reset() {
        val settings = GitAiSettingsState.instance()
        baseUrlField.text = settings.ollamaBaseUrl
        modelField.text = settings.model
        promptStyleField.text = settings.promptStyle
    }

    override fun disposeUIResources() {
        baseUrlField = javax.swing.JTextField()
        modelField = javax.swing.JTextField()
        promptStyleField = javax.swing.JTextField()
    }
}
