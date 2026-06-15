package com.github.pooryam92.vimcoach.features.tips.application.ideavimrc

/**
 * Given the current file text and a tip's config lines, decides whether the snippet needs
 * appending, the text to insert, and the 0-based line the first appended line will land on.
 *
 * A tip's config is treated as a single, indivisible snippet: it is either copied in full
 * (verbatim, preserving order and any repeated lines) or — when the whole block is already
 * present — left untouched. We deliberately do not append "just the missing lines"; a snippet
 * may rely on its lines being together and in order.
 *
 * Line/length math mirrors IntelliJ's Document: lineCount == number of '\n' + 1.
 */
internal object IdeaVimRcAppendPlan {

    sealed interface Plan {
        /** [startLine] is the 0-based line of the first appended line; [addedCount] how many were added. */
        data class Append(val insertText: String, val startLine: Int, val addedCount: Int) : Plan

        /** The whole snippet is already present (as a contiguous block, after trimming) — nothing to do. */
        data object AlreadyPresent : Plan

        /** No usable config lines (all blank). */
        data object Empty : Plan
    }

    fun determine(existingText: String, configLines: List<String>): Plan {
        val snippet = configLines.map(String::trim).filter(String::isNotEmpty)
        if (snippet.isEmpty()) return Plan.Empty

        if (containsBlock(existingText, snippet)) return Plan.AlreadyPresent

        val endsWithNewline = existingText.isEmpty() || existingText.endsWith('\n')
        val lineCount = existingText.count { it == '\n' } + 1
        val startLine = if (endsWithNewline) lineCount - 1 else lineCount

        val insertText = buildString {
            if (existingText.isNotEmpty() && !endsWithNewline) append('\n')
            snippet.forEach { append(it).append('\n') }
        }
        return Plan.Append(insertText, startLine, snippet.size)
    }

    /**
     * Whether [snippet] already appears in [existingText] as a contiguous run of (trimmed) lines,
     * in order. This is a coarse "already present" check that treats the snippet as one unit — a
     * snippet whose lines exist but are scattered or reordered will still be re-appended for now.
     */
    private fun containsBlock(existingText: String, snippet: List<String>): Boolean {
        val lines = existingText.lineSequence().map(String::trim).toList()
        if (snippet.size > lines.size) return false
        return (0..lines.size - snippet.size).any { start ->
            snippet.indices.all { lines[start + it] == snippet[it] }
        }
    }
}
