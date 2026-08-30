plugins {
    kotlin("jvm") version "2.2.20"
    application
    id("org.jetbrains.intellij.platform") version "2.18.1"
}

group = "com.gitai.commit"
version = "0.1.25"

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
        val goLandApp = file("/Applications/GoLand.app")
        if (goLandApp.exists()) {
            local(goLandApp)
        } else {
            intellijIdea("2025.3.5")
        }
        javaCompiler()
    }
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation(kotlin("stdlib"))
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

    signing {
        providers.environmentVariable("JETBRAINS_CERTIFICATE_CHAIN").orNull?.let {
            certificateChain.set(it)
        }
        providers.environmentVariable("JETBRAINS_PRIVATE_KEY").orNull?.let {
            privateKey.set(it)
        }
        providers.environmentVariable("JETBRAINS_PRIVATE_KEY_PASSWORD").orNull?.let {
            password.set(it)
        }
    }

    publishing {
        host.set("https://plugins.jetbrains.com")
        providers.environmentVariable("JETBRAINS_PUBLISH_TOKEN").orNull?.let {
            token.set(it)
        }
        channels.set(
            providers.environmentVariable("JETBRAINS_PUBLISH_CHANNELS").orNull
                ?.takeIf { value -> value.isNotBlank() }
                ?.split(',')
                ?.map(String::trim)
                ?.filter(String::isNotEmpty)
                ?.takeIf { it.isNotEmpty() }
                ?: listOf("default")
        )
    }
}
