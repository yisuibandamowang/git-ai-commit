import { describe, it } from 'node:test';
import * as assert from 'node:assert';
import { CommitMessageGenerator } from '../src/core/commitMessageGenerator';
import { GitAiSettings, ModelProvider, ModelProviderFactory, ModelProviderRegistry } from '../src/providers/modelProvider';
import { GitDiffReader } from '../src/git/gitDiffReader';

class FakeProvider implements ModelProvider {
    readonly id = 'fake';

    constructor(private chunks: string[]) {}

    async generate(): Promise<string> {
        return this.chunks.join('');
    }

    async generateStream(_model: string, _prompt: string, onChunk: (chunk: string) => void): Promise<string> {
        let raw = '';
        for (const chunk of this.chunks) {
            raw += chunk;
            onChunk(chunk);
        }
        return raw;
    }
}

class FakeFactory implements ModelProviderFactory {
    readonly id = 'fake';
    readonly defaultModel = 'fake-model';

    constructor(private provider: ModelProvider) {}

    create(): ModelProvider {
        return this.provider;
    }
}

describe('CommitMessageGenerator streaming', () => {
    const settings: GitAiSettings = {
        providerId: 'fake',
        model: '',
        promptStyle: 'conventional-commits',
        messageStyle: 'short',
        ollamaBaseUrl: 'http://localhost:11434',
        deepSeekBaseUrl: 'https://api.deepseek.com',
        aliyunBaseUrl: 'https://dashscope.aliyuncs.com/compatible-mode/v1',
        miniMaxBaseUrl: 'https://api.minimaxi.com/v1',
        kimiBaseUrl: 'https://api.moonshot.cn/v1',
        glmBaseUrl: 'https://open.bigmodel.cn/api/paas/v4',
        openAiCompatibleBaseUrl: 'https://api.openai.com/v1',
        openAiCompatibleApiKey: '',
    };

    it('should forward streamed chunks before returning the final message', async () => {
        const provider = new FakeProvider(['fix', '(plugin)', ': ', '支持', '流式']);
        const registry = new ModelProviderRegistry([new FakeFactory(provider)]);
        const diffReader = { readDiff: () => 'diff' } as unknown as GitDiffReader;
        const generator = new CommitMessageGenerator(
            diffReader,
            undefined,
            undefined,
            undefined,
            registry,
        );
        const updates: string[] = [];

        const result = await generator.generate('repo', settings, value => updates.push(value));

        assert.deepStrictEqual(updates, ['fix', 'fix(plugin)', 'fix(plugin): ', 'fix(plugin): 支持', 'fix(plugin): 支持流式', 'fix(plugin): 支持流式']);
        assert.strictEqual(result.kind, 'success');
        if (result.kind === 'success') {
            assert.ok(result.message.includes('支持流式'));
        }
    });
});
