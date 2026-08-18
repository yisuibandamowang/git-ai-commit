package com.gitai.commit

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service

data class GitAiSettingsStateData(
    var providerId: String = "ollama",
    var ollamaBaseUrl: String = "http://localhost:11434",
    var deepSeekBaseUrl: String = "https://api.deepseek.com",
    var model: String = "qwen2.5-coder:7b",
    var promptStyle: String = "conventional-commits",
    var openAiCompatibleBaseUrl: String = "https://api.openai.com/v1",
    var openAiCompatibleApiKey: String = ""
)

@State(name = "GitAiSettingsState", storages = [Storage("git-ai-commit.xml")])
class GitAiSettingsState : PersistentStateComponent<GitAiSettingsStateData> {
    private var state = GitAiSettingsStateData()

    override fun getState(): GitAiSettingsStateData = state

    override fun loadState(state: GitAiSettingsStateData) {
        this.state = state
    }

    var providerId: String
        get() = state.providerId
        set(value) { state.providerId = value }

    var ollamaBaseUrl: String
        get() = state.ollamaBaseUrl
        set(value) { state.ollamaBaseUrl = value }

    var deepSeekBaseUrl: String
        get() = state.deepSeekBaseUrl
        set(value) { state.deepSeekBaseUrl = value }

    var model: String
        get() = state.model
        set(value) { state.model = value }

    var promptStyle: String
        get() = state.promptStyle
        set(value) { state.promptStyle = value }

    var openAiCompatibleBaseUrl: String
        get() = state.openAiCompatibleBaseUrl
        set(value) { state.openAiCompatibleBaseUrl = value }

    var openAiCompatibleApiKey: String
        get() = state.openAiCompatibleApiKey
        set(value) { state.openAiCompatibleApiKey = value }

    companion object {
        fun instance(): GitAiSettingsState = ApplicationManager.getApplication().service()
    }
}
