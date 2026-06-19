package com.github.pooryam92.vimcoach.features.tips.application.ideavimrc

import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.util.Computable
import com.intellij.openapi.components.serviceOrNull
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import java.nio.file.Path

class AddTipToIdeaVimRc(
    private val project: Project,
    private val findService: () -> FindIdeaVimRc? = { serviceOrNull<FindIdeaVimRc>() }
) {
    // Available whenever IdeaVim is installed — its file-locator service is registered only then
    // (plugin-ideavim.xml). Independent of whether a .ideavimrc file exists yet: when it doesn't,
    // add() returns NoVimRc so the user can be guided to create one. We never create it ourselves.
    fun isAvailable(): Boolean = findService() != null

    fun add(tip: VimTip): Result {
        val service = findService() ?: return Result.Failed(FailureReason.NotAccessible)
        val path = service.findVimRc() ?: return Result.NoVimRc
        val vf = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path)
            ?: return Result.Failed(FailureReason.NotAccessible)
        if (!vf.isWritable) return Result.Failed(FailureReason.ReadOnly)
        val doc = FileDocumentManager.getInstance().getDocument(vf)
            ?: return Result.Failed(FailureReason.NotAccessible)

        val existingText = ApplicationManager.getApplication().runReadAction(Computable { doc.text })
        val stamp = stampFor(tip.config?.name)
        return when (val plan = IdeaVimRcAppendPlan.determine(existingText, tip.config?.lines ?: emptyList(), stamp)) {
            IdeaVimRcAppendPlan.Plan.Empty -> Result.Failed(FailureReason.NothingToAdd)
            is IdeaVimRcAppendPlan.Plan.AlreadyPresent -> Result.AlreadyPresent(path, plan.startLine, plan.lineCount)
            is IdeaVimRcAppendPlan.Plan.Append -> {
                appendAndSave(doc, plan.insertText)
                Result.Added(path, plan.startLine, plan.addedCount)
            }
        }
    }

    /**
     * The vimscript comment written above an appended snippet so the user can tell which lines
     * Vim Coach added (and find them later). When the tip's config has a [name] (e.g. a plugin
     * name) it is folded into the stamp for traceability; otherwise a generic stamp is used.
     */
    private fun stampFor(name: String?): String {
        val label = name?.trim()?.takeIf(String::isNotEmpty)
        return if (label != null) "\" $label — added by Vim Coach" else "\" Added by Vim Coach"
    }

    private fun appendAndSave(doc: Document, insertText: String) {
        WriteCommandAction.runWriteCommandAction(project) {
            doc.insertString(doc.textLength, insertText)
        }
        WriteAction.run<Throwable> {
            FileDocumentManager.getInstance().saveDocument(doc)
        }
    }

    sealed interface Result {
        /** [startLine] is the 0-based line of the first appended line; [lineCount] how many were added. */
        data class Added(val path: Path, val startLine: Int, val lineCount: Int) : Result

        /** [startLine] is the 0-based line where the existing block begins; [lineCount] how many it spans. */
        data class AlreadyPresent(val path: Path, val startLine: Int, val lineCount: Int) : Result

        /**
         * IdeaVim is installed but the user has no .ideavimrc yet. We deliberately do not create one
         * (see [AddTipToIdeaVimRc]); the caller guides the user to create it through IdeaVim instead.
         */
        data object NoVimRc : Result
        data class Failed(val reason: FailureReason) : Result
    }

    /** Why an add could not be performed — surfaced to the user so the failure isn't opaque. */
    enum class FailureReason {
        /** The .ideavimrc could not be located or opened (vanished, or no in-memory document). */
        NotAccessible,

        /** The .ideavimrc exists but is not writable (e.g. read-only file). */
        ReadOnly,

        /** The tip had no usable config lines to add (all blank). */
        NothingToAdd,
    }
}
