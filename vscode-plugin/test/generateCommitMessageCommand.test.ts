import { describe, it } from 'node:test';
import * as assert from 'node:assert';
import { readGitAiSettings } from '../src/commands/generateCommitMessage';

describe('readGitAiSettings', () => {
    it('should read detailed message style from configuration', () => {
        const settings = readGitAiSettings({
            get<T>(key: string, defaultValue: T): T {
                if (key === 'messageStyle') {
                    return 'detailed' as T;
                }
                return defaultValue;
            },
        });

        assert.strictEqual(settings.messageStyle, 'detailed');
    });
});
