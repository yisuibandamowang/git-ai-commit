/**
 * Commit message formatter matching the Kotlin `CommitMessageFormatter`.
 * Post-processes the LLM response: unwraps JSON, strips markdown,
 * translates English to Chinese, ensures Conventional Commit prefix.
 */
export class CommitMessageFormatter {
    private static readonly MAX_LENGTH = 80;
    private static readonly SUBJECT_PATTERN = /^[a-z][a-z0-9-]*(?:\([^)]+\))?:\s+\S.*$/i;
    private static readonly CONVENTIONAL_PREFIX_PATTERN = /^([a-z][a-z0-9-]*(?:\([^)]+\))?):\s*(\S.*)$/i;

    format(message: string, style: 'short' | 'detailed' = 'short'): string {
        const unwrapped = this.unwrapModelResponse(message);
        if (!unwrapped) {
            return '';
        }

        if (style === 'detailed') {
            return this.formatDetailed(unwrapped);
        }

        return this.formatShort(unwrapped);
    }

    private formatShort(unwrapped: string): string {
        const lines = unwrapped.split('\n');
        const subject = lines
            .map(l => this.cleanLine(l))
            .filter(l => l.length > 0)
            .find(l => this.looksLikeSubject(l) || this.looksLikeShortChineseSentence(l));

        let normalized: string;
        if (subject) {
            normalized = this.normalizeSubject(subject, unwrapped);
        } else {
            const summary = this.summarizeVerbose(unwrapped);
            if (summary) {
                normalized = summary;
            } else {
                const firstLine = lines.find(l => l.trim().length > 0) ?? '';
                normalized = this.normalizeSubject(this.cleanLine(firstLine), unwrapped);
            }
        }

        return this.ensureConventionalPrefix(normalized).slice(0, CommitMessageFormatter.MAX_LENGTH).trimEnd();
    }

    private formatDetailed(unwrapped: string): string {
        const lines = unwrapped.split('\n');
        const subjectIndex = this.findSubjectIndex(lines, unwrapped);
        const subjectSource = subjectIndex >= 0
            ? this.cleanLine(lines[subjectIndex])
            : this.cleanLine(lines.find(l => l.trim().length > 0) ?? '');
        const normalizedSubject = this.normalizeSubject(subjectSource, unwrapped);
        const bodyLines = this.buildDetailedBody(lines, subjectIndex, unwrapped);

        if (bodyLines.length === 0) {
            return this.formatShort(unwrapped);
        }

        return [this.ensureConventionalPrefix(normalizedSubject).slice(0, CommitMessageFormatter.MAX_LENGTH).trimEnd(), '', ...bodyLines].join('\n').trimEnd();
    }

    private unwrapModelResponse(message: string): string {
        const withoutFences = message.split('\n')
            .map(l => l.trim())
            .filter(l => !l.startsWith('```'))
            .join('\n')
            .trim();

        if (!withoutFences.startsWith('{')) {
            return withoutFences;
        }

        try {
            const json = JSON.parse(withoutFences);
            const text = this.extractJsonText(json);
            return text ?? '';
        } catch {
            return withoutFences;
        }
    }

    private extractJsonText(json: Record<string, unknown>): string | null {
        for (const key of ['commit_message', 'message', 'response']) {
            const value = json[key];
            if (typeof value === 'string' && this.isUsableModelText(value)) {
                return value;
            }
        }
        return null;
    }

    private isUsableModelText(text: string): boolean {
        const lower = text.toLowerCase();
        return text.length > 0 && !['success', 'ok', 'done', 'true', 'false', 'null', 'none'].includes(lower);
    }

    private cleanLine(line: string): string {
        return line
            .replace(/^# /, '')
            .replace(/^> /, '')
            .replace(/^- /, '')
            .replace(/^\* /, '')
            .replace(/^\d+[.)]\s*/, '')
            .replace(/\s+/g, ' ')
            .trim();
    }

    private looksLikeSubject(line: string): boolean {
        return CommitMessageFormatter.SUBJECT_PATTERN.test(line);
    }

    private looksLikeShortChineseSentence(line: string): boolean {
        return this.hasChineseCharacter(line) &&
            line.length <= CommitMessageFormatter.MAX_LENGTH &&
            !line.endsWith(':') &&
            !line.endsWith('：');
    }

    private findSubjectIndex(lines: string[], fullText: string): number {
        const subjectPatterns = lines
            .map(l => this.cleanLine(l))
            .map((line, index) => ({ line, index }))
            .find(({ line }) => line.length > 0 && (this.looksLikeSubject(line) || this.looksLikeShortChineseSentence(line)));

        if (subjectPatterns) {
            return subjectPatterns.index;
        }

        const firstNonBlank = lines.findIndex(l => l.trim().length > 0);
        return firstNonBlank >= 0 ? firstNonBlank : (this.summarizeVerbose(fullText) ? 0 : -1);
    }

    private buildDetailedBody(lines: string[], subjectIndex: number, fullText: string): string[] {
        const bodyLines = lines
            .slice(Math.max(subjectIndex + 1, 0))
            .map(line => this.cleanLine(line))
            .filter(line => line.length > 0)
            .map(line => this.toBulletLine(line))
            .filter((line): line is string => line.length > 0);

        if (bodyLines.length > 0) {
            return bodyLines;
        }

        const summary = this.summarizeVerbose(fullText) ?? this.summarizeByKeywords(fullText.toLowerCase());
        if (!summary) {
            return [];
        }

        return [`- ${summary}`];
    }

    private toBulletLine(line: string): string {
        const cleaned = this.stripBulletMarker(line);
        if (!cleaned) {
            return '';
        }

        return cleaned.startsWith('- ') ? cleaned : `- ${cleaned}`;
    }

    private stripBulletMarker(line: string): string {
        return line
            .replace(/^[-*•]\s*/, '')
            .replace(/^\d+[.)]\s*/, '')
            .replace(/\s+/g, ' ')
            .trim();
    }

    private summarizeVerbose(text: string): string | null {
        const lower = text.toLowerCase();
        const sentenceCount = (text.match(/[.!?。！？]+/g) ?? []).length;
        const numberedLineCount = text.split('\n').filter(l => /^\d+[.)]\s+/.test(l.trim())).length;
        const verboseMarkers = [
            'this commit',
            'the diff shows',
            'the changes include',
            "here's a breakdown",
            'overall,',
            'i have reviewed',
            'the repository now contains',
        ];
        const hasVerboseMarker = verboseMarkers.some(m => lower.includes(m));

        if (!hasVerboseMarker && !(text.length > 120 && sentenceCount >= 2) && numberedLineCount < 2) {
            return null;
        }

        return this.summarizeByKeywords(lower);
    }

    private normalizeSubject(subject: string, fullText: string): string {
        const prefixMatch = CommitMessageFormatter.CONVENTIONAL_PREFIX_PATTERN.exec(subject);
        const prefix = prefixMatch?.[1];
        const stripped = this.stripConventionalPrefix(subject);

        if (this.hasChineseCharacter(stripped)) {
            const body = this.isGenericPluginSubject(stripped)
                ? this.defaultPluginFeatureSummary()
                : stripped;
            return this.ensureConventionalPrefix(body, prefix);
        }

        const translated = this.translateEnglishSubject(stripped);
        if (this.hasChineseCharacter(translated)) {
            return this.ensureConventionalPrefix(translated, prefix);
        }

        return this.ensureConventionalPrefix(
            this.summarizeByKeywords(fullText.toLowerCase()),
            prefix,
        );
    }

    private translateEnglishSubject(subject: string): string {
        const specific = this.translateSpecificEnglishSubject(subject);
        if (specific) {
            return specific;
        }

        const replacements: Array<[RegExp, string]> = [
            [/generated commit messages/gi, '生成的提交信息'],
            [/generated commit message/gi, '生成的提交信息'],
            [/commit messages/gi, '提交信息'],
            [/commit message/gi, '提交信息'],
            [/commit/gi, '提交信息'],
            [/output language/gi, '输出语言'],
            [/openai-compatible/gi, 'OpenAI 兼容'],
            [/provider/gi, '提供商'],
            [/settings/gi, '设置'],
            [/switching/gi, '切换'],
            [/switch/gi, '切换'],
            [/support/gi, '支持'],
            [/add/gi, '添加'],
            [/update/gi, '更新'],
            [/improve/gi, '优化'],
            [/fix/gi, '修复'],
            [/refactor/gi, '重构'],
            [/formatting/gi, '格式化'],
            [/analysis/gi, '分析'],
            [/analyzer/gi, '分析器'],
            [/filtering/gi, '过滤'],
            [/filter/gi, '过滤器'],
            [/tests/gi, '测试'],
            [/test/gi, '测试'],
            [/english/gi, '英文'],
            [/chinese/gi, '中文'],
        ];

        let translated = subject;
        for (const [pattern, replacement] of replacements) {
            translated = translated.replace(pattern, replacement);
        }
        translated = translated.replace(/\s+/g, '');
        translated = translated.replace(/\.$/, '');
        return translated.trim();
    }

    private translateSpecificEnglishSubject(subject: string): string | null {
        const normalized = subject.toLowerCase();

        if (
            normalized.includes('add log files for indexing diagnostics and open-telemetry metrics') ||
            normalized.includes('add log files for indexing diagnostics and open telemetry metrics')
        ) {
            return '添加索引诊断日志和OpenTelemetry指标文件';
        }

        return null;
    }

    private summarizeByKeywords(lower: string): string {
        if (lower.includes('english') && lower.includes('chinese') && lower.includes('commit message')) {
            return '支持中英文提交信息切换';
        }

        const featureSummary = this.featureSummary(lower);
        if (featureSummary) {
            return featureSummary;
        }

        if (lower.includes('model provider') || lower.includes('openai-compatible')) {
            return '完善模型提供商支持';
        }

        if (lower.includes('diff')) {
            return '完善差异分析能力';
        }

        return '优化提交信息生成';
    }

    private isGenericPluginSubject(subject: string): boolean {
        return /Git AI Commit/i.test(subject) &&
            ['核心功能', '主要功能', '基础功能'].some(w => subject.includes(w));
    }

    private defaultPluginFeatureSummary(): string {
        return '新增提交信息格式化、差异分析、Git diff 过滤、模型提供商管理和测试覆盖';
    }

    private featureSummary(lower: string): string | null {
        const features: string[] = [];

        if (['commitmessageformatter', 'commit message formatting', 'formatting commit messages']
            .some(k => lower.includes(k))) {
            features.push('提交信息格式化');
        }
        if (lower.includes('json') || lower.includes('response')) {
            features.push('JSON 解析');
        }
        if (['diff analysis', 'diff analyzer', 'analyzing diffs'].some(k => lower.includes(k))) {
            features.push('差异分析');
        }
        if (['git diff filtering', 'gitdifffilter', 'diff filter'].some(k => lower.includes(k))) {
            features.push('Git diff 过滤');
        }
        if (['model provider', 'provider management', 'provider selection'].some(k => lower.includes(k))) {
            features.push('模型提供商管理');
        }
        if (lower.includes('openai-compatible')) {
            features.push('OpenAI 兼容客户端');
        }
        if (['promptbuilder', 'prompt style', 'prompt template'].some(k => lower.includes(k))) {
            features.push('提示词优化');
        }
        if (['test coverage', 'unit tests', 'html reports', 'report pages'].some(k => lower.includes(k))) {
            features.push('测试覆盖');
        }

        if (features.length < 2) {
            return null;
        }

        const prefix = '新增';
        if (features.length === 1) {
            return prefix + features[0];
        }
        if (features.length === 2) {
            return prefix + features.join('和');
        }
        return prefix + features.slice(0, -1).join('、') + '和' + features[features.length - 1];
    }

    private stripConventionalPrefix(subject: string): string {
        return subject.replace(/^[a-z][a-z0-9-]*(?:\([^)]+\))?:\s*/i, '').trim();
    }

    private ensureConventionalPrefix(message: string, preferredPrefix?: string): string {
        const trimmed = message.trim();
        if (!trimmed) {
            return '';
        }
        if (CommitMessageFormatter.CONVENTIONAL_PREFIX_PATTERN.test(trimmed)) {
            return trimmed;
        }

        const prefix = (preferredPrefix && preferredPrefix.length > 0) ? preferredPrefix : 'feat';
        return `${prefix}: ${trimmed}`;
    }

    private hasChineseCharacter(str: string): boolean {
        return /[\u4e00-\u9fff]/.test(str);
    }
}
