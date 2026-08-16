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
    fun create(settings: GitAiSettingsStateData): ModelProvider
}

class ModelProviderRegistry(
    private val factories: List<ModelProviderFactory> = listOf(
        OllamaProviderFactory(),
        OpenAiCompatibleProviderFactory()
    )
) {
    fun select(settings: GitAiSettingsStateData): ProviderSelection {
        val factory = factories.firstOrNull { it.id == settings.providerId }
            ?: factories.first { it.id == "ollama" }
        return ProviderSelection(factory.create(settings), settings.model)
    }
}

class OllamaProviderFactory : ModelProviderFactory {
    override val id: String = "ollama"

    override fun create(settings: GitAiSettingsStateData): ModelProvider =
        OllamaClient(settings.ollamaBaseUrl)
}

class OpenAiCompatibleProviderFactory : ModelProviderFactory {
    override val id: String = "openai-compatible"

    override fun create(settings: GitAiSettingsStateData): ModelProvider =
        OpenAiCompatibleClient(settings.openAiCompatibleBaseUrl, settings.openAiCompatibleApiKey)
}
