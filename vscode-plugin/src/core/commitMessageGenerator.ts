import { GitDiffReader } from '../git/gitDiffReader';
import { GitDiffFilter } from './diffFilter';
import { PromptBuilder } from './promptBuilder';
import { CommitMessageFormatter } from './commitMessageFormatter';
import { ModelProviderRegistry, GitAiSettings } from '../providers/modelProvider';
import { createDefaultRegistry } from '../providers/registry';

/**
 * Commit message generation result matching the Kotlin `CommitMessageGeneration` sealed class.
 */
export type CommitMessageGeneration =
    | { kind: 'success'; message: string }
    | { kind: 'emptyDiff' }
    | { kind: 'emptyResponse' };

/**
 * Commit message generator matching the Kotlin `CommitMessageGenerator`.
 * Orchestrates reading diff, filtering, prompt building, LLM invocation, and formatting.
 */
export class CommitMessageGenerator {
    private diffReader: GitDiffReader;
    private diffFilter: GitDiffFilter;
    private promptBuilder: PromptBuilder;
    private formatter: CommitMessageFormatter;
    private providerRegistry: ModelProviderRegistry;

    constructor(
        diffReader?: GitDiffReader,
        diffFilter?: GitDiffFilter,
        promptBuilder?: PromptBuilder,
        formatter?: CommitMessageFormatter,
        providerRegistry?: ModelProviderRegistry,
    ) {
        this.diffReader = diffReader ?? new GitDiffReader();
        this.diffFilter = diffFilter ?? new GitDiffFilter();
        this.promptBuilder = promptBuilder ?? new PromptBuilder();
        this.formatter = formatter ?? new CommitMessageFormatter();
        this.providerRegistry = providerRegistry ?? createDefaultRegistry();
    }

    async generate(
        repoPath: string,
        settings: GitAiSettings,
        onUpdate: (message: string) => void = () => {},
    ): Promise<CommitMessageGeneration> {
        const rawDiff = this.diffReader.readDiff(repoPath);
        if (!rawDiff) {
            return { kind: 'emptyDiff' };
        }

        const diff = this.diffFilter.filter(rawDiff);
        if (!diff) {
            return { kind: 'emptyDiff' };
        }

        const prompt = this.promptBuilder.build(diff, settings.promptStyle, settings.messageStyle);
        const selection = this.providerRegistry.select(settings);

        try {
            let streamedRaw = '';
            const rawMessage = await selection.provider.generateStream(
                selection.model,
                prompt,
                chunk => {
                    streamedRaw += chunk;
                    onUpdate(streamedRaw);
                },
            );
            const finalRaw = rawMessage || streamedRaw;
            const message = this.formatter.format(finalRaw, settings.messageStyle);
            if (!message) {
                return { kind: 'emptyResponse' };
            }
            onUpdate(message);
            return { kind: 'success', message };
        } catch (err) {
            throw err;
        }
    }
}
