import { describe, it } from 'node:test';
import * as assert from 'node:assert';
import { DiffAnalyzer } from '../src/core/diffAnalyzer';

describe('DiffAnalyzer', () => {
    const analyzer = new DiffAnalyzer();

    it('should return empty summary for empty diff', () => {
        const summary = analyzer.analyze('');
        assert.strictEqual(summary.files.length, 0);
        assert.strictEqual(summary.renderForPrompt(), '未检测到文件变更');
    });

    it('should parse single file diff', () => {
        const diff = [
            'diff --git a/src/main.kt b/src/main.kt',
            '--- a/src/main.kt',
            '+++ b/src/main.kt',
            '+fun hello() = "world"',
        ].join('\n');

        const summary = analyzer.analyze(diff);
        assert.strictEqual(summary.files.length, 1);
        assert.strictEqual(summary.files[0].path, 'src/main.kt');
        assert.strictEqual(summary.files[0].changeKind, '修改');
        assert.strictEqual(summary.files[0].fileRole, '代码');
    });

    it('should detect new files', () => {
        const diff = [
            'diff --git a/src/new.kt b/src/new.kt',
            'new file mode 100644',
            '--- /dev/null',
            '+++ b/src/new.kt',
            '+fun newFunction() = true',
        ].join('\n');

        const summary = analyzer.analyze(diff);
        assert.strictEqual(summary.files[0].changeKind, '新增');
    });

    it('should detect deleted files', () => {
        const diff = [
            'diff --git a/src/old.kt b/src/old.kt',
            'deleted file mode 100644',
            '--- a/src/old.kt',
            '+++ /dev/null',
            '-fun oldFunction() = false',
        ].join('\n');

        const summary = analyzer.analyze(diff);
        assert.strictEqual(summary.files[0].changeKind, '删除');
    });

    it('should detect renamed files', () => {
        const diff = [
            'diff --git a/src/old.kt b/src/new.kt',
            'rename from src/old.kt',
            'rename to src/new.kt',
        ].join('\n');

        const summary = analyzer.analyze(diff);
        assert.strictEqual(summary.files[0].changeKind, '重命名');
    });

    it('should classify test files', () => {
        const diff = [
            'diff --git a/src/test/FooTest.kt b/src/test/FooTest.kt',
            '--- a/src/test/FooTest.kt',
            '+++ b/src/test/FooTest.kt',
            '+assertTrue(true)',
        ].join('\n');

        const summary = analyzer.analyze(diff);
        assert.strictEqual(summary.files[0].fileRole, '测试');
    });

    it('should capture important lines', () => {
        const diff = [
            'diff --git a/src/main.kt b/src/main.kt',
            '--- a/src/main.kt',
            '+++ b/src/main.kt',
            '+fun hello() = "world"',
            '-fun oldHello() = "old"',
        ].join('\n');

        const summary = analyzer.analyze(diff);
        const lines = summary.files[0].importantLines;
        assert.ok(lines.some((l: string) => l.includes('hello()')));
    });

    it('should render for prompt correctly', () => {
        const diff = [
            'diff --git a/src/main.kt b/src/main.kt',
            '--- a/src/main.kt',
            '+++ b/src/main.kt',
            '+fun hello() = "world"',
        ].join('\n');

        const summary = analyzer.analyze(diff);
        const rendered = summary.renderForPrompt();
        assert.ok(rendered.includes('src/main.kt'));
        assert.ok(rendered.includes('修改'));
    });
});