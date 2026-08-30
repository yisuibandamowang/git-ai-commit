/**
 * File diff summary matching the Kotlin `FileDiffSummary`.
 */
export interface FileDiffSummary {
    path: string;
    changeKind: string;
    fileRole: string;
    importantLines: string[];
}

/**
 * Diff summary matching the Kotlin `DiffSummary`.
 */
export class DiffSummary {
    constructor(public readonly files: FileDiffSummary[]) {}

    renderForPrompt(): string {
        if (this.files.length === 0) {
            return '未检测到文件变更';
        }

        return this.files.map(file => {
            const lines = file.importantLines.slice(0, 3).join('；') || '无关键片段';
            return `文件: ${file.path} | 类型: ${file.changeKind} | 角色: ${file.fileRole} | 关键片段: ${lines}`;
        }).join('\n');
    }
}

/**
 * Diff analyzer matching the Kotlin `DiffAnalyzer`.
 * Parses git diff output into structured file summaries.
 */
export class DiffAnalyzer {
    private static readonly DIFF_HEADER_PATTERN = /^diff --git a\/(.*?) b\/(.*)$/;

    analyze(diff: string): DiffSummary {
        const files: MutableFileDiffSummary[] = [];
        let current: MutableFileDiffSummary | null = null;

        for (const line of diff.split('\n')) {
            const match = DiffAnalyzer.DIFF_HEADER_PATTERN.exec(line);
            if (match) {
                current = new MutableFileDiffSummary(match[2]);
                files.push(current);
                continue;
            }

            const active = current;
            if (!active) {
                continue;
            }

            if (line.startsWith('new file mode')) {
                active.changeKind = '新增';
            } else if (line.startsWith('deleted file mode')) {
                active.changeKind = '删除';
            } else if (line.startsWith('rename from') || line.startsWith('rename to')) {
                active.changeKind = '重命名';
            } else if (line.startsWith('+') && !line.startsWith('+++')) {
                active.addImportantLine(line.slice(1).trim());
            } else if (line.startsWith('-') && !line.startsWith('---')) {
                active.addImportantLine(line.slice(1).trim());
            }
        }

        return new DiffSummary(files.map(f => f.toSummary()));
    }
}

class MutableFileDiffSummary {
    changeKind: string = '修改';
    private importantLines: string[] = [];

    constructor(private path: string) {}

    addImportantLine(line: string): void {
        if (line && this.importantLines.length < 8) {
            this.importantLines.push(line);
        }
    }

    toSummary(): FileDiffSummary {
        return {
            path: this.path,
            changeKind: this.changeKind,
            fileRole: classifyRole(this.path),
            importantLines: this.importantLines,
        };
    }
}

function classifyRole(path: string): string {
    if (/\/test\//i.test(path) || path.endsWith('Test.kt') || path.endsWith('.test.ts') || path.endsWith('.spec.ts')) {
        return '测试';
    }
    if (path.endsWith('.md')) {
        return '文档';
    }
    if (path.endsWith('.gradle') || path.endsWith('.kts') || /config/i.test(path)) {
        return '配置';
    }
    if (/\/build\//.test(path)) {
        return '构建';
    }
    return '代码';
}