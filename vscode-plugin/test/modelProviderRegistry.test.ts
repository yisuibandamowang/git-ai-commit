import { describe, it } from 'node:test';
import * as assert from 'node:assert';
import { ModelProviderRegistry, GitAiSettings } from '../src/providers/modelProvider';
import { createDefaultRegistry } from '../src/providers/registry';
import { OllamaProviderFactory } from '../src/providers/ollamaClient';
import { DeepSeekProviderFactory } from '../src/providers/openAiCompatibleClient';

describe('ModelProviderRegistry', () => {
    const defaultSettings: GitAiSettings = {
        providerId: 'ollama',
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

    it('should return all provider IDs', () => {
        const registry = createDefaultRegistry();
        const ids = registry.providerIds();
        assert.ok(ids.includes('ollama'));
        assert.ok(ids.includes('deepseek'));
        assert.ok(ids.includes('aliyun'));
        assert.ok(ids.includes('minimax'));
        assert.ok(ids.includes('kimi'));
        assert.ok(ids.includes('glm'));
        assert.ok(ids.includes('openai-compatible'));
    });

    it('should select ollama provider by default', () => {
        const registry = createDefaultRegistry();
        const selection = registry.select(defaultSettings);
        assert.strictEqual(selection.provider.id, 'ollama');
        assert.strictEqual(selection.model, 'qwen2.5-coder:7b');
    });

    it('should select deepseek provider when configured', () => {
        const registry = createDefaultRegistry();
        const settings = { ...defaultSettings, providerId: 'deepseek' };
        const selection = registry.select(settings);
        assert.strictEqual(selection.provider.id, 'openai-compatible');
        assert.strictEqual(selection.model, 'deepseek-v4-flash');
    });

    it('should use custom model when specified', () => {
        const registry = createDefaultRegistry();
        const settings = { ...defaultSettings, model: 'custom-model' };
        const selection = registry.select(settings);
        assert.strictEqual(selection.model, 'custom-model');
    });

    it('should fall back to ollama for unknown provider', () => {
        const registry = createDefaultRegistry();
        const settings = { ...defaultSettings, providerId: 'unknown-provider' };
        const selection = registry.select(settings);
        assert.strictEqual(selection.provider.id, 'ollama');
    });

    it('should return default model for known providers', () => {
        const registry = createDefaultRegistry();
        assert.strictEqual(registry.defaultModelFor('ollama'), 'qwen2.5-coder:7b');
        assert.strictEqual(registry.defaultModelFor('deepseek'), 'deepseek-v4-flash');
        assert.strictEqual(registry.defaultModelFor('aliyun'), 'qwen-plus');
        assert.strictEqual(registry.defaultModelFor('unknown'), '');
    });

    it('should support custom factory list', () => {
        const registry = new ModelProviderRegistry([
            new OllamaProviderFactory(),
            new DeepSeekProviderFactory(),
        ]);
        const ids = registry.providerIds();
        assert.strictEqual(ids.length, 2);
        assert.ok(ids.includes('ollama'));
        assert.ok(ids.includes('deepseek'));
    });
});
