/**
 * Git diff filter matching the Kotlin `GitDiffFilter`.
 * Filters out build artifacts and other noise from the diff.
 */
export class GitDiffFilter {
    private static readonly IGNORED_PATH_FRAGMENTS = [
        '/build/',
        '/.gradle/',
        '/.intellijPlatform/',
        '/build/reports/',
        '/build/tmp/',
        '/node_modules/',
        '/dist/',
        '/.next/',
        '/out/',
        '/.turbo/',
        '/coverage/',
        '/.nyc_output/',
    ];

    private static readonly DIFF_HEADER_PATTERN = /^diff --git a\/(.*?) b\/(.*)$/;

    filter(diff: string): string {
        const result: string[] = [];
        let keepCurrentFile = true;

        for (const line of diff.split('\n')) {
            const header = GitDiffFilter.DIFF_HEADER_PATTERN.exec(line);
            if (header) {
                const path = header[2];
                keepCurrentFile = !GitDiffFilter.IGNORED_PATH_FRAGMENTS.some(
                    fragment => path.includes(fragment),
                );
            }
            if (keepCurrentFile) {
                result.push(line);
            }
        }

        return result.join('\n').trim();
    }
}