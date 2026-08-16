package com.gitai.commit

data class FileDiffSummary(
    val path: String,
    val changeKind: String,
    val fileRole: String,
    val importantLines: List<String>
)

data class DiffSummary(
    val files: List<FileDiffSummary>
) {
    fun renderForPrompt(): String {
        if (files.isEmpty()) return "未检测到文件变更"

        return files.joinToString("\n") { file ->
            val lines = file.importantLines.take(3).joinToString("；").ifBlank { "无关键片段" }
            "文件: ${file.path} | 类型: ${file.changeKind} | 角色: ${file.fileRole} | 关键片段: $lines"
        }
    }
}

class DiffAnalyzer {
    fun analyze(diff: String): DiffSummary {
        val files = mutableListOf<MutableFileDiffSummary>()
        var current: MutableFileDiffSummary? = null

        diff.lineSequence().forEach { line ->
            val match = diffHeaderPattern.find(line)
            if (match != null) {
                current = MutableFileDiffSummary(match.groupValues[2])
                files += current!!
                return@forEach
            }

            val active = current ?: return@forEach
            when {
                line.startsWith("new file mode") -> active.changeKind = "新增"
                line.startsWith("deleted file mode") -> active.changeKind = "删除"
                line.startsWith("rename from") || line.startsWith("rename to") -> active.changeKind = "重命名"
                line.startsWith("+") && !line.startsWith("+++") -> active.addImportantLine(line.removePrefix("+").trim())
                line.startsWith("-") && !line.startsWith("---") -> active.addImportantLine(line.removePrefix("-").trim())
            }
        }

        return DiffSummary(files.map { it.toSummary() })
    }

    private class MutableFileDiffSummary(
        private val path: String
    ) {
        var changeKind: String = "修改"
        private val importantLines = mutableListOf<String>()

        fun addImportantLine(line: String) {
            if (line.isNotBlank() && importantLines.size < 8) {
                importantLines += line
            }
        }

        fun toSummary(): FileDiffSummary =
            FileDiffSummary(path, changeKind, classifyRole(path), importantLines)
    }

    companion object {
        private val diffHeaderPattern = Regex("""^diff --git a/(.*?) b/(.*)$""")

        private fun classifyRole(path: String): String = when {
            path.contains("/test/", ignoreCase = true) || path.endsWith("Test.kt") -> "测试"
            path.endsWith(".md") -> "文档"
            path.endsWith(".gradle") || path.endsWith(".kts") || path.contains("config", ignoreCase = true) -> "配置"
            path.contains("/build/") -> "构建"
            else -> "代码"
        }
    }
}
