package com.gitai.commit

class PromptBuilder {
    private val analyzer = DiffAnalyzer()

    fun build(diff: String, style: String): String {
        val summary = analyzer.analyze(diff)
        return """
            你是一位资深工程师，擅长根据 git diff 生成简洁、准确的中文提交信息。
            先阅读结构化变更摘要，再判断高层意图、影响范围和提交类型，最后只输出一条最终提交信息。
            风格：$style

            规则：
            - 只输出最终提交信息，不要解释。
            - 优先使用 conventional commit 形式，并在范围明确时带 scope。
            - 如果涉及测试、配置、重命名或重构，要在提交信息里体现。
            - 主标题尽量短；只有正文真正增加信息时才写正文。

            结构化变更摘要：
            ${summary.renderForPrompt()}

            提交正文建议：
            - 如果变更由多个子项组成，正文用 1-3 行短 bullet 补充范围、风险或验证方式。
            - 不要重复原始 diff hunk。
            - 如果 test 文件变化很多，优先描述验证能力变化，而不是逐行复述。

            原始 diff：
            $diff
        """.trimIndent()
    }
}
