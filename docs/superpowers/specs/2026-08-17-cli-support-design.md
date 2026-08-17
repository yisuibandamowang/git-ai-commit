# Git AI Commit CLI Support Design

## Context
`idea-plugin` already has a working generation pipeline:
`GitDiffReader` -> `GitDiffFilter` -> `PromptBuilder` -> `ModelProviderRegistry` -> `CommitMessageFormatter`.
The IDE settings page can already store provider/model/base URL/API key values, but those values live only inside the plugin state.

This change adds a real terminal entry point and a config command so the tool can be used without IDEA, while keeping the IDE plugin working.

## Goals
- Run from a terminal inside any git repo.
- Support `config` commands for reading and updating settings.
- Support global config by default.
- Allow project-level config as an override.
- Generate a commit message and print a shell-ready `git commit -m "..."` line for manual editing.
- Reuse the existing generation logic instead of duplicating it.
- Keep the IDEA plugin on the same provider/model semantics.

## Non-Goals
- Do not auto-run `git commit`.
- Do not build an interactive TUI or editor flow.
- Do not introduce cloud sync or remote config storage.
- Do not redesign the IDE UI beyond what is needed to expose the same providers.

## Architecture
Add a small shared Kotlin core and two thin entry points:

1. `core` owns diff reading, filtering, prompt building, provider selection, message formatting, and config resolution.
2. `cli` exposes `git-ai-commit` commands and writes/reads config files.
3. `idea-plugin` keeps its UI/actions, but reads the shared config model through a small adapter.

The shared provider layer should include:
- `ollama`
- `openai-compatible`
- `deepseek` as a preset alias backed by the OpenAI-compatible client and DeepSeek defaults

That keeps DeepSeek usable directly in both CLI and IDE without special-case logic in the rest of the app.

## Config Model
Config is layered, with later sources overriding earlier ones:

1. Built-in defaults
2. Global config
3. Project config

Storage locations:
- Global: `$XDG_CONFIG_HOME/git-ai-commit/config.json` when `XDG_CONFIG_HOME` is set, otherwise `~/.config/git-ai-commit/config.json` on Unix-like systems, and `%APPDATA%\\git-ai-commit\\config.json` on Windows.
- Project: `<repo>/.git-ai-commit/config.json`

Shared config fields:
- `providerId`
- `model`
- `promptStyle`
- `ollamaBaseUrl`
- `openAiCompatibleBaseUrl`
- `openAiCompatibleApiKey`

The CLI should support:
- `git-ai-commit config get [key] [--scope global|project]`
- `git-ai-commit config set key=value [key=value ...] [--scope global|project]`
- `git-ai-commit config list [--scope global|project|merged]`

Default scope for writes is global. Project scope is opt-in.

## CLI Commands
Primary flow:
- `git-ai-commit commit`

Behavior:
- Detect the current git repository root.
- Load merged config.
- Read staged diff first, then unstaged diff if needed.
- Generate the message through the shared pipeline.
- Print a single shell-ready preview line, such as:
  `git commit -m "feat: 优化提交信息生成"`

The CLI stops there. The user can copy or edit the command manually.

## Data Flow
1. CLI resolves repo root.
2. Config resolver loads global and project config.
3. Diff reader collects git diff.
4. Prompt builder prepares the request.
5. Selected provider generates text.
6. Formatter normalizes output into one commit subject.
7. CLI prints the preview command.

## IDE Impact
The existing plugin actions keep working.
The settings page should use the same provider IDs and config schema as the shared core, so switching between Ollama, OpenAI-compatible, and DeepSeek is consistent.
No IDE workflow changes are required for the first pass beyond wiring the shared provider set.

## Errors
- No git repo: fail with a short stderr message and non-zero exit code.
- Empty diff: report that there are no changes to commit.
- Provider failure: surface the provider error clearly.
- Invalid config: report the bad key or value and stop.

## Testing
Add focused tests for:
- config read/write and precedence
- git repo detection
- CLI preview output escaping
- message generation through the shared core
- provider selection, including the DeepSeek alias

Keep existing IDEA tests passing.

## Out Of Scope For This Pass
- Auto-commit execution
- Interactive message editing
- Per-branch or remote config sync
- Any larger UI redesign
