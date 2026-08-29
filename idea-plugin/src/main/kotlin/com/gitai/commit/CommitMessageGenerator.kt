package com.gitai.commit

import com.intellij.openapi.project.Project

sealed class CommitMessageGeneration {
    data class Success(val message: String) : CommitMessageGeneration()
    data object EmptyDiff : CommitMessageGeneration()
    data object EmptyResponse : CommitMessageGeneration()
}

class CommitMessageGenerator(
    private val diffProvider: (String?) -> String = { GitDiffReader().readDiff(it) },
    private val diffFilter: GitDiffFilter = GitDiffFilter(),
    private val promptBuilder: PromptBuilder = PromptBuilder(),
    private val settingsProvider: () -> GitAiSettingsStateData = { GitAiSettingsState.instance().state },
    private val providerRegistry: ModelProviderRegistry = ModelProviderRegistry()
) {
    fun generate(project: Project): CommitMessageGeneration = generate(project.basePath)

    fun generate(basePath: String?, onUpdate: (String) -> Unit = {}): CommitMessageGeneration {
        val settings = settingsProvider()
        val rawDiff = diffProvider(basePath)
        if (rawDiff.isBlank()) return CommitMessageGeneration.EmptyDiff

        val diff = diffFilter.filter(rawDiff)
        if (diff.isBlank()) return CommitMessageGeneration.EmptyDiff

        val prompt = promptBuilder.build(diff, settings.promptStyle, settings.messageStyle)
        val selection = providerRegistry.select(settings)
        var streamedRaw = ""
        val rawMessage = selection.provider.generateStream(selection.model, prompt) { chunk ->
            streamedRaw += chunk
            onUpdate(streamedRaw)
        }
        val message = CommitMessageFormatter.format(rawMessage.ifBlank { streamedRaw }, settings.messageStyle)
        if (message.isNotBlank()) {
            onUpdate(message)
        }

        return if (message.isBlank()) {
            CommitMessageGeneration.EmptyResponse
        } else {
            CommitMessageGeneration.Success(message)
        }
    }
}
