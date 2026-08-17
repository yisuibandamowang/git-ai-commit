package com.gitai.commit.cli

import com.gitai.commit.CommitMessageGeneration
import com.gitai.commit.CommitMessageGenerator
import com.gitai.commit.config.ConfigScope
import com.gitai.commit.config.GitAiConfig
import com.gitai.commit.config.GitAiConfigResolver
import com.gitai.commit.config.GitAiConfigStore
import java.io.PrintStream
import java.nio.file.Path
import java.nio.file.Paths

class GitAiCommitCli(
    private val configStore: GitAiConfigStore = GitAiConfigStore(),
    private val generateMessage: (GitAiConfig, Path) -> CommitMessageGeneration = ::defaultGenerateMessage,
    private val repoFinder: (Path) -> Path? = ::defaultRepoFinder,
    private val cwd: Path = Paths.get("").toAbsolutePath(),
    private val stdout: PrintStream = System.out,
    private val stderr: PrintStream = System.err
) {
    private val parser = CliCommandParser()

    fun run(args: Array<String>): Int {
        return try {
            when (val command = parser.parse(args)) {
                CliCommand.Help -> {
                    printUsage(stdout)
                    0
                }
                CliCommand.Commit -> GitAiCommitCommand.execute(
                    repoRoot = repoFinder(cwd),
                    generateMessage = generateMessage,
                    loadMergedConfig = { GitAiConfigResolver(configStore).loadMerged(it) },
                    stdout = stdout,
                    stderr = stderr
                )
                is CliCommand.Config -> {
                    val repoRoot = when (command) {
                        is CliCommand.Config.Get -> if (command.scope == ConfigScope.GLOBAL) null else repoFinder(cwd)
                        is CliCommand.Config.Set -> if (command.scope == ConfigScope.GLOBAL) null else repoFinder(cwd)
                        is CliCommand.Config.ListAll -> if (command.scope == ConfigScope.GLOBAL) null else repoFinder(cwd)
                    }
                    GitAiConfigCommand.execute(command, configStore, repoRoot, stdout, stderr)
                }
            }
        } catch (t: IllegalArgumentException) {
            stderr.println(t.message ?: "Invalid command.")
            1
        }
    }

    private fun printUsage(out: PrintStream) {
        out.println("git-ai-commit commit")
        out.println("git-ai-commit config get [key] [--scope global|project|merged]")
        out.println("git-ai-commit config set key=value [key=value ...] [--scope global|project]")
        out.println("git-ai-commit config list [--scope global|project|merged]")
    }

    companion object {
        private fun defaultGenerateMessage(config: GitAiConfig, repoRoot: Path): CommitMessageGeneration {
            val generator = CommitMessageGenerator(
                settingsProvider = { config.toSettingsStateData() }
            )
            return generator.generate(repoRoot.toString())
        }

        private fun defaultRepoFinder(start: Path): Path? {
            val process = ProcessBuilder("git", "-C", start.toString(), "rev-parse", "--show-toplevel")
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().use { it.readText().trim() }
            return if (process.waitFor() == 0 && output.isNotBlank()) {
                Paths.get(output)
            } else {
                null
            }
        }
    }
}

fun main(args: Array<String>) {
    kotlin.system.exitProcess(GitAiCommitCli().run(args))
}
