package com.github.pooryam92.vimcoach.features.tips.application.ideavimrc

import com.github.pooryam92.vimcoach.features.tips.application.notifications.TipMessageHandle
import com.github.pooryam92.vimcoach.features.tips.application.notifications.TipNotifier
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.intellij.codeInsight.highlighting.HighlightManager
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

// IdeaVim's reload action id. Like the .ideavimrc search order in FindIdeaVimRc, this is an
// internal IdeaVim detail with no stability contract — if IdeaVim renames or removes it, the
// "Reload now" affordance silently disappears (reloadAvailable() returns false) rather than
// breaking. The external/ideavim submodule is the reference to check for drift.
private const val IDEAVIM_RELOAD_ACTION_ID = "IdeaVim.ReloadVimRc.reload"

/**
 * Handles the "Add to .ideavimrc" button on tip notifications.
 *
 * getAction() returns the button callback, or null if the button should not be shown
 * (tip has no config lines, or IdeaVim is not installed).
 *
 * On click:
 *   Added          → opens .ideavimrc at the appended lines with a brief highlight;
 *                    shows a "Reload now" affordance if IdeaVim reload is available.
 *   AlreadyPresent → opens .ideavimrc at the existing lines with a brief highlight,
 *                    reports it is already in.
 *   NoVimRc        → no .ideavimrc exists yet; guides the user to create one (we never
 *                    create it for them). The button works once the file exists.
 *   Failed         → reports a failure; file is not opened.
 *
 * Notifications go through the [TipNotifier] port; [project] is used only for editor IO.
 */
class TipIdeaVimRc(
    private val project: Project,
    private val notifier: TipNotifier,
    private val addTipToIdeaVimRc: AddTipToIdeaVimRc = AddTipToIdeaVimRc(project),
    private val reloadIdeaVimRc: (() -> Unit)? = null
) {
    fun getAction(tip: VimTip): (() -> Unit)? {
        if (tip.config?.lines.isNullOrEmpty() || !addTipToIdeaVimRc.isAvailable()) return null
        return { handle(tip) }
    }

    private fun handle(tip: VimTip) {
        when (val result = addTipToIdeaVimRc.add(tip)) {
            is AddTipToIdeaVimRc.Result.Added -> {
                val editor = openIdeaVimRcAtLine(result.path, result.startLine, result.lineCount)
                // Forward reference: onReload needs to dismiss the message, but the message handle
                // only exists once showAddedToIdeaVimRc has been called with onReload. The lambda is
                // not invoked until the user clicks "Reload now", by which point `message` is set.
                var message: TipMessageHandle? = null
                val onReload = if (reloadAvailable()) {
                    { message?.dismiss(); triggerReload(editor) }
                } else null
                message = notifier.showAddedToIdeaVimRc(onReload)
            }
            is AddTipToIdeaVimRc.Result.AlreadyPresent -> {
                openIdeaVimRcAtLine(result.path, result.startLine, result.lineCount)
                notifier.showAlreadyInIdeaVimRc()
            }
            is AddTipToIdeaVimRc.Result.NoVimRc ->
                notifier.showCreateIdeaVimRcGuidance()
            is AddTipToIdeaVimRc.Result.Failed ->
                notifier.showAddToIdeaVimRcFailed(result.reason)
        }
    }

    private fun reloadAvailable(): Boolean =
        reloadIdeaVimRc != null ||
            //noinspection ActionIsNotPreregistered
            ActionManager.getInstance().getAction(IDEAVIM_RELOAD_ACTION_ID) != null

    private fun triggerReload(editor: Editor?) {
        val injected = reloadIdeaVimRc
        if (injected != null) {
            injected()
            notifier.showReloadedIdeaVimRc()
            return
        }
        //noinspection ActionIsNotPreregistered
        val action = ActionManager.getInstance().getAction(IDEAVIM_RELOAD_ACTION_ID)
        if (action == null) {
            notifier.showReloadIdeaVimRcFailed()
            return
        }
        // IdeaVim's ReloadVimRc requires EDITOR and VIRTUAL_FILE in its data context and disables
        // itself otherwise. The click originates on the notification balloon, whose focus-based data
        // context (the null-component case) has neither — so the action would always be disabled and
        // the reload would silently fail. Pass the .ideavimrc editor we just opened as the context
        // component so the action sees the file it needs to reload.
        val contextComponent = editor?.takeUnless { it.isDisposed }?.contentComponent
        // tryToExecute runs synchronously (now = true) and rejects the callback when the action is
        // disabled. Only report success when the action actually ran, never unconditionally.
        val callback = ActionManager.getInstance()
            .tryToExecute(action, null, contextComponent, ActionPlaces.NOTIFICATION, true)
        if (callback.isDone) notifier.showReloadedIdeaVimRc() else notifier.showReloadIdeaVimRcFailed()
    }

    private fun openIdeaVimRcAtLine(path: Path, startLine: Int, lineCount: Int): Editor? {
        val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path) ?: return null
        val descriptor = OpenFileDescriptor(project, virtualFile, startLine, 0)
        val editor = FileEditorManager.getInstance(project).openTextEditor(descriptor, true) ?: return null
        highlightAppendedLines(editor, startLine, lineCount)
        return editor
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
            editor, offsets.first, offsets.second,
            EditorColors.SEARCH_RESULT_ATTRIBUTES, true, highlighters
        )
        AppExecutorUtil.getAppScheduledExecutorService().schedule({
            ApplicationManager.getApplication().invokeLater {
                // The project or editor may have been disposed during the 1s delay; touching a
                // disposed editor throws. Skip cleanup in that case — the highlighters die with it.
                if (project.isDisposed || editor.isDisposed) return@invokeLater
                val highlightManager = HighlightManager.getInstance(project)
                highlighters.forEach { highlightManager.removeSegmentHighlighter(editor, it) }
            }
        }, 1000, TimeUnit.MILLISECONDS)
    }
}
