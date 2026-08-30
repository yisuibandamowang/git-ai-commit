import * as vscode from 'vscode';
import { GenerateCommitMessageCommand } from './commands/generateCommitMessage';

/**
 * Extension entry point.
 * Called when the extension is activated (onStartupFinished).
 */
export function activate(context: vscode.ExtensionContext): void {
    const command = new GenerateCommitMessageCommand();

    const disposable = vscode.commands.registerCommand(
        'git-ai-commit.generateCommitMessage',
        () => command.execute(),
    );

    context.subscriptions.push(disposable);
}

/**
 * Extension deactivation.
 */
export function deactivate(): void {
    // Cleanup if needed
}