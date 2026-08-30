/**
 * Model provider interface matching the Kotlin `ModelProvider` interface.
 */
export interface ModelProvider {
    readonly id: string;
    generate(model: string, prompt: string): Promise<string>;
    generateStream(model: string, prompt: string, onChunk: (chunk: string) => void): Promise<string>;
}

export interface ProviderSelection {
    provider: ModelProvider;
    model: string;
}

export type CommitMessageStyle = 'short' | 'detailed';

export function normalizeCommitMessageStyle(style: string | undefined): CommitMessageStyle {
    return style === 'detailed' ? 'detailed' : 'short';
}

/**
 * Provider factory interface matching the Kotlin `ModelProviderFactory` interface.
 */
export interface ModelProviderFactory {
    readonly id: string;
    readonly defaultModel: string;
    create(settings: GitAiSettings): ModelProvider;
}

/**
 * Settings model matching the Kotlin `GitAiSettingsStateData`.
 */
export interface GitAiSettings {
    providerId: string;
    model: string;
    promptStyle: string;
    messageStyle: CommitMessageStyle;
    ollamaBaseUrl: string;
    deepSeekBaseUrl: string;
    aliyunBaseUrl: string;
    miniMaxBaseUrl: string;
    kimiBaseUrl: string;
    glmBaseUrl: string;
    openAiCompatibleBaseUrl: string;
    openAiCompatibleApiKey: string;
}

/**
 * Provider base URLs (matching the Kotlin companion objects).
 */
export const PROVIDER_BASE_URLS = {
    DEEPSEEK: 'https://api.deepseek.com',
    ALIYUN: 'https://dashscope.aliyuncs.com/compatible-mode/v1',
    MINIMAX: 'https://api.minimaxi.com/v1',
    KIMI: 'https://api.moonshot.cn/v1',
    GLM: 'https://open.bigmodel.cn/api/paas/v4',
} as const;

/**
 * Provider registry matching the Kotlin `ModelProviderRegistry`.
 */
export class ModelProviderRegistry {
    private factories: ModelProviderFactory[];

    constructor(factories: ModelProviderFactory[]) {
        this.factories = factories;
    }

    select(settings: GitAiSettings): ProviderSelection {
        const factory = this.factories.find(f => f.id === settings.providerId)
            ?? this.factories.find(f => f.id === 'ollama')!;
        const model = settings.model || factory.defaultModel;
        return { provider: factory.create(settings), model };
    }

    providerIds(): string[] {
        return this.factories.map(f => f.id);
    }

    defaultModelFor(providerId: string): string {
        return this.factories.find(f => f.id === providerId)?.defaultModel ?? '';
    }
}
