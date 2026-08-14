package com.gitai.commit

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener

class GitAiProjectListener : ProjectManagerListener {
    override fun projectOpened(project: Project) {
        Logger.getInstance(GitAiProjectListener::class.java)
            .info("Git AI Commit projectOpened: ${project.name}")
    }
}
