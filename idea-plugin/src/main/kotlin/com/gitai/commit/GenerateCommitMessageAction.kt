package com.gitai.commit

import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import java.awt.datatransfer.StringSelection

class GenerateCommitMessageAction : AnAction() {
    private val generator = CommitMessageGenerator()
    private val notifier = GitAiNotifier()

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Generate Commit Message", false) {
            override fun run(indicator: ProgressIndicator) {
                val result = try {
                    generator.generate(project.basePath)
                } catch (t: Throwable) {
                    notifier.notifyLater(project, "调用模型失败：${t.message}", NotificationType.ERROR)
                    return
                }

                when (result) {
                    CommitMessageGeneration.EmptyDiff ->
                        notifier.notifyLater(project, "没有检测到 git diff，先修改文件再试。", NotificationType.WARNING)
                    CommitMessageGeneration.EmptyResponse ->
                        notifier.notifyLater(project, "模型没有返回有效 commit message。", NotificationType.ERROR)
                    is CommitMessageGeneration.Success -> {
                        ApplicationManager.getApplication().invokeLater({
                            CopyPasteManager.getInstance().setContents(StringSelection(result.message))
                        }, ModalityState.any())
                        notifier.notifyLater(project, result.message, NotificationType.INFORMATION)
                    }
                }
            }
        })
    }
}
