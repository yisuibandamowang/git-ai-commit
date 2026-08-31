import { describe, it } from 'node:test';
import * as assert from 'node:assert';
import { readGitAiSettings } from '../src/commands/readGitAiSettings';

describe('readGitAiSettings', () => {
    it('should read detailed message style from configuration', () => {
        const config = {
            get<T>(key: string, defaultValue?: T): T | undefined {
                if (key === 'messageStyle') {
                    return 'detailed' as T;
                }
                return defaultValue;
            },
        } as Parameters<typeof readGitAiSettings>[0];

        const settings = readGitAiSettings({
            get: config.get,
        });

        assert.strictEqual(settings.messageStyle, 'detailed');
    });
});
