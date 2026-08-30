import { ModelProvider, ModelProviderFactory, GitAiSettings } from './modelProvider';

export function extractOllamaDelta(line: string): string {
    const trimmed = line.trim();
    if (!trimmed) {
        return '';
    }

    try {
        const json = JSON.parse(trimmed) as {
            response?: string;
            done?: boolean;
        };
        return json.response ?? '';
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
 * Ollama client matching the Kotlin `OllamaClient`.
 * Calls Ollama's /api/generate endpoint.
 */
export class OllamaClient implements ModelProvider {
    readonly id: string = 'ollama';

    constructor(private baseUrl: string) {}

    async generate(model: string, prompt: string): Promise<string> {
        let raw = '';
        await this.generateStream(model, prompt, chunk => {
            raw += chunk;
        });
        return raw.trim();
    }

    async generateStream(model: string, prompt: string, onChunk: (chunk: string) => void): Promise<string> {
        const url = `${this.baseUrl.replace(/\/+$/, '')}/api/generate`;
        const body = JSON.stringify({
            model,
            prompt,
            stream: true,
            options: { temperature: 0.0 },
        });

        const response = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body,
        });

        if (!response.ok) {
            const text = await response.text();
            throw new Error(`Ollama returned HTTP ${response.status}: ${text}`);
        }

        let raw = '';
        for await (const line of readResponseLines(response)) {
            const delta = extractOllamaDelta(line);
            if (!delta) {
                continue;
            }
            raw += delta;
            onChunk(delta);
        }

        return raw.trim();
    }
}

export class OllamaProviderFactory implements ModelProviderFactory {
    readonly id: string = 'ollama';
    readonly defaultModel: string = 'qwen2.5-coder:7b';

    create(settings: GitAiSettings): ModelProvider {
        return new OllamaClient(settings.ollamaBaseUrl);
    }
}
