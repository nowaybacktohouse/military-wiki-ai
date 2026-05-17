package com.localchat.app.util

object FtsQuerySanitizer {

    private val FTS_SPECIAL = setOf('"', '\'', '*', ':', '(', ')', '-', '+', '~', '<', '>', '&', '|', '!', '^', '{', '}', '[', ']', '\\')

    fun sanitize(raw: String): String {
        if (raw.isBlank()) return ""
        val cleaned = raw.trim()
        if (cleaned.length < 2) return ""

        val sb = StringBuilder()
        for (ch in cleaned) {
            if (ch !in FTS_SPECIAL) {
                sb.append(ch)
            } else {
                sb.append(' ')
            }
        }

        val tokens = sb.toString()
            .split("\\s+".toRegex())
            .filter { it.isNotBlank() }

        if (tokens.isEmpty()) return ""

        return tokens.joinToString(" ") { "\"$it\"" }
    }
}
