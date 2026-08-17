# git-ai-commit

Local-first commit message assistant for IDEA and CLI.

## CLI

Build the runnable distribution:

```bash
cd idea-plugin
JAVA_HOME=/Applications/GoLand.app/Contents/jbr/Contents/Home ./gradlew installDist
```

Run it with Java 21 or the bundled GoLand JBR on your `JAVA_HOME`.

Generate a preview commit command:

```bash
JAVA_HOME=/Applications/GoLand.app/Contents/jbr/Contents/Home \
./build/install/git-ai-commit/bin/git-ai-commit commit
```

Set global config:

```bash
JAVA_HOME=/Applications/GoLand.app/Contents/jbr/Contents/Home \
./build/install/git-ai-commit/bin/git-ai-commit config set providerId=deepseek openAiCompatibleApiKey=<your_api_key>
JAVA_HOME=/Applications/GoLand.app/Contents/jbr/Contents/Home \
./build/install/git-ai-commit/bin/git-ai-commit config set model=deepseek-v4-flash
```

Set a project override:

```bash
JAVA_HOME=/Applications/GoLand.app/Contents/jbr/Contents/Home \
./build/install/git-ai-commit/bin/git-ai-commit config set model=deepseek-v4-flash --scope project
```

## IDE

In IDEA settings, the provider list includes `ollama`, `deepseek`, and `openai-compatible`.
Switching to `DeepSeek` uses the OpenAI-compatible client with the DeepSeek defaults.

## Releases

We publish two kinds of release artifacts from GitHub tags like `v0.1.2`:

- CLI bundles for Linux, macOS, and Windows with a bundled JRE
- The IDEA plugin ZIP for direct install

The CLI bundles are created by GitHub Actions from `scripts/package-cli-release.sh`.

To publish a release, make sure `idea-plugin/build.gradle.kts` has the matching
`version`, then push a tag:

```bash
git tag v0.1.2
git push origin v0.1.2
```

GitHub Releases will contain assets like:

- `git-ai-commit-0.1.2-linux-x86-64.zip`
- `git-ai-commit-0.1.2-macos-arm64.zip`
- `git-ai-commit-0.1.2-windows-x86-64.zip`
- `git-ai-commit-plugin-0.1.2.zip`

## JetBrains Marketplace

The plugin can also be published to JetBrains Marketplace from the same tag push.
Set these secrets in GitHub Actions:

- `JETBRAINS_CERTIFICATE_CHAIN`
- `JETBRAINS_PRIVATE_KEY`
- `JETBRAINS_PRIVATE_KEY_PASSWORD`
- `JETBRAINS_PUBLISH_TOKEN`

Optional variable:

- `JETBRAINS_PUBLISH_CHANNELS` for channels like `default` or `eap`
