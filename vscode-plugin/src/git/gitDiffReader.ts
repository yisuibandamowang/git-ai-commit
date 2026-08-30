import { execSync } from 'child_process';

/**
 * Git diff reader matching the Kotlin `GitDiffReader`.
 * Reads staged diff first, falls back to unstaged diff.
 */
export class GitDiffReader {
    readDiff(repoPath: string): string {
        if (!repoPath) {
            return '';
        }

        // Try staged diff first
        const staged = this.runGit(repoPath, 'diff', '--cached');
        if (staged) {
            return staged;
        }

        // Fall back to unstaged diff
        return this.runGit(repoPath, 'diff');
    }

    private runGit(repoPath: string, ...args: string[]): string {
        try {
            return execSync(
                `git -C "${repoPath}" ${args.join(' ')}`,
                { encoding: 'utf-8', maxBuffer: 10 * 1024 * 1024 },
            ).trim();
        } catch {
            return '';
        }
    }
}