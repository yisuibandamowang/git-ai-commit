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
