package com.github.pooryam92.vimcoach.features.tips.application.ideavimrc

import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.util.Computable
import com.intellij.openapi.components.serviceOrNull
import com.intellij.openapi.command.WriteCommandAction
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

        val cleaned = tip.config.map(String::trim).filter(String::isNotEmpty)
        if (cleaned.isEmpty()) return Result.Failed

        data class DocSnapshot(val existingLines: Set<String>, val textLength: Int, val lineCount: Int, val endsWithNewline: Boolean)
        val snap = ApplicationManager.getApplication().runReadAction(Computable {
            val text = doc.text
            DocSnapshot(
                existingLines = text.lineSequence().map(String::trim).toHashSet(),
                textLength = doc.textLength,
                lineCount = doc.lineCount,
                endsWithNewline = text.isEmpty() || text.endsWith('\n')
            )
        })

        val toAdd = mutableListOf<String>()
        for (line in cleaned) {
            if (line !in snap.existingLines && line !in toAdd) toAdd.add(line)
        }

        if (toAdd.isEmpty()) return Result.AlreadyPresent(path)

        val startLine = if (snap.endsWithNewline) snap.lineCount - 1 else snap.lineCount
        val insertText = buildString {
            if (snap.textLength > 0 && !snap.endsWithNewline) append('\n')
            toAdd.forEach { append(it).append('\n') }
        }

        WriteCommandAction.runWriteCommandAction(project) {
            doc.insertString(doc.textLength, insertText)
        }
        WriteAction.run<Throwable> {
            FileDocumentManager.getInstance().saveDocument(doc)
        }

        return Result.Added(path, startLine, toAdd.size)
    }

    sealed interface Result {
        /** [startLine] is the 0-based line of the first appended line; [lineCount] how many were added. */
        data class Added(val path: Path, val startLine: Int, val lineCount: Int) : Result
        data class AlreadyPresent(val path: Path) : Result
        data object Failed : Result
    }
}
