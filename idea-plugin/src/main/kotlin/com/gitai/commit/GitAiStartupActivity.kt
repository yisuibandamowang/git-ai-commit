package com.gitai.commit

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity

class GitAiStartupActivity : StartupActivity.DumbAware {
    override fun runActivity(project: Project) {
        Logger.getInstance(GitAiStartupActivity::class.java)
            .info("Git AI Commit loaded for project ${project.name}")
    }
}
