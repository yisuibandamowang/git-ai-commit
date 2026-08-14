package com.gitai.commit

import com.intellij.openapi.project.Project

sealed class CommitMessageGeneration {
    data class Success(val message: String) : CommitMessageGeneration()
    data object EmptyDiff : CommitMessageGeneration()
    data object EmptyResponse : CommitMessageGeneration()
}

class CommitMessageGenerator(
    private val diffProvider: (String?) -> String = { GitDiffReader().readDiff(it) },
    private val promptBuilder: PromptBuilder = PromptBuilder(),
    private val settingsProvider: () -> GitAiSettingsStateData = { GitAiSettingsState.instance().state },
    private val clientFactory: (String) -> LanguageModelClient = { OllamaClient(it) }
) {
    fun generate(project: Project): CommitMessageGeneration = generate(project.basePath)

    fun generate(basePath: String?): CommitMessageGeneration {
        val settings = settingsProvider()
        val diff = diffProvider(basePath)
        if (diff.isBlank()) return CommitMessageGeneration.EmptyDiff

        val prompt = promptBuilder.build(diff, settings.promptStyle)
        val message = clientFactory(settings.ollamaBaseUrl)
            .generate(settings.model, prompt)
            .trim()

        return if (message.isBlank()) {
            CommitMessageGeneration.EmptyResponse
        } else {
            CommitMessageGeneration.Success(message)
        }
    }
}
