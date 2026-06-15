package com.github.pooryam92.vimcoach.features.tips.application.ideavimrc

/**
 * Given the current file text and the tip's config lines, decides which lines need
 * appending, the text to insert, and the 0-based line the first appended line will land on.
 *
 * Line/length math mirrors IntelliJ's Document: lineCount == number of '\n' + 1.
 */
internal object IdeaVimRcAppendPlan {

    sealed interface Plan {
        /** [startLine] is the 0-based line of the first appended line; [addedCount] how many were added. */
        data class Append(val insertText: String, val startLine: Int, val addedCount: Int) : Plan

        /** Every config line is already present (after trimming) — nothing to do. */
        data object AlreadyPresent : Plan

        /** No usable config lines (all blank). */
        data object Empty : Plan
    }

    fun of(existingText: String, configLines: List<String>): Plan {
        val cleaned = configLines.map(String::trim).filter(String::isNotEmpty)
        if (cleaned.isEmpty()) return Plan.Empty

        val existingLines = existingText.lineSequence().map(String::trim).toHashSet()
        val toAdd = cleaned.distinct().filter { it !in existingLines }
        if (toAdd.isEmpty()) return Plan.AlreadyPresent

        val endsWithNewline = existingText.isEmpty() || existingText.endsWith('\n')
        val lineCount = existingText.count { it == '\n' } + 1
        val startLine = if (endsWithNewline) lineCount - 1 else lineCount

        val insertText = buildString {
            if (existingText.isNotEmpty() && !endsWithNewline) append('\n')
            toAdd.forEach { append(it).append('\n') }
        }
        return Plan.Append(insertText, startLine, toAdd.size)
    }
}
