package com.gitai.commit

import com.intellij.ide.AppLifecycleListener
import com.intellij.openapi.diagnostic.Logger

class GitAiAppLifecycleListener : AppLifecycleListener {
    override fun appStarted() {
        Logger.getInstance(GitAiAppLifecycleListener::class.java).info("Git AI Commit appStarted")
    }
}
