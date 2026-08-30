import { describe, it } from 'node:test';
import * as assert from 'node:assert';
import { extractOllamaDelta } from '../src/providers/ollamaClient';

describe('OllamaClient streaming parser', () => {
    it('should extract response text from streamed JSON lines', () => {
        const line = '{"response":"修复","done":false}';
        assert.strictEqual(extractOllamaDelta(line), '修复');
    });

    it('should ignore done-only lines', () => {
        const line = '{"response":"","done":true}';
        assert.strictEqual(extractOllamaDelta(line), '');
    });
});
