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
        val path = findPath() ?: return Result.Failed
        val vf = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path) ?: return Result.Failed
        if (!vf.isWritable) return Result.Failed
        val doc = FileDocumentManager.getInstance().getDocument(vf) ?: return Result.Failed

        val existingText = ApplicationManager.getApplication().runReadAction(Computable { doc.text })
        return when (val plan = IdeaVimRcAppendPlan.of(existingText, tip.config)) {
            IdeaVimRcAppendPlan.Plan.Empty -> Result.Failed
            IdeaVimRcAppendPlan.Plan.AlreadyPresent -> Result.AlreadyPresent(path)
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
        data class AlreadyPresent(val path: Path) : Result
        data object Failed : Result
    }
}
