plugins {
    kotlin("jvm") version "2.2.20"
    application
    id("org.jetbrains.intellij.platform") version "2.18.1"
}

group = "com.gitai.commit"
version = "0.1.2"

kotlin {
    jvmToolchain(21)
}

application {
    applicationName = "git-ai-commit"
    mainClass.set("com.gitai.commit.cli.GitAiCommitCliKt")
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
        intellijDependencies()
    }
}

dependencies {
    intellijPlatform {
        local("/Applications/GoLand.app")
        javaCompiler()
    }
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    testImplementation(kotlin("test"))
}

intellijPlatform {
    pluginConfiguration {
        name = "Git AI Commit"
        version = project.version.toString()
        ideaVersion {
            sinceBuild = "242"
            untilBuild = "253.*"
        }
        description = "Generate commit messages from git diff using local or compatible model providers."
        changeNotes = "Add configurable model providers and structured diff prompting."
        vendor {
            name = "Codex"
        }
    }
}
