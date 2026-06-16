package com.launcher.aynthords.shell.launch

import android.content.ComponentName
import android.content.Intent
import android.net.Uri

/**
 * Builds an Android Intent from Daijishou-style am start arguments string.
 * Performs placeholder substitution for {file.path}, {file.uri}, and {tags.<name>}
 * before parsing. Does not depend on Compose or UI.
 */
object AmStartIntentBuilder {

    private val TAGS_PATTERN = Regex("\\{tags\\.([^}]+)}")

    /**
     * Substitute placeholders in [template]. [filePath] and [fileUri] replace
     * {file.path} and {file.uri}; [tags] replace {tags.<key>}.
     */
    fun substitute(
        template: String,
        filePath: String? = null,
        fileUri: String? = null,
        tags: Map<String, String> = emptyMap(),
    ): String {
        var s = template
        filePath?.let { s = s.replace("{file.path}", it) }
        fileUri?.let { s = s.replace("{file.uri}", it) }
        tags.forEach { (key, value) ->
            s = s.replace("{tags.$key}", value)
        }
        // Replace any remaining unresolved tags with empty string to avoid passing literal "{tags.xxx}"
        s = TAGS_PATTERN.replace(s) { "" }
        return s
    }

    /**
     * Parse substituted am start arguments into an Intent.
     * Supports -n (component), -a (action), -d (data URI), -e/--es/--ei/--ez (extras),
     * and --activity-clear-task / --activity-clear-top as intent flags.
     */
    fun parseToIntent(substitutedArgs: String): Intent {
        val intent = Intent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val lines = substitutedArgs.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

        for (line in lines) {
            val parts = line.split(Regex("\\s+"), limit = 4)
            val opt = parts.getOrNull(0) ?: continue

            when (opt) {
                "-n" -> {
                    val comp = parts.getOrNull(1) ?: continue
                    val (pkg, activity) = comp.split("/", limit = 2).let { p ->
                        if (p.size == 2) p[0] to p[1] else return@let null to null
                    }
                    if (pkg != null && activity != null) {
                        intent.component = ComponentName(pkg, activity)
                    }
                }
                "-a" -> intent.action = parts.getOrNull(1)
                "-d" -> parts.getOrNull(1)?.let { intent.data = Uri.parse(it) }
                "-e", "--es" -> {
                    val key = parts.getOrNull(1)
                    val value = parts.drop(2).joinToString(" ").trim().takeIf { it.isNotEmpty() }
                    if (key != null && value != null) intent.putExtra(key, value)
                }
                "--ei" -> {
                    val key = parts.getOrNull(1)
                    val value = parts.getOrNull(2)?.toIntOrNull()
                    if (key != null && value != null) intent.putExtra(key, value)
                }
                "--ez" -> {
                    val key = parts.getOrNull(1)
                    val value = parts.getOrNull(2)?.toBoolean()
                    if (key != null && value != null) intent.putExtra(key, value)
                }
                "--activity-clear-task" -> intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                "--activity-clear-top" -> intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                "--activity-single-top" -> intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
        }

        return intent
    }

    /**
     * Substitute placeholders in [template] and parse result into an Intent.
     */
    fun buildIntent(
        template: String,
        filePath: String? = null,
        fileUri: String? = null,
        tags: Map<String, String> = emptyMap(),
    ): Intent {
        val substituted = substitute(template, filePath, fileUri, tags)
        return parseToIntent(substituted)
    }
}
