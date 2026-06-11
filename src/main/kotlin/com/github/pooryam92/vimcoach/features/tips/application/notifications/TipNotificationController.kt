package com.github.pooryam92.vimcoach.features.tips.application.notifications

import com.github.pooryam92.vimcoach.features.tips.application.ideavimrc.AddTipToIdeaVimRc
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.state.VimCoachSettingsService
import com.github.pooryam92.vimcoach.features.tips.state.VimTipService
import com.github.pooryam92.vimcoach.features.tips.ui.notifications.TipNotificationActions
import com.github.pooryam92.vimcoach.features.tips.ui.notifications.TipNotificationFactory
import com.github.pooryam92.vimcoach.features.tips.ui.settings.VimCoachSettingsConfigurable
import com.intellij.codeInsight.highlighting.HighlightManager
import com.intellij.notification.Notification
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import java.nio.file.Path

class TipNotificationController(
    private val project: Project
) : ShowTips {

    private var injectedNotificationFactory: TipNotificationFactory = TipNotificationFactory()
    private var injectedSettingsService: VimCoachSettingsService? = null
    private var injectedTipService: VimTipService? = null
    private var injectedAddTipToIdeaVimRc: AddTipToIdeaVimRc = AddTipToIdeaVimRc()
    private val activeTipNotifications = ActiveTipNotificationTracker(project)

    constructor(
        project: Project,
        tipService: VimTipService,
        notificationFactory: TipNotificationFactory,
        settingsService: VimCoachSettingsService? = null,
        addTipToIdeaVimRc: AddTipToIdeaVimRc = AddTipToIdeaVimRc()
    ) : this(project) {
        injectedNotificationFactory = notificationFactory
        injectedSettingsService = settingsService
        injectedTipService = tipService
        injectedAddTipToIdeaVimRc = addTipToIdeaVimRc
    }

    internal val activeNotification: Notification?
        get() = activeTipNotifications.current

    override fun showRandomTip() {
        showTip(selectRandomTip())
    }

    override fun showRandomTipIfNoneActive(): Boolean {
        if (activeTipNotifications.hasVisibleNotification()) {
            return false
        }
        showTip(selectRandomTip())
        return true
    }

    private fun showTip(tip: VimTip) {
        val notification = createNotificationWithActions(tip)
        activeTipNotifications.replaceWith(notification)
        notification.notify(project)
    }

    private fun createNotificationWithActions(tip: VimTip): Notification {
        return injectedNotificationFactory.createNotificationWithActions(tip, notificationActions(tip))
    }

    private fun notificationActions(tip: VimTip): TipNotificationActions {
        return TipNotificationActions(
            onShowNextTip = ::showRandomTip,
            onExcludeTip = { notification -> excludeTipFromFutureNotifications(tip, notification) },
            onAddToIdeaVimRc = if (tip.config.isNotEmpty()) {
                { addTipToIdeaVimRc(tip) }
            } else {
                null
            }
        )
    }

    private fun addTipToIdeaVimRc(tip: VimTip) {
        when (val result = injectedAddTipToIdeaVimRc.add(tip)) {
            is AddTipToIdeaVimRc.Result.Added ->
                showIdeaVimRcUpdatedNotification(
                    TipNotificationFactory.TIP_ADDED_TO_IDEAVIMRC_TEXT,
                    result.path,
                    result.startLine,
                    result.lineCount
                )

            is AddTipToIdeaVimRc.Result.AlreadyPresent ->
                showIdeaVimRcUpdatedNotification(TipNotificationFactory.TIP_ALREADY_IN_IDEAVIMRC_TEXT, result.path)

            AddTipToIdeaVimRc.Result.Failed ->
                injectedNotificationFactory.createAddToIdeaVimRcFailedNotification().notify(project)
        }
    }

    private fun showIdeaVimRcUpdatedNotification(
        message: String,
        path: Path,
        startLine: Int = -1,
        lineCount: Int = 0
    ) {
        openIdeaVimRc(path, startLine, lineCount)
        injectedNotificationFactory
            .createAddedToIdeaVimRcNotification(message)
            .notify(project)
    }

    private fun openIdeaVimRc(path: Path, startLine: Int, lineCount: Int) {
        val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path) ?: return
        if (startLine < 0 || lineCount <= 0) {
            FileEditorManager.getInstance(project).openFile(virtualFile, true)
            return
        }
        val descriptor = OpenFileDescriptor(project, virtualFile, startLine, 0)
        val editor = FileEditorManager.getInstance(project).openTextEditor(descriptor, true)
            ?: return
        highlightAppendedLines(editor, startLine, lineCount)
    }

    /** Briefly flashes the just-appended line(s) so the user can see exactly what changed. */
    private fun highlightAppendedLines(editor: Editor, startLine: Int, lineCount: Int) {
        val document = editor.document
        if (startLine >= document.lineCount) {
            return
        }
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

    private fun excludeTipFromFutureNotifications(tip: VimTip, notification: Notification) {
        val result = ExcludeTipFromNotifications(settingsService()).exclude(tip)
        activeTipNotifications.expire(notification)
        if (result.shouldShowManagementHint) {
            injectedNotificationFactory
                .createTipExcludedNotification(::openSettings)
                .notify(project)
        }
    }

    private fun openSettings() {
        ShowSettingsUtil.getInstance().showSettingsDialog(project, VimCoachSettingsConfigurable::class.java)
    }

    private fun selectRandomTip(): VimTip {
        val availableCategories = tipService().getCategories().values
        if (availableCategories.isEmpty()) {
            return tipService().getRandomTip()
        }

        val enabledCategories = settingsService().getEnabledTipCategories(availableCategories)
        return tipService().getRandomTip(enabledCategories)
    }

    private fun settingsService(): VimCoachSettingsService = injectedSettingsService ?: service()

    private fun tipService(): VimTipService = injectedTipService ?: service()

}
