package com.gitai.commit.config

import com.gitai.commit.GitAiSettingsStateData

class GitAiConfig {
    var providerId: String? = null
    var model: String? = null
    var promptStyle: String? = null
    var ollamaBaseUrl: String? = null
    var openAiCompatibleBaseUrl: String? = null
    var openAiCompatibleApiKey: String? = null

    fun applyFrom(other: GitAiConfig) {
        other.providerId?.let { providerId = it }
        other.model?.let { model = it }
        other.promptStyle?.let { promptStyle = it }
        other.ollamaBaseUrl?.let { ollamaBaseUrl = it }
        other.openAiCompatibleBaseUrl?.let { openAiCompatibleBaseUrl = it }
        other.openAiCompatibleApiKey?.let { openAiCompatibleApiKey = it }
    }

    fun toSettingsStateData(): GitAiSettingsStateData {
        val defaults = defaults()
        return GitAiSettingsStateData(
            providerId = providerId ?: defaults.providerId.orEmpty(),
            ollamaBaseUrl = ollamaBaseUrl ?: defaults.ollamaBaseUrl.orEmpty(),
            model = model ?: defaults.model.orEmpty(),
            promptStyle = promptStyle ?: defaults.promptStyle.orEmpty(),
            openAiCompatibleBaseUrl = openAiCompatibleBaseUrl ?: defaults.openAiCompatibleBaseUrl.orEmpty(),
            openAiCompatibleApiKey = openAiCompatibleApiKey ?: defaults.openAiCompatibleApiKey.orEmpty()
        )
    }

    companion object {
        fun defaults(): GitAiConfig = GitAiConfig().apply {
            providerId = "ollama"
            model = "qwen2.5-coder:7b"
            promptStyle = "conventional-commits"
            ollamaBaseUrl = "http://localhost:11434"
            openAiCompatibleBaseUrl = "https://api.openai.com/v1"
            openAiCompatibleApiKey = ""
        }
    }
}

fun GitAiSettingsStateData.toGitAiConfig(): GitAiConfig = GitAiConfig().apply {
    providerId = this@toGitAiConfig.providerId
    model = this@toGitAiConfig.model
    promptStyle = this@toGitAiConfig.promptStyle
    ollamaBaseUrl = this@toGitAiConfig.ollamaBaseUrl
    openAiCompatibleBaseUrl = this@toGitAiConfig.openAiCompatibleBaseUrl
    openAiCompatibleApiKey = this@toGitAiConfig.openAiCompatibleApiKey
}
