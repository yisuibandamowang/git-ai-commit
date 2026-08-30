package com.gitai.commit

class AliyunProviderFactory : ModelProviderFactory {
    override val id: String = "aliyun"
    override val defaultModel: String = "qwen-plus"

    override fun create(settings: GitAiSettingsStateData): ModelProvider =
        OpenAiCompatibleClient(
            settings.aliyunBaseUrl.ifBlank { ALIYUN_BASE_URL },
            settings.openAiCompatibleApiKey
        )

    companion object {
        const val ALIYUN_BASE_URL: String = "https://dashscope.aliyuncs.com/compatible-mode/v1"
    }
}

class MiniMaxProviderFactory : ModelProviderFactory {
    override val id: String = "minimax"
    override val defaultModel: String = "MiniMax-Text-01"

    override fun create(settings: GitAiSettingsStateData): ModelProvider =
        OpenAiCompatibleClient(
            settings.miniMaxBaseUrl.ifBlank { MINIMAX_BASE_URL },
            settings.openAiCompatibleApiKey
        )

    companion object {
        const val MINIMAX_BASE_URL: String = "https://api.minimaxi.com/v1"
    }
}

class KimiProviderFactory : ModelProviderFactory {
    override val id: String = "kimi"
    override val defaultModel: String = "kimi-k2.5"

    override fun create(settings: GitAiSettingsStateData): ModelProvider =
        OpenAiCompatibleClient(
            settings.kimiBaseUrl.ifBlank { KIMI_BASE_URL },
            settings.openAiCompatibleApiKey
        )

    companion object {
        const val KIMI_BASE_URL: String = "https://api.moonshot.cn/v1"
    }
}

class GlmProviderFactory : ModelProviderFactory {
    override val id: String = "glm"
    override val defaultModel: String = "glm-4-flash"

    override fun create(settings: GitAiSettingsStateData): ModelProvider =
        OpenAiCompatibleClient(
            settings.glmBaseUrl.ifBlank { GLM_BASE_URL },
            settings.openAiCompatibleApiKey
        )

    companion object {
        const val GLM_BASE_URL: String = "https://open.bigmodel.cn/api/paas/v4"
    }
}