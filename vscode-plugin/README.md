# Git AI Commit - VS Code Extension

Generate commit messages from git diff using local or compatible model providers.

## Features

- **Local-first**: Defaults to Ollama with `qwen2.5-coder:7b`, no API key or internet required
- **Multi-provider**: Supports Ollama, DeepSeek, Aliyun (DashScope), MiniMax, Kimi (Moonshot), GLM, and OpenAI-compatible endpoints
- **One-click**: Click the ✨ button in the SCM panel to generate a commit message from staged changes
- **Conventional Commits**: Produces messages in `feat: 中文描述` format

## Usage

1. Open a project with a git repository in VS Code
2. Stage your changes in the Source Control panel
3. Click the ✨ (Generate Commit Message) button in the SCM title bar
4. The generated commit message will be filled into the commit input box

You can also run the command from the Command Palette (`Cmd+Shift+P` / `Ctrl+Shift+P`): **Generate Commit Message**

## Configuration

Configure through VS Code Settings (`Cmd+,` / `Ctrl+,`), search for "Git AI Commit":

| Setting | Default | Description |
|---------|---------|-------------|
| `git-ai-commit.providerId` | `ollama` | Model provider: ollama, deepseek, aliyun, minimax, kimi, glm, openai-compatible |
| `git-ai-commit.model` | (empty) | Model name (uses provider default if empty) |
| `git-ai-commit.promptStyle` | `conventional-commits` | Prompt style for generation |
| `git-ai-commit.ollamaBaseUrl` | `http://localhost:11434` | Ollama API base URL |
| `git-ai-commit.deepSeekBaseUrl` | `https://api.deepseek.com` | DeepSeek API base URL |
| `git-ai-commit.aliyunBaseUrl` | `https://dashscope.aliyuncs.com/compatible-mode/v1` | Aliyun DashScope API base URL |
| `git-ai-commit.miniMaxBaseUrl` | `https://api.minimaxi.com/v1` | MiniMax API base URL |
| `git-ai-commit.kimiBaseUrl` | `https://api.moonshot.cn/v1` | Kimi (Moonshot) API base URL |
| `git-ai-commit.glmBaseUrl` | `https://open.bigmodel.cn/api/paas/v4` | GLM (Zhipu) API base URL |
| `git-ai-commit.openAiCompatibleBaseUrl` | `https://api.openai.com/v1` | OpenAI-compatible API base URL |
| `git-ai-commit.openAiCompatibleApiKey` | (empty) | API key for all non-Ollama providers |

## Extension Settings

This extension contributes the following settings:

* `git-ai-commit.providerId`: The model provider to use for generating commit messages
* `git-ai-commit.model`: The model name to use (leave empty for provider default)
* `git-ai-commit.promptStyle`: The prompt style for generating commit messages
* `git-ai-commit.ollamaBaseUrl`: Base URL for the Ollama API
* `git-ai-commit.deepSeekBaseUrl`: Base URL for the DeepSeek API
* `git-ai-commit.aliyunBaseUrl`: Base URL for the Aliyun DashScope API
* `git-ai-commit.miniMaxBaseUrl`: Base URL for the MiniMax API
* `git-ai-commit.kimiBaseUrl`: Base URL for the Kimi (Moonshot) API
* `git-ai-commit.glmBaseUrl`: Base URL for the GLM (Zhipu) API
* `git-ai-commit.openAiCompatibleBaseUrl`: Base URL for the OpenAI-compatible API
* `git-ai-commit.openAiCompatibleApiKey`: API key for OpenAI-compatible providers

## Development

```bash
# Install dependencies
npm install

# Compile
npm run compile

# Run tests
npm test
```

## Release Notes

### 0.1.23

Initial release of Git AI Commit for VS Code.