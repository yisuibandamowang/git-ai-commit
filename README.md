# git-ai-commit

> 智能提交描述 —— 本地优先的 AI commit message 生成助手，提供 IDEA 插件与 CLI 两种形态。

[![version](https://img.shields.io/badge/version-0.1.23-blue)](https://github.com/yisuibandamowang/git-ai-commit/releases)
[![JDK](https://img.shields.io/badge/JDK-21-red)](https://adoptium.net/)
[![platform](https://img.shields.io/badge/platform-IntelliJ%20Platform-green)](https://plugins.jetbrains.com/)

`git-ai-commit` 从你的 git diff 出发，用大模型生成一条规范的 commit message。默认本地优先：只要本机有 [Ollama](https://ollama.com/) 和 `qwen2.5-coder:7b`，就能零密钥、离线使用。需要更强的模型时，切换到云服务商即可，接入只需一个 API key。

## 特性

- **本地优先**：默认通过 Ollama 跑本地模型，不联网、不上传 diff。
- **一次接入，多厂商可用**：DeepSeek、阿里云百炼、MiniMax、Kimi（Moonshot）、智谱 GLM 都是 OpenAI 兼容协议，共用同一个 API key，切换零成本。
- **可插拔架构**：新增一个模型服务商只需一个 Factory 类 + 一行注册，不改核心逻辑。
- **双形态**：IDEA 插件（在提交面板一键生成并填入）与 CLI（命令行预览/生成）。
- **分层配置**：支持全局配置与项目级覆盖，方便团队共享默认值。

## 目录

- [快速开始](#快速开始)
  - [方式一：IDEA 插件](#方式一idea-插件)
  - [方式二：CLI](#方式二cli)
- [支持的模型服务商](#支持的模型服务商)
- [配置](#配置)
  - [配置键](#配置键)
  - [作用域与优先级](#作用域与优先级)
- [CLI 用法](#cli-用法)
- [构建与打包](#构建与打包)
- [如何扩展](#如何扩展)
- [免责声明](#免责声明)

## 快速开始

### 方式一：IDEA 插件

1. 从 [Releases](https://github.com/yisuibandamowang/git-ai-commit/releases) 下载最新的插件 ZIP（`git-ai-commit-plugin-*.zip`）。
2. 在 IDE 中打开 **Settings / Preferences → Plugins → 齿轮图标 → Install Plugin from Disk…**，选择刚下载的 ZIP 并安装。
3. 打开 **Settings → Tools → Git AI Commit**，选择 Provider 并填入 API key（本地 Ollama 无需 key）。
4. 在提交面板点击 **Generate Commit Message** 按钮，或从 **Tools → Git AI Commit → Generate Commit Message** 生成并复制到剪贴板。

> 使用本地 Ollama 时无需做任何配置：默认 Provider 就是 `ollama`，指向 `http://localhost:11434` 的 `qwen2.5-coder:7b`。

### 方式二：CLI

预编译包从 Releases 下载后解压即可运行；也可以本地构建（见[构建与打包](#构建与打包)）。

生成一条 commit 命令预览：

```bash
git-ai-commit commit
```

切换到云端服务商并写入 API key（所有 OpenAI 兼容厂商共用 `openAiCompatibleApiKey`）：

```bash
git-ai-commit config set providerId=deepseek openAiCompatibleApiKey=<your_api_key>
git-ai-commit config set model=deepseek-v4-flash
```

## 支持的模型服务商

| Provider | 说明 | 默认 base URL | 默认模型 |
| --- | --- | --- | --- |
| `ollama` | 本地模型，默认走 Ollama | `http://localhost:11434` | `qwen2.5-coder:7b` |
| `deepseek` | DeepSeek | `https://api.deepseek.com` | `deepseek-v4-flash` |
| `aliyun` | 阿里云百炼（DashScope） | `https://dashscope.aliyuncs.com/compatible-mode/v1` | `qwen-plus` |
| `minimax` | MiniMax | `https://api.minimaxi.com/v1` | `MiniMax-Text-01` |
| `kimi` | Kimi（Moonshot） | `https://api.moonshot.cn/v1` | `moonshot-v1-8k` |
| `glm` | 智谱 GLM | `https://open.bigmodel.cn/api/paas/v4` | `glm-4-flash` |
| `openai-compatible` | 任意 OpenAI 兼容接口 | `https://api.openai.com/v1` | 需自行填写 |

除 `ollama` 外，其余厂商都走 OpenAI 兼容协议，共用同一个 API key（配置键 `openAiCompatibleApiKey`），IDEA 设置面板里统一为一个 "API key" 输入框。

## 配置

### 配置键

| 键 | 说明 |
| --- | --- |
| `providerId` | 服务商标识，见上表 |
| `model` | 模型名 |
| `promptStyle` | 提交信息风格，默认 `conventional-commits` |
| `openAiCompatibleApiKey` | 统一的 API key（所有 OpenAI 兼容厂商共用） |
| `ollamaBaseUrl` | Ollama 地址 |
| `deepSeekBaseUrl` | DeepSeek 地址 |
| `aliyunBaseUrl` | 阿里云百炼地址 |
| `miniMaxBaseUrl` | MiniMax 地址 |
| `kimiBaseUrl` | Kimi 地址 |
| `glmBaseUrl` | 智谱 GLM 地址 |
| `openAiCompatibleBaseUrl` | 通用 OpenAI 兼容接口地址 |

### 作用域与优先级

| 作用域 | 位置 | 说明 |
| --- | --- | --- |
| `global` | `~/.config/git-ai-commit/config.json` | 全局默认 |
| `project` | `<repo>/.git-ai-commit/config.json` | 项目覆盖，优先级更高 |
| `merged` | 只读 | 全局 + 项目合并后的最终值 |

## CLI 用法

```bash
# 生成 commit 命令预览
git-ai-commit commit

# 查看配置（--scope 可选 global/project/merged，默认 merged）
git-ai-commit config get [key] [--scope global|project|merged]
git-ai-commit config list [--scope global|project|merged]

# 写入配置（默认写入 global，支持一次多个 key=value）
git-ai-commit config set providerId=glm openAiCompatibleApiKey=<key> model=glm-4-flash
git-ai-commit config set model=deepseek-v4-flash --scope project
```

生成成功会打印形如 `git commit -m "feat: ..."` 的命令，可以直接手动微调后执行。

## 构建与打包

需要 JDK 21。本地开发默认依赖 `/Applications/GoLand.app`（可改成你自己的 IDE 路径；CI 环境会自动回退到 JetBrains 官方依赖）。

```bash
cd idea-plugin

# 运行测试
JAVA_HOME=/Applications/GoLand.app/Contents/jbr/Contents/Home ./gradlew test

# 打包 IDEA 插件（产物在 build/distributions/*.zip）
JAVA_HOME=/Applications/GoLand.app/Contents/jbr/Contents/Home ./gradlew buildPlugin

# 构建 CLI 可运行发行版（产物在 build/install/git-ai-commit/）
JAVA_HOME=/Applications/GoLand.app/Contents/jbr/Contents/Home ./gradlew installDist
```

## 如何扩展

项目采用「工厂 + 注册」的可插拔结构，新增一个模型服务商只需三步：

1. **新建 Factory**：实现 `ModelProviderFactory`，声明 `id`、默认模型，并在 `create()` 里返回一个 `OpenAiCompatibleClient`（若走 OpenAI 兼容协议）。参考 `idea-plugin/src/main/kotlin/com/gitai/commit/CloudProviderFactories.kt`。
2. **注册**：把新 Factory 加进 `ModelProviderRegistry` 的默认工厂列表。
3. **加配置**：在 `GitAiSettingsStateData`、`GitAiConfig`、CLI 配置键以及 IDEA 设置面板中补上对应的 base URL 字段。

核心类：

| 类 | 职责 |
| --- | --- |
| `ModelProvider` / `ModelProviderFactory` | 服务商抽象与工厂接口 |
| `ModelProviderRegistry` | 按 `providerId` 选择服务商 |
| `CommitMessageGenerator` | 读取 diff → 过滤 → 拼 prompt → 调模型 → 格式化输出 |
| `OpenAiCompatibleClient` | 通用 OpenAI 兼容 HTTP 客户端 |

## 免责声明

使用云端服务商意味着你的 git diff 会被发送到对应厂商。请根据团队合规要求选择合适的本地或云端方案，并妥善保管 API key。