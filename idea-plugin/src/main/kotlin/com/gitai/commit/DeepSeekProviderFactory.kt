package com.gitai.commit

class DeepSeekProviderFactory : ModelProviderFactory {
    override val id: String = "deepseek"
    override val defaultModel: String = "deepseek-v4-flash"

    override fun create(settings: GitAiSettingsStateData): ModelProvider =
        OpenAiCompatibleClient(
            settings.deepSeekBaseUrl.ifBlank { DEEPSEEK_BASE_URL },
            settings.openAiCompatibleApiKey
        )

    companion object {
        const val DEEPSEEK_BASE_URL: String = "https://api.deepseek.com"
    }
}
