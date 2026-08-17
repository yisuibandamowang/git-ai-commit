# CLI and DeepSeek Support Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a terminal CLI with config commands and commit preview output, while letting the IDE plugin switch cleanly to DeepSeek or other model providers.

**Architecture:** Keep the current IntelliJ plugin module as the shared Kotlin runtime. Add a thin CLI entrypoint, a small config store/resolver layer, and a DeepSeek provider factory that reuses the existing OpenAI-compatible client. The IDE settings page keeps its current role, but reads and writes the same provider IDs and config fields as the CLI.

**Tech Stack:** Kotlin 2.2, Gradle IntelliJ Platform plugin, Jackson databind, Kotlin test, standard git CLI.

## Global Constraints

- Do not auto-run `git commit`.
- Do not build an interactive TUI or editor flow.
- Do not introduce cloud sync or remote config storage.
- Do not redesign the IDE UI beyond what is needed to expose the same providers.
- Global config by default. Project-level config as an override.
- Generate a commit message and print a shell-ready `git commit -m "..."` line for manual editing.
- Reuse the existing generation logic instead of duplicating it.
- Keep the IDEA plugin on the same provider/model semantics.
- Storage locations: `$XDG_CONFIG_HOME/git-ai-commit/config.json` when `XDG_CONFIG_HOME` is set, otherwise `~/.config/git-ai-commit/config.json` on Unix-like systems, and `%APPDATA%\\git-ai-commit\\config.json` on Windows.
- Project config location: `<repo>/.git-ai-commit/config.json`.
- Shared config fields: `providerId`, `model`, `promptStyle`, `ollamaBaseUrl`, `openAiCompatibleBaseUrl`, `openAiCompatibleApiKey`.

---

### Task 1: Shared config model and file store

**Files:**
- Create: `idea-plugin/src/main/kotlin/com/gitai/commit/config/GitAiConfig.kt`
- Create: `idea-plugin/src/main/kotlin/com/gitai/commit/config/GitAiConfigStore.kt`
- Create: `idea-plugin/src/main/kotlin/com/gitai/commit/config/GitAiConfigResolver.kt`
- Create: `idea-plugin/src/test/kotlin/com/gitai/commit/config/GitAiConfigStoreTest.kt`

**Interfaces:**
- Consumes: `jackson-databind`, `java.nio.file.Path`
- Produces: `GitAiConfig`, `ConfigScope`, `GitAiConfigStore.load/save`, `GitAiConfigResolver.loadMerged`

- [ ] **Step 1: Write the failing tests**

```kotlin
@Test
fun projectConfigOverridesGlobalConfig() {
    val repoRoot = tempDir.resolve("repo")
    val globalHome = tempDir.resolve("global-home")
    val store = GitAiConfigStore(globalHome)
    val global = GitAiConfig().apply {
        providerId = "ollama"
        model = "qwen2.5-coder:7b"
    }
    val project = GitAiConfig().apply {
        providerId = "deepseek"
        openAiCompatibleApiKey = "sk-test"
    }
    store.save(ConfigScope.GLOBAL, null, global)
    store.save(ConfigScope.PROJECT, repoRoot, project)

    val resolver = GitAiConfigResolver(store)
    val merged = resolver.loadMerged(repoRoot)

    assertEquals("deepseek", merged.providerId)
    assertEquals("qwen2.5-coder:7b", merged.model)
    assertEquals("sk-test", merged.openAiCompatibleApiKey)
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd idea-plugin && ./gradlew test --tests com.gitai.commit.config.GitAiConfigStoreTest`
Expected: FAIL because the config classes and resolver do not exist yet.

- [ ] **Step 3: Write minimal implementation**

```kotlin
class GitAiConfig {
    var providerId: String = "ollama"
    var model: String = "qwen2.5-coder:7b"
    var promptStyle: String = "conventional-commits"
    var ollamaBaseUrl: String = "http://localhost:11434"
    var openAiCompatibleBaseUrl: String = "https://api.openai.com/v1"
    var openAiCompatibleApiKey: String = ""
}
```

Add `GitAiConfigStore` with `load(scope, repoRoot)`, `save(scope, repoRoot, config)`, and `pathFor(scope, repoRoot)` that reads and writes plain JSON with `ObjectMapper`.
Add `GitAiConfigResolver.loadMerged(repoRoot)` that merges built-in defaults, global config, then project config.

- [ ] **Step 4: Run test to verify it passes**

Run: `cd idea-plugin && ./gradlew test --tests com.gitai.commit.config.GitAiConfigStoreTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add idea-plugin/src/main/kotlin/com/gitai/commit/config idea-plugin/src/test/kotlin/com/gitai/commit/config
git commit -m "feat: add shared config store"
```

### Task 2: DeepSeek provider and IDE provider selection

**Files:**
- Modify: `idea-plugin/src/main/kotlin/com/gitai/commit/ModelProvider.kt`
- Modify: `idea-plugin/src/main/kotlin/com/gitai/commit/GitAiSettingsState.kt`
- Modify: `idea-plugin/src/main/kotlin/com/gitai/commit/GitAiSettingsConfigurable.kt`
- Create: `idea-plugin/src/main/kotlin/com/gitai/commit/DeepSeekProviderFactory.kt`
- Create: `idea-plugin/src/test/kotlin/com/gitai/commit/ModelProviderRegistryTest.kt`

**Interfaces:**
- Consumes: `GitAiSettingsStateData`, `OpenAiCompatibleClient`
- Produces: `DeepSeekProviderFactory`, provider id `deepseek`, updated settings mapping

- [ ] **Step 1: Write the failing tests**

```kotlin
@Test
fun deepseekProviderUsesDeepSeekDefaults() {
    val registry = ModelProviderRegistry(listOf(DeepSeekProviderFactory()))
    val selection = registry.select(
        GitAiSettingsStateData(
            providerId = "deepseek",
            model = "",
            promptStyle = "conventional-commits",
            openAiCompatibleApiKey = "sk-test"
        )
    )

    assertEquals("deepseek-v4-flash", selection.model)
}
```

Add a second test that `GitAiSettingsConfigurable` includes `deepseek` in the provider combo box and round-trips it through `apply()` / `reset()`.

- [ ] **Step 2: Run test to verify it fails**

Run: `cd idea-plugin && ./gradlew test --tests com.gitai.commit.ModelProviderRegistryTest`
Expected: FAIL because `deepseek` is not wired yet.

- [ ] **Step 3: Write minimal implementation**

```kotlin
class DeepSeekProviderFactory : ModelProviderFactory {
    override val id: String = "deepseek"

    override fun create(settings: GitAiSettingsStateData): ModelProvider =
        OpenAiCompatibleClient("https://api.deepseek.com", settings.openAiCompatibleApiKey)

    override fun defaultModel(): String = "deepseek-v4-flash"
}
```

Update `ModelProviderRegistry` to include `DeepSeekProviderFactory()` and to fall back to `factory.defaultModel()` when `settings.model` is blank.
Update the IDE settings combo box to offer `ollama`, `deepseek`, and `openai-compatible`.

- [ ] **Step 4: Run test to verify it passes**

Run: `cd idea-plugin && ./gradlew test --tests com.gitai.commit.ModelProviderRegistryTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add idea-plugin/src/main/kotlin/com/gitai/commit/ModelProvider.kt idea-plugin/src/main/kotlin/com/gitai/commit/DeepSeekProviderFactory.kt idea-plugin/src/main/kotlin/com/gitai/commit/GitAiSettingsState.kt idea-plugin/src/main/kotlin/com/gitai/commit/GitAiSettingsConfigurable.kt idea-plugin/src/test/kotlin/com/gitai/commit/ModelProviderRegistryTest.kt
git commit -m "feat: add deepseek provider support"
```

### Task 3: CLI entrypoint, config commands, and preview output

**Files:**
- Modify: `idea-plugin/build.gradle.kts`
- Create: `idea-plugin/src/main/kotlin/com/gitai/commit/cli/GitAiCommitCli.kt`
- Create: `idea-plugin/src/main/kotlin/com/gitai/commit/cli/CliCommandParser.kt`
- Create: `idea-plugin/src/main/kotlin/com/gitai/commit/cli/GitAiConfigCommand.kt`
- Create: `idea-plugin/src/main/kotlin/com/gitai/commit/cli/GitAiCommitCommand.kt`
- Create: `idea-plugin/src/main/kotlin/com/gitai/commit/cli/GitCommitPreview.kt`
- Create: `idea-plugin/src/test/kotlin/com/gitai/commit/cli/GitAiCommitCliTest.kt`
- Create: `idea-plugin/src/test/kotlin/com/gitai/commit/cli/GitCommitPreviewTest.kt`

**Interfaces:**
- Consumes: `GitAiConfigStore`, `CommitMessageGenerator`, `GitCommitPreview`
- Produces: `fun main(args: Array<String>): Unit`, `CliCommandParser.parse`, `GitAiCommitCli.run`

- [ ] **Step 1: Write the failing tests**

```kotlin
@Test
fun commitCommandPrintsPreviewLine() {
    val stdout = ByteArrayOutputStream()
    val stderr = ByteArrayOutputStream()
    val cli = GitAiCommitCli(
        configStore = InMemoryConfigStore(),
        generateMessage = { _, _ -> CommitMessageGeneration.Success("feat: add cli support") },
        repoFinder = { Paths.get("/repo") },
        stdout = PrintStream(stdout),
        stderr = PrintStream(stderr)
    )

    val exitCode = cli.run(arrayOf("commit"))

    assertEquals(0, exitCode)
    assertEquals("git commit -m \"feat: add cli support\"\n", stdout.toString(Charsets.UTF_8))
}
```

Add a second test for config commands:

```kotlin
@Test
fun configSetAndGetUseGlobalScopeByDefault() {
    val store = InMemoryConfigStore()
    val cli = GitAiCommitCli(
        configStore = store,
        generateMessage = { _, _ -> CommitMessageGeneration.Success("feat: add cli support") },
        repoFinder = { Paths.get("/repo") },
        stdout = PrintStream(ByteArrayOutputStream()),
        stderr = PrintStream(ByteArrayOutputStream())
    )

    assertEquals(0, cli.run(arrayOf("config", "set", "providerId=deepseek")))
    assertEquals("deepseek", store.global.providerId)
    assertEquals(0, cli.run(arrayOf("config", "get", "providerId")))
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd idea-plugin && ./gradlew test --tests com.gitai.commit.cli.GitAiCommitCliTest`
Expected: FAIL because the CLI entrypoint and commands do not exist yet.

- [ ] **Step 3: Write minimal implementation**

```kotlin
class InMemoryConfigStore {
    val global = GitAiConfig()
    val project = GitAiConfig()

    fun save(scope: ConfigScope, config: GitAiConfig) {
        if (scope == ConfigScope.GLOBAL) {
            copyInto(global, config)
        } else {
            copyInto(project, config)
        }
    }

    fun mergedConfig(): GitAiConfig = GitAiConfig().apply {
        copyInto(this, global)
        providerId = project.providerId
        model = project.model
        promptStyle = project.promptStyle
        ollamaBaseUrl = project.ollamaBaseUrl
        openAiCompatibleBaseUrl = project.openAiCompatibleBaseUrl
        openAiCompatibleApiKey = project.openAiCompatibleApiKey
    }

    private fun copyInto(target: GitAiConfig, source: GitAiConfig) {
        target.providerId = source.providerId
        target.model = source.model
        target.promptStyle = source.promptStyle
        target.ollamaBaseUrl = source.ollamaBaseUrl
        target.openAiCompatibleBaseUrl = source.openAiCompatibleBaseUrl
        target.openAiCompatibleApiKey = source.openAiCompatibleApiKey
    }
}
fun main(args: Array<String>) {
    val cli = GitAiCommitCli()
    kotlin.system.exitProcess(cli.run(args))
}
```

Implement `CliCommandParser` for:
- `git-ai-commit config get [key] [--scope global|project|merged]`
- `git-ai-commit config set key=value [key=value ...] [--scope global|project]`
- `git-ai-commit config list [--scope global|project|merged]`
- `git-ai-commit commit`

Implement `GitCommitPreview.quote(message)` so it returns a single shell-safe double-quoted argument by replacing backslashes with `\\`, double quotes with `\"`, and newlines with spaces.
Apply the Gradle `application` plugin in `idea-plugin/build.gradle.kts` and set the CLI main class to `com.gitai.commit.cli.GitAiCommitCliKt`.

- [ ] **Step 4: Run test to verify it passes**

Run: `cd idea-plugin && ./gradlew test --tests com.gitai.commit.cli.GitAiCommitCliTest --tests com.gitai.commit.cli.GitCommitPreviewTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add idea-plugin/build.gradle.kts idea-plugin/src/main/kotlin/com/gitai/commit/cli idea-plugin/src/test/kotlin/com/gitai/commit/cli
git commit -m "feat: add cli commands"
```

### Task 4: Wire shared config into generation flow and update user docs

**Files:**
- Modify: `idea-plugin/src/main/kotlin/com/gitai/commit/CommitMessageGenerator.kt`
- Modify: `idea-plugin/src/main/kotlin/com/gitai/commit/GitAiSettingsState.kt`
- Modify: `README.md`
- Modify: `idea-plugin/src/test/kotlin/com/gitai/commit/CommitMessageGeneratorTest.kt`
- Create: `idea-plugin/src/test/kotlin/com/gitai/commit/cli/GitAiCommitIntegrationTest.kt`

**Interfaces:**
- Consumes: `GitAiConfig`, `GitAiConfigStore`, `GitAiCommitCli`
- Produces: end-to-end commit preview from a real git repo and updated README usage examples

- [ ] **Step 1: Write the failing tests**

```kotlin
@Test
fun cliUsesMergedProjectConfigOverGlobalConfig() {
    val stdout = ByteArrayOutputStream()
    val store = InMemoryConfigStore().apply {
        save(ConfigScope.GLOBAL, GitAiConfig().apply {
            providerId = "ollama"
            model = "qwen2.5-coder:7b"
        })
        save(ConfigScope.PROJECT, GitAiConfig().apply {
            providerId = "deepseek"
            openAiCompatibleApiKey = "sk-test"
        })
    }
    val cli = GitAiCommitCli(
        configStore = store,
        generateMessage = { _, _ -> CommitMessageGeneration.Success("feat: use project config") },
        repoFinder = { Paths.get("/repo") },
        stdout = PrintStream(stdout),
        stderr = PrintStream(ByteArrayOutputStream())
    )

    assertEquals(0, cli.run(arrayOf("commit")))
    assertTrue(stdout.toString(Charsets.UTF_8).contains("git commit -m \"feat: use project config\""))
}
```

Add a README assertion by checking the CLI usage block contains `git-ai-commit config set providerId=deepseek`.

- [ ] **Step 2: Run test to verify it fails**

Run: `cd idea-plugin && ./gradlew test --tests com.gitai.commit.cli.GitAiCommitIntegrationTest`
Expected: FAIL because the config store is not yet wired into the CLI path.

- [ ] **Step 3: Write minimal implementation**

```kotlin
val settings = configStore.loadMerged(repoRoot)
val generator = CommitMessageGenerator(
    settingsProvider = { settings.toSettingsStateData() }
)
```

Add README examples for:
- `git-ai-commit config set providerId=deepseek openAiCompatibleApiKey=...`
- `git-ai-commit commit`

- [ ] **Step 4: Run test to verify it passes**

Run: `cd idea-plugin && ./gradlew test`
Expected: PASS for the full Kotlin test suite.

- [ ] **Step 5: Commit**

```bash
git add README.md idea-plugin/src/main/kotlin/com/gitai/commit/CommitMessageGenerator.kt idea-plugin/src/main/kotlin/com/gitai/commit/GitAiSettingsState.kt idea-plugin/src/test/kotlin/com/gitai/commit idea-plugin/src/test/kotlin/com/gitai/commit/cli
git commit -m "feat: wire cli into commit generation"
```

## Self-Review Checklist

- Config precedence is covered by Task 1.
- DeepSeek provider support and IDE switching are covered by Task 2.
- CLI config commands and preview output are covered by Task 3.
- End-to-end wiring and documentation are covered by Task 4.
- No task assumes an undefined function or type.
- No task says “TODO” or defers actual implementation details.
