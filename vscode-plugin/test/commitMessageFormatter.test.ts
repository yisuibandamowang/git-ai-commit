import { describe, it } from 'node:test';
import * as assert from 'node:assert';
import { CommitMessageFormatter } from '../src/core/commitMessageFormatter';

describe('CommitMessageFormatter', () => {
    const formatter = new CommitMessageFormatter();

    it('should return empty string for blank input', () => {
        assert.strictEqual(formatter.format(''), '');
        assert.strictEqual(formatter.format('   '), '');
    });

    it('should preserve already-formatted conventional commit messages', () => {
        const result = formatter.format('feat: 优化提交信息生成');
        assert.ok(result.includes('优化提交信息生成'));
    });

    it('should strip markdown code fences', () => {
        const result = formatter.format('```\nfeat: 添加新功能\n```');
        assert.ok(result.includes('添加新功能'));
    });

    it('should unwrap JSON response with commit_message key', () => {
        const json = JSON.stringify({ commit_message: 'feat: 添加新功能' });
        const result = formatter.format(json);
        assert.ok(result.includes('添加新功能'));
    });

    it('should unwrap JSON response with message key', () => {
        const json = JSON.stringify({ message: 'feat: 修复bug' });
        const result = formatter.format(json);
        assert.ok(result.includes('修复bug'));
    });

    it('should add conventional commit prefix if missing', () => {
        const result = formatter.format('优化提交信息生成');
        assert.ok(result.startsWith('feat:'));
    });

    it('should translate simple English subjects', () => {
        const result = formatter.format('fix: add support for multiple providers');
        assert.ok(result.includes('支持'));
    });

    it('should handle empty JSON response', () => {
        const result = formatter.format('{}');
        assert.strictEqual(result, '');
    });

    it('should filter out verbose explanations', () => {
        const verbose = 'This commit includes several changes to the codebase. Here\'s a breakdown of what was done.';
        const result = formatter.format(verbose);
        // Should produce a summary, not the raw verbose text
        assert.ok(result.length < 80);
        assert.ok(!result.toLowerCase().includes('this commit'));
    });

    it('should keep detailed subject and bullet body when requested', () => {
        const result = formatter.format('feat: 优化提交信息生成\n\n- 保持短格式\n- 支持详细格式', 'detailed');
        assert.ok(result.includes('\n\n- '));
    });
});
