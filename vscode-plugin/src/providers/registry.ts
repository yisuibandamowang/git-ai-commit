import { ModelProviderRegistry } from './modelProvider';
import { OllamaProviderFactory } from './ollamaClient';
import {
    DeepSeekProviderFactory,
    AliyunProviderFactory,
    MiniMaxProviderFactory,
    KimiProviderFactory,
    GlmProviderFactory,
    OpenAiCompatibleProviderFactory,
} from './openAiCompatibleClient';

/**
 * Creates the default ModelProviderRegistry with all built-in providers.
 * Mirrors the Kotlin `ModelProviderRegistry` default constructor.
 */
export function createDefaultRegistry(): ModelProviderRegistry {
    return new ModelProviderRegistry([
        new OllamaProviderFactory(),
        new DeepSeekProviderFactory(),
        new AliyunProviderFactory(),
        new MiniMaxProviderFactory(),
        new KimiProviderFactory(),
        new GlmProviderFactory(),
        new OpenAiCompatibleProviderFactory(),
    ]);
}