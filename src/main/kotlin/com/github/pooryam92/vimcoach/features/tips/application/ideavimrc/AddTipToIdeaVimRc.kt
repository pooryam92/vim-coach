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
    private val findPath: () -> Path? = {
        serviceOrNull<FindIdeaVimRc>()?.findVimRc()
    }
) {
    // Only available when the user already has a vimrc — we never create one for them.
    fun isAvailable(): Boolean = findPath() != null

    fun add(tip: VimTip): Result {
        val path = findPath() ?: return Result.Failed(FailureReason.NotAccessible)
        val vf = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path)
            ?: return Result.Failed(FailureReason.NotAccessible)
        if (!vf.isWritable) return Result.Failed(FailureReason.ReadOnly)
        val doc = FileDocumentManager.getInstance().getDocument(vf)
            ?: return Result.Failed(FailureReason.NotAccessible)

        val existingText = ApplicationManager.getApplication().runReadAction(Computable { doc.text })
        return when (val plan = IdeaVimRcAppendPlan.determine(existingText, tip.config?.lines ?: emptyList())) {
            IdeaVimRcAppendPlan.Plan.Empty -> Result.Failed(FailureReason.NothingToAdd)
            is IdeaVimRcAppendPlan.Plan.AlreadyPresent -> Result.AlreadyPresent(path, plan.startLine, plan.lineCount)
            is IdeaVimRcAppendPlan.Plan.Append -> {
                appendAndSave(doc, plan.insertText)
                Result.Added(path, plan.startLine, plan.addedCount)
            }
        }
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
