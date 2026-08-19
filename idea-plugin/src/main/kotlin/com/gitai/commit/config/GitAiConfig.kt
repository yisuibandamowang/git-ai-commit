package com.gitai.commit.config

import com.gitai.commit.GitAiSettingsStateData

class GitAiConfig {
    var providerId: String? = null
    var model: String? = null
    var promptStyle: String? = null
    var ollamaBaseUrl: String? = null
    var deepSeekBaseUrl: String? = null
    var aliyunBaseUrl: String? = null
    var miniMaxBaseUrl: String? = null
    var kimiBaseUrl: String? = null
    var glmBaseUrl: String? = null
    var openAiCompatibleBaseUrl: String? = null
    var openAiCompatibleApiKey: String? = null

    fun applyFrom(other: GitAiConfig) {
        other.providerId?.let { providerId = it }
        other.model?.let { model = it }
        other.promptStyle?.let { promptStyle = it }
        other.ollamaBaseUrl?.let { ollamaBaseUrl = it }
        other.deepSeekBaseUrl?.let { deepSeekBaseUrl = it }
        other.aliyunBaseUrl?.let { aliyunBaseUrl = it }
        other.miniMaxBaseUrl?.let { miniMaxBaseUrl = it }
        other.kimiBaseUrl?.let { kimiBaseUrl = it }
        other.glmBaseUrl?.let { glmBaseUrl = it }
        other.openAiCompatibleBaseUrl?.let { openAiCompatibleBaseUrl = it }
        other.openAiCompatibleApiKey?.let { openAiCompatibleApiKey = it }
    }

    fun toSettingsStateData(): GitAiSettingsStateData {
        val defaults = defaults()
        return GitAiSettingsStateData(
            providerId = providerId ?: defaults.providerId.orEmpty(),
            ollamaBaseUrl = ollamaBaseUrl ?: defaults.ollamaBaseUrl.orEmpty(),
            deepSeekBaseUrl = deepSeekBaseUrl ?: defaults.deepSeekBaseUrl.orEmpty(),
            aliyunBaseUrl = aliyunBaseUrl ?: defaults.aliyunBaseUrl.orEmpty(),
            miniMaxBaseUrl = miniMaxBaseUrl ?: defaults.miniMaxBaseUrl.orEmpty(),
            kimiBaseUrl = kimiBaseUrl ?: defaults.kimiBaseUrl.orEmpty(),
            glmBaseUrl = glmBaseUrl ?: defaults.glmBaseUrl.orEmpty(),
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
            deepSeekBaseUrl = "https://api.deepseek.com"
            aliyunBaseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1"
            miniMaxBaseUrl = "https://api.minimaxi.com/v1"
            kimiBaseUrl = "https://api.moonshot.cn/v1"
            glmBaseUrl = "https://open.bigmodel.cn/api/paas/v4"
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
    deepSeekBaseUrl = this@toGitAiConfig.deepSeekBaseUrl
    aliyunBaseUrl = this@toGitAiConfig.aliyunBaseUrl
    miniMaxBaseUrl = this@toGitAiConfig.miniMaxBaseUrl
    kimiBaseUrl = this@toGitAiConfig.kimiBaseUrl
    glmBaseUrl = this@toGitAiConfig.glmBaseUrl
    openAiCompatibleBaseUrl = this@toGitAiConfig.openAiCompatibleBaseUrl
    openAiCompatibleApiKey = this@toGitAiConfig.openAiCompatibleApiKey
}
