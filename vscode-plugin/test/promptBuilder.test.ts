import { describe, it } from 'node:test';
import * as assert from 'node:assert';
import { PromptBuilder } from '../src/core/promptBuilder';

describe('PromptBuilder', () => {
    const builder = new PromptBuilder();

    it('should build prompt with diff content', () => {
        const diff = [
            'diff --git a/src/main.kt b/src/main.kt',
            '--- a/src/main.kt',
            '+++ b/src/main.kt',
            '+fun hello() = "world"',
        ].join('\n');

        const prompt = builder.build(diff, 'conventional-commits');
        assert.ok(prompt.includes('你是一位资深工程师'));
        assert.ok(prompt.includes('conventional-commits'));
        assert.ok(prompt.includes('src/main.kt'));
        assert.ok(prompt.includes('hello()'));
    });

    it('should include rules in prompt', () => {
        const prompt = builder.build('diff --git a/x b/x\n+x', 'conventional-commits');
        assert.ok(prompt.includes('只输出一句 Conventional Commit 格式'));
        assert.ok(prompt.includes('不要输出 JSON'));
        assert.ok(prompt.includes('长度尽量控制在30字以内'));
    });

    it('should request bullet body output in detailed mode', () => {
        const prompt = builder.build('diff --git a/x b/x\n+x', 'conventional-commits', 'detailed');
        assert.ok(prompt.includes('subject + 空行 + 若干 bullet body'));
    });

    it('should handle empty diff gracefully', () => {
        const prompt = builder.build('', 'conventional-commits');
        assert.ok(prompt.includes('未检测到文件变更'));
    });
});
