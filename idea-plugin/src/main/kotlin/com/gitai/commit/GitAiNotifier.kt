package com.gitai.commit

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.project.Project

class GitAiNotifier {
    fun notify(project: Project, content: String, type: NotificationType) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Git AI Commit")
            .createNotification(content, type)
            .notify(project)
    }

    fun notifyLater(project: Project, content: String, type: NotificationType) {
        ApplicationManager.getApplication().invokeLater({
            notify(project, content, type)
        }, ModalityState.any())
    }
}
