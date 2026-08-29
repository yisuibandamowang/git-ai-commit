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
    var aliyunBaseUrl: String = "https://dashscope.aliyuncs.com/compatible-mode/v1",
    var miniMaxBaseUrl: String = "https://api.minimaxi.com/v1",
    var kimiBaseUrl: String = "https://api.moonshot.cn/v1",
    var glmBaseUrl: String = "https://open.bigmodel.cn/api/paas/v4",
    var model: String = "qwen2.5-coder:7b",
    var promptStyle: String = "conventional-commits",
    var messageStyle: String = "short",
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

    var aliyunBaseUrl: String
        get() = state.aliyunBaseUrl
        set(value) { state.aliyunBaseUrl = value }

    var miniMaxBaseUrl: String
        get() = state.miniMaxBaseUrl
        set(value) { state.miniMaxBaseUrl = value }

    var kimiBaseUrl: String
        get() = state.kimiBaseUrl
        set(value) { state.kimiBaseUrl = value }

    var glmBaseUrl: String
        get() = state.glmBaseUrl
        set(value) { state.glmBaseUrl = value }

    var model: String
        get() = state.model
        set(value) { state.model = value }

    var promptStyle: String
        get() = state.promptStyle
        set(value) { state.promptStyle = value }

    var messageStyle: String
        get() = state.messageStyle
        set(value) { state.messageStyle = value }

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
