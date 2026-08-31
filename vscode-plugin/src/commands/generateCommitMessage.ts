import * as vscode from 'vscode';
import { CommitMessageGenerator } from '../core/commitMessageGenerator';
import { readGitAiSettings } from './readGitAiSettings';

/**
 * Command handler for generating commit messages.
 * Mirrors the Kotlin `CommitMessageAssistantAction` behavior.
 */
export class GenerateCommitMessageCommand {
    private generator = new CommitMessageGenerator();

    async execute(): Promise<void> {
        const gitExtension = vscode.extensions.getExtension<GitExtension>('vscode.git');
        if (!gitExtension) {
            vscode.window.showErrorMessage('Git extension not found. Please make sure Git is enabled.');
            return;
        }

        const git = gitExtension.exports.getAPI(1);
        const repo = git.repositories[0];
        if (!repo) {
            vscode.window.showWarningMessage('No git repository found. Open a folder with a git repository.');
            return;
        }

        const settings = readGitAiSettings(vscode.workspace.getConfiguration('git-ai-commit'));
        const repoPath = repo.rootUri.fsPath;

        await vscode.window.withProgress(
            {
                location: vscode.ProgressLocation.Notification,
                title: 'Generating commit message...',
                cancellable: false,
            },
            async () => {
                try {
                    const result = await this.generator.generate(repoPath, settings, message => {
                        repo.inputBox.value = message;
                    });

                    switch (result.kind) {
                        case 'emptyDiff':
                            vscode.window.showWarningMessage(
                                '没有检测到 git diff，先修改文件再试。',
                            );
                            break;

                        case 'emptyResponse':
                            vscode.window.showErrorMessage(
                                '模型没有返回有效 commit message。',
                            );
                            break;

                        case 'success':
                            repo.inputBox.value = result.message;
                            vscode.window.showInformationMessage(
                                '已生成并填入 commit message。',
                            );
                            break;
                    }
                } catch (err) {
                    const message = err instanceof Error ? err.message : String(err);
                    vscode.window.showErrorMessage(`调用模型失败：${message}`);
                }
            },
        );
    }

}

/**
 * Minimal Git extension API interface.
 */
interface GitExtension {
    getAPI(version: 1): GitAPI;
}

interface GitAPI {
    repositories: Repository[];
}

interface Repository {
    rootUri: vscode.Uri;
    inputBox: InputBox;
}

interface InputBox {
    value: string;
}
