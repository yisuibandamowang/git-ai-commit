import { GitAiSettings, normalizeCommitMessageStyle } from '../providers/modelProvider';

export interface GitAiConfiguration {
    get<T>(key: string): T | undefined;
    get<T>(key: string, defaultValue: T): T;
}

export function readGitAiSettings(config: GitAiConfiguration): GitAiSettings {
    return {
        providerId: config.get<string>('providerId', 'ollama'),
        model: config.get<string>('model', ''),
        promptStyle: config.get<string>('promptStyle', 'conventional-commits'),
        messageStyle: normalizeCommitMessageStyle(config.get<string>('messageStyle', 'short')),
        ollamaBaseUrl: config.get<string>('ollamaBaseUrl', 'http://localhost:11434'),
        deepSeekBaseUrl: config.get<string>('deepSeekBaseUrl', 'https://api.deepseek.com'),
        aliyunBaseUrl: config.get<string>('aliyunBaseUrl', 'https://dashscope.aliyuncs.com/compatible-mode/v1'),
        miniMaxBaseUrl: config.get<string>('miniMaxBaseUrl', 'https://api.minimaxi.com/v1'),
        kimiBaseUrl: config.get<string>('kimiBaseUrl', 'https://api.moonshot.cn/v1'),
        glmBaseUrl: config.get<string>('glmBaseUrl', 'https://open.bigmodel.cn/api/paas/v4'),
        openAiCompatibleBaseUrl: config.get<string>('openAiCompatibleBaseUrl', 'https://api.openai.com/v1'),
        openAiCompatibleApiKey: config.get<string>('openAiCompatibleApiKey', ''),
    };
}
