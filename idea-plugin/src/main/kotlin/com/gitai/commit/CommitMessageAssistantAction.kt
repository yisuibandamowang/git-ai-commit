package com.gitai.commit

import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.vcs.VcsDataKeys

class CommitMessageAssistantAction : DumbAwareAction() {
    private val generator = CommitMessageGenerator()
    private val notifier = GitAiNotifier()

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val hasCommitMessageTarget =
            e.getData(VcsDataKeys.COMMIT_MESSAGE_CONTROL) != null ||
                e.getData(VcsDataKeys.COMMIT_WORKFLOW_UI) != null ||
                e.getData(VcsDataKeys.COMMIT_MESSAGE_DOCUMENT) != null
        e.presentation.isEnabledAndVisible = e.project != null && hasCommitMessageTarget
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val commitMessageControl = e.getData(VcsDataKeys.COMMIT_MESSAGE_CONTROL)
        val commitWorkflowUi = e.getData(VcsDataKeys.COMMIT_WORKFLOW_UI)
        val commitMessageDocument = e.getData(VcsDataKeys.COMMIT_MESSAGE_DOCUMENT)

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Generate Commit Message", false) {
            override fun run(indicator: ProgressIndicator) {
                val result = try {
                    generator.generate(project.basePath) { message ->
                        ApplicationManager.getApplication().invokeLater({
                            when {
                                commitWorkflowUi != null -> {
                                    commitWorkflowUi.commitMessageUi.text = message
                                }
                                commitMessageControl != null -> {
                                    commitMessageControl.setCommitMessage(message)
                                }
                                commitMessageDocument != null -> {
                                    WriteCommandAction.runWriteCommandAction(project) {
                                        commitMessageDocument.setText(message)
                                    }
                                }
                            }
                        }, ModalityState.any())
                    }
                } catch (t: Throwable) {
                    notifier.notifyLater(project, "调用模型失败：${t.message}", NotificationType.ERROR)
                    return
                }

                when (result) {
                    CommitMessageGeneration.EmptyDiff ->
                        notifier.notifyLater(project, "没有检测到 git diff，先修改文件再试。", NotificationType.WARNING)
                    CommitMessageGeneration.EmptyResponse ->
                        notifier.notifyLater(project, "模型没有返回有效 commit message。", NotificationType.ERROR)
                    is CommitMessageGeneration.Success ->
                        ApplicationManager.getApplication().invokeLater({
                            when {
                                commitWorkflowUi != null -> {
                                    commitWorkflowUi.commitMessageUi.text = result.message
                                    commitWorkflowUi.commitMessageUi.focus()
                                }
                                commitMessageControl != null -> {
                                    commitMessageControl.setCommitMessage(result.message)
                                }
                                commitMessageDocument != null -> {
                                    WriteCommandAction.runWriteCommandAction(project) {
                                        commitMessageDocument.setText(result.message)
                                    }
                                }
                            }
                            notifier.notify(project, "已生成并填入 commit message。", NotificationType.INFORMATION)
                        }, ModalityState.any())
                }
            }
        })
    }
}
