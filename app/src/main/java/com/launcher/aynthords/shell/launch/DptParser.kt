package com.launcher.aynthords.shell.launch

/**
 * Parses Daijishou Player Template (.dpt) file content.
 * Format: first line "# Daijishou Player Template" or "# DST"; then "[tagName] value" per line.
 * Returns a map of tag names to values for {tags.tagName} substitution.
 * Does not depend on Android or Compose.
 */
object DptParser {

    private val HEADER_LINE = Regex("^#\\s*(?:Daijishou Player Template|DST)\\s*$", RegexOption.IGNORE_CASE)
    private val TAG_LINE = Regex("^\\s*\\[([^]]+)]\\s*(.*)$")

    /**
     * Parse .dpt file content and return tag map. Empty map if content is invalid or empty.
     */
    fun parse(content: String): Map<String, String> {
        val lines = content.lineSequence().toList()
        if (lines.isEmpty()) return emptyMap()

        val first = lines.first().trim()
        if (!HEADER_LINE.matches(first)) return emptyMap()

        val tags = mutableMapOf<String, String>()
        for (i in 1 until lines.size) {
            val match = TAG_LINE.find(lines[i]) ?: continue
            val (_, key, value) = match.groupValues
            if (key.isNotBlank()) {
                tags[key.trim()] = value.trim()
            }
        }
        return tags
    }
}
