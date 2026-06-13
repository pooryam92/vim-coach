package com.github.pooryam92.vimcoach.features.tips.application.ideavimrc

import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.ui.notifications.TipNotificationFactory
import com.intellij.codeInsight.highlighting.HighlightManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import java.nio.file.Path

class TipIdeaVimRc(
    private val project: Project,
    private val notificationFactory: TipNotificationFactory,
    private val addTipToIdeaVimRc: AddTipToIdeaVimRc = AddTipToIdeaVimRc(project),
    private val reloadIdeaVimRc: (() -> Unit)? = null
) {
    fun getAction(tip: VimTip): (() -> Unit)? {
        if (tip.config.isEmpty() || !addTipToIdeaVimRc.isAvailable()) return null
        return { handle(tip) }
    }

    private fun handle(tip: VimTip) {
        when (val result = addTipToIdeaVimRc.add(tip)) {
            is AddTipToIdeaVimRc.Result.Added ->
                showUpdatedNotification(
                    TipNotificationFactory.TIP_ADDED_TO_IDEAVIMRC_TEXT,
                    result.path,
                    result.startLine,
                    result.lineCount,
                    onReloadIdeaVimRc = if (reloadAvailable()) ::triggerReload else null
                )
            is AddTipToIdeaVimRc.Result.AlreadyPresent ->
                showUpdatedNotification(TipNotificationFactory.TIP_ALREADY_IN_IDEAVIMRC_TEXT, result.path)
            AddTipToIdeaVimRc.Result.Failed ->
                notificationFactory.createAddToIdeaVimRcFailedNotification().notify(project)
        }
    }

    private fun showUpdatedNotification(
        message: String,
        path: Path,
        startLine: Int = -1,
        lineCount: Int = 0,
        onReloadIdeaVimRc: (() -> Unit)? = null
    ) {
        openIdeaVimRc(path, startLine, lineCount)
        notificationFactory
            .createAddedToIdeaVimRcNotification(message, onReloadIdeaVimRc)
            .notify(project)
    }

    private fun reloadAvailable(): Boolean =
        reloadIdeaVimRc != null ||
            ActionManager.getInstance().getAction("IdeaVim.ReloadVimRc.reload") != null

    private fun triggerReload() {
        val reload = reloadIdeaVimRc
        if (reload != null) {
            reload()
            notificationFactory.createAddedToIdeaVimRcNotification(
                TipNotificationFactory.TIP_RELOADED_IDEAVIMRC_TEXT
            ).notify(project)
            return
        }
        val action = ActionManager.getInstance().getAction("IdeaVim.ReloadVimRc.reload")
        if (action == null) {
            notificationFactory.createReloadIdeaVimRcFailedNotification().notify(project)
            return
        }
        ActionManager.getInstance().tryToExecute(action, null, null, ActionPlaces.NOTIFICATION, true)
        notificationFactory.createAddedToIdeaVimRcNotification(
            TipNotificationFactory.TIP_RELOADED_IDEAVIMRC_TEXT
        ).notify(project)
    }

    private fun openIdeaVimRc(path: Path, startLine: Int, lineCount: Int) {
        val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path) ?: return
        if (startLine < 0 || lineCount <= 0) {
            FileEditorManager.getInstance(project).openFile(virtualFile, true)
            return
        }
        val descriptor = OpenFileDescriptor(project, virtualFile, startLine, 0)
        val editor = FileEditorManager.getInstance(project).openTextEditor(descriptor, true) ?: return
        highlightAppendedLines(editor, startLine, lineCount)
    }

    private fun highlightAppendedLines(editor: Editor, startLine: Int, lineCount: Int) {
        val document = editor.document
        if (startLine >= document.lineCount) return
        val lastLine = (startLine + lineCount - 1).coerceAtMost(document.lineCount - 1)
        val startOffset = document.getLineStartOffset(startLine)
        val endOffset = document.getLineEndOffset(lastLine)
        HighlightManager.getInstance(project).addRangeHighlight(
            editor,
            startOffset,
            endOffset,
            EditorColors.SEARCH_RESULT_ATTRIBUTES,
            true,
            null
        )
    }
}
