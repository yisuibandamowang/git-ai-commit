package com.gitai.commit

class PromptBuilder {
    private val analyzer = DiffAnalyzer()

    fun build(diff: String, style: String): String {
        val summary = analyzer.analyze(diff)
        return """
            你是一位资深工程师，擅长根据 git diff 生成一句中文提交信息。
            风格：$style

            规则：
            - 只输出一句中文，不要标题、不要分点、不要正文。
            - 不要输出 JSON、英文提交信息或多行内容。
            - 第一行必须直接是最终提交信息，不要前言，不要解释，不要总结 patchset。
            - 长度尽量控制在30字以内。
            - 如果涉及测试、配置、重命名或重构，要在提交信息里体现。
            - 不要使用“核心功能”“主要功能”“完善能力”这类泛化描述，要点出具体模块或能力。

            结构化变更摘要：
            ${summary.renderForPrompt()}

            原始 diff：
            $diff
        """.trimIndent()
    }
}
