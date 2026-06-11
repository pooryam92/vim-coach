package com.github.pooryam92.vimcoach.features.tips.application.ideavimrc

import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.ideavimrc.infra.IdeaVimRcFile
import java.io.IOException
import java.nio.file.Path

/**
 * Appends a tip's config line(s) to the user's .ideavimrc, keeping file I/O out of the
 * notification controller and making the flow unit-testable.
 */
class AddTipToIdeaVimRc(
    private val ideaVimRcFile: IdeaVimRcFile = IdeaVimRcFile()
) {
    fun add(tip: VimTip): Result {
        if (tip.config.isEmpty()) {
            return Result.Failed
        }
        val path = try {
            ideaVimRcFile.findOrCreate()
        } catch (ignored: IOException) {
            null
        } ?: return Result.Failed

        return try {
            val outcome = ideaVimRcFile.append(path, tip.config)
            if (outcome.addedSomething) {
                Result.Added(outcome.path, outcome.addedStartLine, outcome.addedLines.size)
            } else {
                Result.AlreadyPresent(outcome.path)
            }
        } catch (ignored: IOException) {
            Result.Failed
        }
    }

    sealed interface Result {
        /** [startLine] is the 0-based line of the first appended line; [lineCount] how many were added. */
        data class Added(val path: Path, val startLine: Int, val lineCount: Int) : Result
        data class AlreadyPresent(val path: Path) : Result
        data object Failed : Result
    }
}
