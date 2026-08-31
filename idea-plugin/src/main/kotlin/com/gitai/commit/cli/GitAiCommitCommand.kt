package com.gitai.commit.cli

import com.gitai.commit.CommitMessageGeneration
import com.gitai.commit.config.GitAiConfig
import java.io.PrintStream
import java.nio.file.Path

object GitAiCommitCommand {
    fun execute(
        repoRoot: Path?,
        generateMessage: (GitAiConfig, Path, (String) -> Unit) -> CommitMessageGeneration,
        loadMergedConfig: (Path) -> GitAiConfig,
        messageStyleOverride: String? = null,
        stdout: PrintStream,
        stderr: PrintStream
    ): Int {
        val root = repoRoot ?: run {
            stderr.println("Not inside a git repository.")
            return 1
        }

        val config = loadMergedConfig(root)
        if (!messageStyleOverride.isNullOrBlank()) {
            config.messageStyle = messageStyleOverride
        }
        return when (val result = generateMessage(config, root) { partial ->
            stderr.println("生成中：$partial")
            stderr.flush()
        }) {
            CommitMessageGeneration.EmptyDiff -> {
                stderr.println("没有检测到 git diff，先修改文件再试。")
                1
            }
            CommitMessageGeneration.EmptyResponse -> {
                stderr.println("模型没有返回有效 commit message。")
                1
            }
            is CommitMessageGeneration.Success -> {
                stdout.println(GitCommitPreview.command(result.message))
                0
            }
        }
    }
}
