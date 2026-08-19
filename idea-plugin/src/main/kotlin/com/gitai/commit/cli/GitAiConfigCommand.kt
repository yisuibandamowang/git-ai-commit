package com.gitai.commit.cli

import com.gitai.commit.config.ConfigScope
import com.gitai.commit.config.GitAiConfig
import com.gitai.commit.config.GitAiConfigResolver
import com.gitai.commit.config.GitAiConfigStore
import java.io.PrintStream
import java.nio.file.Path

object GitAiConfigCommand {
    fun execute(
        command: CliCommand.Config,
        store: GitAiConfigStore,
        repoRoot: Path?,
        stdout: PrintStream,
        stderr: PrintStream
    ): Int = when (command) {
        is CliCommand.Config.Get -> handleGet(command, store, repoRoot, stdout, stderr)
        is CliCommand.Config.Set -> handleSet(command, store, repoRoot, stdout, stderr)
        is CliCommand.Config.ListAll -> handleList(command, store, repoRoot, stdout, stderr)
    }

    private fun handleGet(
        command: CliCommand.Config.Get,
        store: GitAiConfigStore,
        repoRoot: Path?,
        stdout: PrintStream,
        stderr: PrintStream
    ): Int {
        val config = loadConfig(command.scope, store, repoRoot, stderr) ?: return 1
        if (command.key.isNullOrBlank()) {
            printConfig(config, stdout)
        } else {
            stdout.println(readKey(config, command.key))
        }
        return 0
    }

    private fun handleSet(
        command: CliCommand.Config.Set,
        store: GitAiConfigStore,
        repoRoot: Path?,
        stdout: PrintStream,
        stderr: PrintStream
    ): Int {
        if (command.scope == ConfigScope.MERGED) {
            stderr.println("Merged config is read-only.")
            return 1
        }

        val targetRepoRoot = if (command.scope == ConfigScope.PROJECT) {
            repoRoot ?: run {
                stderr.println("Project config requires a git repository root.")
                return 1
            }
        } else {
            null
        }

        val config = store.load(command.scope, targetRepoRoot) ?: GitAiConfig()
        command.assignments.forEach { (key, value) -> writeKey(config, key, value) }
        store.save(command.scope, targetRepoRoot, config)
        stdout.println("Updated ${command.scope.name.lowercase()} config.")
        return 0
    }

    private fun handleList(
        command: CliCommand.Config.ListAll,
        store: GitAiConfigStore,
        repoRoot: Path?,
        stdout: PrintStream,
        stderr: PrintStream
    ): Int {
        val config = loadConfig(command.scope, store, repoRoot, stderr) ?: return 1
        printConfig(config, stdout)
        return 0
    }

    private fun loadConfig(
        scope: ConfigScope,
        store: GitAiConfigStore,
        repoRoot: Path?,
        stderr: PrintStream
    ): GitAiConfig? = when (scope) {
        ConfigScope.GLOBAL -> store.load(ConfigScope.GLOBAL, null) ?: GitAiConfig.defaults()
        ConfigScope.PROJECT -> {
            val root = repoRoot ?: run {
                stderr.println("Project config requires a git repository root.")
                return null
            }
            store.load(ConfigScope.PROJECT, root) ?: GitAiConfig.defaults()
        }
        ConfigScope.MERGED -> {
            val root = repoRoot ?: run {
                stderr.println("Merged config requires a git repository root.")
                return null
            }
            GitAiConfigResolver(store).loadMerged(root)
        }
    }

    private fun printConfig(config: GitAiConfig, stdout: PrintStream) {
        presentValues(config).forEach { (key, value) ->
            stdout.println("$key=$value")
        }
    }

    private fun presentValues(config: GitAiConfig): List<Pair<String, String>> = buildList {
        config.providerId?.let { add("providerId" to it) }
        config.model?.let { add("model" to it) }
        config.promptStyle?.let { add("promptStyle" to it) }
        config.ollamaBaseUrl?.let { add("ollamaBaseUrl" to it) }
        config.deepSeekBaseUrl?.let { add("deepSeekBaseUrl" to it) }
        config.aliyunBaseUrl?.let { add("aliyunBaseUrl" to it) }
        config.miniMaxBaseUrl?.let { add("miniMaxBaseUrl" to it) }
        config.kimiBaseUrl?.let { add("kimiBaseUrl" to it) }
        config.glmBaseUrl?.let { add("glmBaseUrl" to it) }
        config.openAiCompatibleBaseUrl?.let { add("openAiCompatibleBaseUrl" to it) }
        config.openAiCompatibleApiKey?.let { add("openAiCompatibleApiKey" to it) }
    }

    private fun readKey(config: GitAiConfig, key: String): String = when (key) {
        "providerId" -> config.providerId.orEmpty()
        "model" -> config.model.orEmpty()
        "promptStyle" -> config.promptStyle.orEmpty()
        "ollamaBaseUrl" -> config.ollamaBaseUrl.orEmpty()
        "deepSeekBaseUrl" -> config.deepSeekBaseUrl.orEmpty()
        "aliyunBaseUrl" -> config.aliyunBaseUrl.orEmpty()
        "miniMaxBaseUrl" -> config.miniMaxBaseUrl.orEmpty()
        "kimiBaseUrl" -> config.kimiBaseUrl.orEmpty()
        "glmBaseUrl" -> config.glmBaseUrl.orEmpty()
        "openAiCompatibleBaseUrl" -> config.openAiCompatibleBaseUrl.orEmpty()
        "openAiCompatibleApiKey" -> config.openAiCompatibleApiKey.orEmpty()
        else -> throw IllegalArgumentException("Unknown config key: $key")
    }

    private fun writeKey(config: GitAiConfig, key: String, value: String) {
        when (key) {
            "providerId" -> config.providerId = value
            "model" -> config.model = value
            "promptStyle" -> config.promptStyle = value
            "ollamaBaseUrl" -> config.ollamaBaseUrl = value
            "deepSeekBaseUrl" -> config.deepSeekBaseUrl = value
            "aliyunBaseUrl" -> config.aliyunBaseUrl = value
            "miniMaxBaseUrl" -> config.miniMaxBaseUrl = value
            "kimiBaseUrl" -> config.kimiBaseUrl = value
            "glmBaseUrl" -> config.glmBaseUrl = value
            "openAiCompatibleBaseUrl" -> config.openAiCompatibleBaseUrl = value
            "openAiCompatibleApiKey" -> config.openAiCompatibleApiKey = value
            else -> throw IllegalArgumentException("Unknown config key: $key")
        }
    }
}
