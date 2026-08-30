import { describe, it } from 'node:test';
import * as assert from 'node:assert';
import { extractOpenAiCompatibleDelta } from '../src/providers/openAiCompatibleClient';

describe('OpenAiCompatibleClient streaming parser', () => {
    it('should extract delta content from SSE data lines', () => {
        const line = 'data: {"choices":[{"delta":{"content":"修复"}}]}';
        assert.strictEqual(extractOpenAiCompatibleDelta(line), '修复');
    });

    it('should ignore stream terminator lines', () => {
        assert.strictEqual(extractOpenAiCompatibleDelta('data: [DONE]'), '');
    });
});
