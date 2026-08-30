import { ModelProvider, ModelProviderFactory, GitAiSettings, PROVIDER_BASE_URLS } from './modelProvider';

export function extractOpenAiCompatibleDelta(line: string): string {
    const trimmed = line.trim();
    if (!trimmed.startsWith('data:')) {
        return '';
    }

    const payload = trimmed.replace(/^data:\s*/, '');
    if (!payload || payload === '[DONE]') {
        return '';
    }

    try {
        const json = JSON.parse(payload) as {
            choices?: Array<{
                delta?: { content?: string };
                message?: { content?: string };
            }>;
        };
        return (json.choices?.[0]?.delta?.content ?? json.choices?.[0]?.message?.content ?? '');
    } catch {
        return '';
    }
}

async function* readResponseLines(response: Response): AsyncGenerator<string> {
    const reader = response.body?.getReader();
    if (!reader) {
        return;
    }

    const decoder = new TextDecoder();
    let buffer = '';

    while (true) {
        const { done, value } = await reader.read();
        if (done) {
            break;
        }

        buffer += decoder.decode(value, { stream: true });

        let newlineIndex = buffer.indexOf('\n');
        while (newlineIndex >= 0) {
            const line = buffer.slice(0, newlineIndex).replace(/\r$/, '');
            buffer = buffer.slice(newlineIndex + 1);
            yield line;
            newlineIndex = buffer.indexOf('\n');
        }
    }

    const tail = `${buffer}${decoder.decode()}`.trimEnd();
    if (tail) {
        yield tail;
    }
}

/**
 * OpenAI-compatible client matching the Kotlin `OpenAiCompatibleClient`.
 * Calls any OpenAI-compatible /chat/completions endpoint.
 */
export class OpenAiCompatibleClient implements ModelProvider {
    readonly id: string = 'openai-compatible';

    constructor(
        private baseUrl: string,
        private apiKey: string = '',
    ) {}

    async generate(model: string, prompt: string): Promise<string> {
        let raw = '';
        await this.generateStream(model, prompt, chunk => {
            raw += chunk;
        });
        return raw.trim();
    }

    async generateStream(model: string, prompt: string, onChunk: (chunk: string) => void): Promise<string> {
        const url = `${this.baseUrl.replace(/\/+$/, '')}/chat/completions`;
        const body = JSON.stringify({
            model,
            messages: [
                {
                    role: 'system',
                    content: '只输出一句中文提交信息，不要输出 JSON、不要输出英文提交信息、不要解释、不要正文。',
                },
                {
                    role: 'user',
                    content: prompt,
                },
            ],
            stream: true,
        });

        const headers: Record<string, string> = {
            'Content-Type': 'application/json',
        };
        if (this.apiKey) {
            headers['Authorization'] = `Bearer ${this.apiKey}`;
        }

        const response = await fetch(url, {
            method: 'POST',
            headers,
            body,
        });

        if (!response.ok) {
            const text = await response.text();
            throw new Error(`OpenAI-compatible provider returned HTTP ${response.status}: ${text}`);
        }

        let raw = '';
        for await (const line of readResponseLines(response)) {
            const delta = extractOpenAiCompatibleDelta(line);
            if (!delta) {
                continue;
            }
            raw += delta;
            onChunk(delta);
        }

        return raw.trim();
    }
}

export class OpenAiCompatibleProviderFactory implements ModelProviderFactory {
    readonly id: string = 'openai-compatible';
    readonly defaultModel: string = '';

    create(settings: GitAiSettings): ModelProvider {
        return new OpenAiCompatibleClient(
            settings.openAiCompatibleBaseUrl,
            settings.openAiCompatibleApiKey,
        );
    }
}

// --- Cloud provider factories (matching Kotlin CloudProviderFactories.kt) ---

export class DeepSeekProviderFactory implements ModelProviderFactory {
    readonly id: string = 'deepseek';
    readonly defaultModel: string = 'deepseek-v4-flash';

    create(settings: GitAiSettings): ModelProvider {
        return new OpenAiCompatibleClient(
            settings.deepSeekBaseUrl || PROVIDER_BASE_URLS.DEEPSEEK,
            settings.openAiCompatibleApiKey,
        );
    }
}

export class AliyunProviderFactory implements ModelProviderFactory {
    readonly id: string = 'aliyun';
    readonly defaultModel: string = 'qwen-plus';

    create(settings: GitAiSettings): ModelProvider {
        return new OpenAiCompatibleClient(
            settings.aliyunBaseUrl || PROVIDER_BASE_URLS.ALIYUN,
            settings.openAiCompatibleApiKey,
        );
    }
}

export class MiniMaxProviderFactory implements ModelProviderFactory {
    readonly id: string = 'minimax';
    readonly defaultModel: string = 'MiniMax-Text-01';

    create(settings: GitAiSettings): ModelProvider {
        return new OpenAiCompatibleClient(
            settings.miniMaxBaseUrl || PROVIDER_BASE_URLS.MINIMAX,
            settings.openAiCompatibleApiKey,
        );
    }
}

export class KimiProviderFactory implements ModelProviderFactory {
    readonly id: string = 'kimi';
    readonly defaultModel: string = 'kimi-k2.5';

    create(settings: GitAiSettings): ModelProvider {
        return new OpenAiCompatibleClient(
            settings.kimiBaseUrl || PROVIDER_BASE_URLS.KIMI,
            settings.openAiCompatibleApiKey,
        );
    }
}

export class GlmProviderFactory implements ModelProviderFactory {
    readonly id: string = 'glm';
    readonly defaultModel: string = 'glm-4-flash';

    create(settings: GitAiSettings): ModelProvider {
        return new OpenAiCompatibleClient(
            settings.glmBaseUrl || PROVIDER_BASE_URLS.GLM,
            settings.openAiCompatibleApiKey,
        );
    }
}
