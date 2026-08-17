package com.gitai.commit.config

import java.nio.file.Path

class GitAiConfigResolver(
    private val store: GitAiConfigStore = GitAiConfigStore()
) {
    fun loadMerged(repoRoot: Path): GitAiConfig {
        val merged = GitAiConfig.defaults()
        store.load(ConfigScope.GLOBAL, null)?.let { merged.applyFrom(it) }
        store.load(ConfigScope.PROJECT, repoRoot)?.let { merged.applyFrom(it) }
        return merged
    }
}
