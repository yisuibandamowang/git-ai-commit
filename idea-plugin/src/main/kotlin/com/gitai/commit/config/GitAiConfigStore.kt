package com.gitai.commit.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

enum class ConfigScope {
    GLOBAL,
    PROJECT,
    MERGED
}

class GitAiConfigStore(
    private val configHomeDir: Path = defaultConfigHomeDir(),
    private val objectMapper: ObjectMapper = ObjectMapper()
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
) {
    fun pathFor(scope: ConfigScope, repoRoot: Path?): Path = when (scope) {
        ConfigScope.GLOBAL -> configHomeDir.resolve("git-ai-commit/config.json")
        ConfigScope.PROJECT -> requireNotNull(repoRoot) {
            "Project config requires a git repository root."
        }.resolve(".git-ai-commit/config.json")
        ConfigScope.MERGED -> error("Merged config does not have a writable file path.")
    }

    fun load(scope: ConfigScope, repoRoot: Path?): GitAiConfig? {
        val path = pathFor(scope, repoRoot)
        if (!Files.exists(path)) return null
        return objectMapper.readValue(path.toFile(), GitAiConfig::class.java)
    }

    fun save(scope: ConfigScope, repoRoot: Path?, config: GitAiConfig) {
        val path = pathFor(scope, repoRoot)
        Files.createDirectories(path.parent)
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), config)
    }

    companion object {
        fun defaultConfigHomeDir(): Path {
            val osName = System.getProperty("os.name").lowercase()
            val appData = System.getenv("APPDATA")
            if (osName.contains("windows") && !appData.isNullOrBlank()) {
                return Paths.get(appData)
            }

            val xdgConfigHome = System.getenv("XDG_CONFIG_HOME")
            if (!xdgConfigHome.isNullOrBlank()) {
                return Paths.get(xdgConfigHome)
            }

            return Paths.get(System.getProperty("user.home"), ".config")
        }
    }
}
