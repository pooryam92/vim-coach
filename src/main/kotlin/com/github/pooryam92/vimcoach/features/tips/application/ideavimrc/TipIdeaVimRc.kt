package com.github.pooryam92.vimcoach.features.tips.application.ideavimrc

import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.ui.notifications.TipNotificationFactory
import com.intellij.codeInsight.highlighting.HighlightManager
import com.intellij.notification.Notification
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Computable
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.util.concurrency.AppExecutorUtil
import java.nio.file.Path
import java.util.concurrent.TimeUnit

private const val IDEAVIM_RELOAD_ACTION_ID = "IdeaVim.ReloadVimRc.reload"

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
            is AddTipToIdeaVimRc.Result.Added -> {
                var addedNotification: Notification? = null
                val reloadAction = if (reloadAvailable()) {
                    { addedNotification?.expire(); triggerReload() }
                } else null
                addedNotification = showUpdatedNotification(
                    TipNotificationFactory.TIP_ADDED_TO_IDEAVIMRC_TEXT,
                    result.path,
                    result.startLine,
                    result.lineCount,
                    onReloadIdeaVimRc = reloadAction
                )
            }
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
    ): Notification {
        openIdeaVimRc(path, startLine, lineCount)
        return notificationFactory
            .createAddedToIdeaVimRcNotification(message, onReloadIdeaVimRc)
            .also { it.notify(project) }
    }

    private fun reloadAvailable(): Boolean =
        reloadIdeaVimRc != null ||
            //noinspection ActionIsNotPreregistered
            ActionManager.getInstance().getAction(IDEAVIM_RELOAD_ACTION_ID) != null

    private fun triggerReload() {
        val reload = reloadIdeaVimRc
        if (reload != null) {
            reload()
            notificationFactory.createAddedToIdeaVimRcNotification(
                TipNotificationFactory.TIP_RELOADED_IDEAVIMRC_TEXT
            ).notify(project)
            return
        }
        //noinspection ActionIsNotPreregistered
        val action = ActionManager.getInstance().getAction(IDEAVIM_RELOAD_ACTION_ID)
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
        val offsets = ApplicationManager.getApplication().runReadAction(Computable {
            if (startLine >= document.lineCount) null
            else {
                val lastLine = (startLine + lineCount - 1).coerceAtMost(document.lineCount - 1)
                document.getLineStartOffset(startLine) to document.getLineEndOffset(lastLine)
            }
        }) ?: return
        val highlighters = mutableListOf<RangeHighlighter>()
        HighlightManager.getInstance(project).addRangeHighlight(
            editor,
            offsets.first,
            offsets.second,
            EditorColors.SEARCH_RESULT_ATTRIBUTES,
            true,
            highlighters
        )
        AppExecutorUtil.getAppScheduledExecutorService().schedule({
            ApplicationManager.getApplication().invokeLater {
                val highlightManager = HighlightManager.getInstance(project)
                highlighters.forEach { highlightManager.removeSegmentHighlighter(editor, it) }
            }
        }, 1000, TimeUnit.MILLISECONDS)
    }
}
