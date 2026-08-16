package com.gitai.commit

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

object CommitMessageFormatter {
    private const val maxLength = 30
    private val objectMapper = ObjectMapper()
    private val subjectPattern = Regex("""(?i)^[a-z][a-z0-9-]*(?:\([^)]+\))?:\s+\S.*$""")

    fun format(message: String): String {
        val unwrapped = unwrapModelResponse(message)
        if (unwrapped.isBlank()) return ""

        val subject = unwrapped.lineSequence()
            .map { cleanLine(it) }
            .filter { it.isNotBlank() }
            .firstOrNull { looksLikeSubject(it) || looksLikeShortChineseSentence(it) }

        val normalized = subject
            ?.let { normalizeSubject(it, unwrapped) }
            ?: summarizeVerbose(unwrapped)
            ?: normalizeSubject(cleanLine(unwrapped.lineSequence().firstOrNull { it.isNotBlank() }.orEmpty()), unwrapped)

        return normalized.take(maxLength).trimEnd()
    }

    private fun unwrapModelResponse(message: String): String {
        val withoutFences = message.lineSequence()
            .map { it.trim() }
            .filter { !it.startsWith("```") }
            .joinToString("\n")
            .trim()

        if (!withoutFences.startsWith("{")) return withoutFences

        return runCatching {
            val json = objectMapper.readTree(withoutFences)
            extractJsonText(json).orEmpty()
        }.getOrElse { withoutFences }
    }

    private fun extractJsonText(json: JsonNode): String? =
        listOf("commit_message", "message", "response")
            .firstNotNullOfOrNull { key ->
                json.path(key).asText("").trim().takeIf { isUsableModelText(it) }
            }

    private fun isUsableModelText(text: String): Boolean {
        val lower = text.lowercase()
        return text.isNotBlank() && lower !in setOf("success", "ok", "done", "true", "false", "null", "none")
    }

    private fun cleanLine(line: String): String =
        line.removePrefix("# ")
            .removePrefix("> ")
            .removePrefix("- ")
            .removePrefix("* ")
            .replace(Regex("""^\d+[.)]\s*"""), "")
            .replace(Regex("""\s+"""), " ")
            .trim()

    private fun looksLikeSubject(line: String): Boolean = subjectPattern.matches(line)

    private fun looksLikeShortChineseSentence(line: String): Boolean =
        line.any(::isChineseCharacter) &&
            line.length <= maxLength &&
            !line.endsWith(":") &&
            !line.endsWith("：")

    private fun summarizeVerbose(text: String): String? {
        val lower = text.lowercase()
        val sentenceCount = Regex("""[.!?。！？]+""").findAll(text).count()
        val numberedLineCount = text.lineSequence().count { it.trim().matches(Regex("""\d+[.)]\s+.*""")) }
        val hasVerboseMarker = listOf(
            "this commit",
            "the diff shows",
            "the changes include",
            "here's a breakdown",
            "overall,",
            "i have reviewed",
            "the repository now contains"
        ).any { lower.contains(it) }

        if (!hasVerboseMarker && !(text.length > 120 && sentenceCount >= 2) && numberedLineCount < 2) {
            return null
        }

        return summarizeByKeywords(lower)
    }

    private fun normalizeSubject(subject: String, fullText: String): String {
        val stripped = stripConventionalPrefix(subject)
        if (stripped.any(::isChineseCharacter)) return stripped

        val translated = translateEnglishSubject(stripped)
        if (translated.any(::isChineseCharacter)) return translated

        return summarizeByKeywords(fullText.lowercase())
    }

    private fun translateEnglishSubject(subject: String): String {
        var translated = subject
            .replace("generated commit messages", "生成的提交信息", ignoreCase = true)
            .replace("generated commit message", "生成的提交信息", ignoreCase = true)
            .replace("commit messages", "提交信息", ignoreCase = true)
            .replace("commit message", "提交信息", ignoreCase = true)
            .replace("commit", "提交信息", ignoreCase = true)
            .replace("output language", "输出语言", ignoreCase = true)
            .replace("openai-compatible", "OpenAI 兼容", ignoreCase = true)
            .replace("provider", "提供商", ignoreCase = true)
            .replace("settings", "设置", ignoreCase = true)
            .replace("switching", "切换", ignoreCase = true)
            .replace("switch", "切换", ignoreCase = true)
            .replace("support", "支持", ignoreCase = true)
            .replace("add", "添加", ignoreCase = true)
            .replace("update", "更新", ignoreCase = true)
            .replace("improve", "优化", ignoreCase = true)
            .replace("fix", "修复", ignoreCase = true)
            .replace("refactor", "重构", ignoreCase = true)
            .replace("formatting", "格式化", ignoreCase = true)
            .replace("analysis", "分析", ignoreCase = true)
            .replace("analyzer", "分析器", ignoreCase = true)
            .replace("filtering", "过滤", ignoreCase = true)
            .replace("filter", "过滤器", ignoreCase = true)
            .replace("tests", "测试", ignoreCase = true)
            .replace("test", "测试", ignoreCase = true)
            .replace("english", "英文", ignoreCase = true)
            .replace("chinese", "中文", ignoreCase = true)
            .replace(Regex("""\s+"""), "")
            .trim()

        translated = translated.removeSuffix(".")
        return translated
    }

    private fun summarizeByKeywords(lower: String): String = when {
        lower.contains("english") && lower.contains("chinese") && lower.contains("commit message") ->
            "支持中英文提交信息切换"

        listOf(
            "commitmessageformatter",
            "commit message formatting",
            "diff analysis",
            "diff analyzer",
            "git diff filtering",
            "diff filter",
            "model provider",
            "openai-compatible",
            "test coverage",
            "html report"
        ).count { lower.contains(it) } >= 2 ->
            "完善提交信息生成、分析和测试能力"

        lower.contains("model provider") || lower.contains("openai-compatible") ->
            "完善模型提供商支持"

        lower.contains("diff") ->
            "完善差异分析能力"

        else ->
            "优化提交信息生成"
    }

    private fun stripConventionalPrefix(subject: String): String =
        subject.replace(Regex("""(?i)^[a-z][a-z0-9-]*(?:\([^)]+\))?:\s*"""), "").trim()

    private fun isChineseCharacter(char: Char): Boolean = char in '\u4e00'..'\u9fff'
}
