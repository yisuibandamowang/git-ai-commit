plugins {
    kotlin("jvm") version "2.2.20"
    id("org.jetbrains.intellij.platform") version "2.18.1"
}

group = "com.gitai.commit"
version = "0.1.1"

kotlin {
    jvmToolchain(21)
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
        description = "Generate commit messages from git diff using local Ollama models."
        changeNotes = "Add commit message toolbar action for the Commit tool window."
        vendor {
            name = "Codex"
        }
    }
}
