import { describe, it } from 'node:test';
import * as assert from 'node:assert';
import { GitDiffFilter } from '../src/core/diffFilter';

describe('GitDiffFilter', () => {
    const filter = new GitDiffFilter();

    it('should return empty string for empty diff', () => {
        assert.strictEqual(filter.filter(''), '');
    });

    it('should keep normal source files', () => {
        const diff = [
            'diff --git a/src/main.kt b/src/main.kt',
            '--- a/src/main.kt',
            '+++ b/src/main.kt',
            '+fun hello() = "world"',
        ].join('\n');

        const result = filter.filter(diff);
        assert.ok(result.includes('src/main.kt'));
        assert.ok(result.includes('hello()'));
    });

    it('should filter out build directory files', () => {
        const diff = [
            'diff --git a/src/main.kt b/src/main.kt',
            '--- a/src/main.kt',
            '+++ b/src/main.kt',
            '+fun hello() = "world"',
            'diff --git a/idea-plugin/build/output.txt b/idea-plugin/build/output.txt',
            '--- a/idea-plugin/build/output.txt',
            '+++ b/idea-plugin/build/output.txt',
            '+build output',
        ].join('\n');

        const result = filter.filter(diff);
        assert.ok(result.includes('src/main.kt'));
        assert.ok(!result.includes('build/output.txt'));
    });

    it('should filter out .gradle directory files', () => {
        const diff = [
            'diff --git a/idea-plugin/.gradle/cache.bin b/idea-plugin/.gradle/cache.bin',
            '--- a/idea-plugin/.gradle/cache.bin',
            '+++ b/idea-plugin/.gradle/cache.bin',
            '+cache data',
        ].join('\n');

        const result = filter.filter(diff);
        assert.ok(!result.includes('.gradle/cache.bin'));
    });

    it('should filter out node_modules files', () => {
        const diff = [
            'diff --git a/project/node_modules/pkg/index.js b/project/node_modules/pkg/index.js',
            '--- a/project/node_modules/pkg/index.js',
            '+++ b/project/node_modules/pkg/index.js',
            '+code',
        ].join('\n');

        const result = filter.filter(diff);
        assert.ok(!result.includes('node_modules/pkg'));
    });

    it('should filter out .intellijPlatform files', () => {
        const diff = [
            'diff --git a/idea-plugin/.intellijPlatform/sandbox/log.txt b/idea-plugin/.intellijPlatform/sandbox/log.txt',
            '--- a/idea-plugin/.intellijPlatform/sandbox/log.txt',
            '+++ b/idea-plugin/.intellijPlatform/sandbox/log.txt',
            '+log',
        ].join('\n');

        const result = filter.filter(diff);
        assert.ok(!result.includes('.intellijPlatform'));
    });
});