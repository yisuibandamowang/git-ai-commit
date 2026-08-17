package com.gitai.commit.cli

import com.gitai.commit.config.ConfigScope

sealed class CliCommand {
    data object Help : CliCommand()
    data object Commit : CliCommand()

    sealed class Config : CliCommand() {
        data class Get(val key: String?, val scope: ConfigScope) : Config()
        data class Set(val assignments: List<Pair<String, String>>, val scope: ConfigScope) : Config()
        data class ListAll(val scope: ConfigScope) : Config()
    }
}

class CliCommandParser {
    fun parse(args: Array<String>): CliCommand {
        if (args.isEmpty()) return CliCommand.Help
        return when (args[0]) {
            "commit" -> CliCommand.Commit
            "config" -> parseConfig(args.drop(1))
            "help", "--help", "-h" -> CliCommand.Help
            else -> throw IllegalArgumentException("Unknown command: ${args[0]}")
        }
    }

    private fun parseConfig(args: List<String>): CliCommand {
        if (args.isEmpty()) throw IllegalArgumentException("Missing config subcommand")
        return when (args[0]) {
            "get" -> parseConfigGet(args.drop(1))
            "set" -> parseConfigSet(args.drop(1))
            "list" -> parseConfigList(args.drop(1))
            else -> throw IllegalArgumentException("Unknown config subcommand: ${args[0]}")
        }
    }

    private fun parseConfigGet(args: List<String>): CliCommand.Config.Get {
        val scope = parseScope(args, defaultScope = ConfigScope.MERGED)
        val key = argsWithoutScope(args).firstOrNull()
        return CliCommand.Config.Get(key = key, scope = scope)
    }

    private fun parseConfigSet(args: List<String>): CliCommand.Config.Set {
        val scope = parseScope(args, defaultScope = ConfigScope.GLOBAL)
        val assignments = argsWithoutScope(args)
            .map {
                val parts = it.split("=", limit = 2)
                if (parts.size != 2 || parts[0].isBlank()) {
                    throw IllegalArgumentException("Config assignments must look like key=value: $it")
                }
                parts[0] to parts[1]
            }
        if (assignments.isEmpty()) {
            throw IllegalArgumentException("Missing key=value assignments")
        }
        return CliCommand.Config.Set(assignments = assignments, scope = scope)
    }

    private fun parseConfigList(args: List<String>): CliCommand.Config.ListAll {
        val scope = parseScope(args, defaultScope = ConfigScope.MERGED)
        return CliCommand.Config.ListAll(scope = scope)
    }

    private fun argsWithoutScope(args: List<String>): List<String> {
        val result = mutableListOf<String>()
        var skipNext = false
        args.forEach { arg ->
            if (skipNext) {
                skipNext = false
                return@forEach
            }
            when {
                arg == "--scope" -> skipNext = true
                arg.startsWith("--scope=") -> Unit
                else -> result += arg
            }
        }
        return result
    }

    private fun parseScope(args: List<String>, defaultScope: ConfigScope): ConfigScope {
        val scopeToken = args.firstOrNull { it == "--scope" || it.startsWith("--scope=") } ?: return defaultScope
        val rawScope = when {
            scopeToken.startsWith("--scope=") -> scopeToken.substringAfter('=')
            else -> args.getOrNull(args.indexOf(scopeToken) + 1)
        } ?: throw IllegalArgumentException("Missing value after --scope")

        return when (rawScope.lowercase()) {
            "global" -> ConfigScope.GLOBAL
            "project" -> ConfigScope.PROJECT
            "merged" -> ConfigScope.MERGED
            else -> throw IllegalArgumentException("Unknown scope: $rawScope")
        }
    }
}
