package com.gitai.commit

interface ModelProvider {
    val id: String
    fun generate(model: String, prompt: String): String
}

data class ProviderSelection(
    val provider: ModelProvider,
    val model: String
)

interface ModelProviderFactory {
    val id: String
    val defaultModel: String
        get() = ""

    fun create(settings: GitAiSettingsStateData): ModelProvider
}

class ModelProviderRegistry(
    private val factories: List<ModelProviderFactory> = listOf(
        OllamaProviderFactory(),
        DeepSeekProviderFactory(),
        OpenAiCompatibleProviderFactory()
    )
) {
    fun select(settings: GitAiSettingsStateData): ProviderSelection {
        val factory = factories.firstOrNull { it.id == settings.providerId }
            ?: factories.first { it.id == "ollama" }
        val model = settings.model.takeIf { it.isNotBlank() } ?: factory.defaultModel
        return ProviderSelection(factory.create(settings), model)
    }
}

class OllamaProviderFactory : ModelProviderFactory {
    override val id: String = "ollama"
    override val defaultModel: String = "qwen2.5-coder:7b"

    override fun create(settings: GitAiSettingsStateData): ModelProvider =
        OllamaClient(settings.ollamaBaseUrl)
}

class OpenAiCompatibleProviderFactory : ModelProviderFactory {
    override val id: String = "openai-compatible"

    override fun create(settings: GitAiSettingsStateData): ModelProvider =
        OpenAiCompatibleClient(settings.openAiCompatibleBaseUrl, settings.openAiCompatibleApiKey)
}
