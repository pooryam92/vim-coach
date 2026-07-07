package com.github.pooryam92.vimcoach.features.tips.ui.notifications

import com.github.pooryam92.vimcoach.features.tips.application.ideavimrc.AddTipToIdeaVimRc
import com.github.pooryam92.vimcoach.features.tips.application.notifications.TipActions
import com.github.pooryam92.vimcoach.features.tips.application.notifications.TipMessageHandle
import com.github.pooryam92.vimcoach.features.tips.application.notifications.TipNotifier
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.intellij.notification.Notification
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

/**
 * IntelliJ adapter for [TipNotifier]: renders notifications as platform balloons via
 * [TipNotificationFactory] and owns the live-notification lifecycle via [ActiveTipNotificationTracker].
 */
class IntelliJTipNotifier(private val project: Project) : TipNotifier {

    private val factory = TipNotificationFactory()
    private val activeTip = ActiveTipNotificationTracker(project)

    /** The currently-tracked tip notification, exposed for adapter-level UI tests. */
    internal val activeTipNotification: Notification?
        get() = activeTip.current

    override fun hasVisibleTip(): Boolean = activeTip.hasVisibleNotification()

    override fun showTip(tip: VimTip, actions: TipActions) {
        val notification = factory.createNotificationWithActions(tip, toFactoryActions(actions))
        activeTip.replaceWith(notification)
        notification.notify(project)
    }

    override fun showTipExcluded(onManage: () -> Unit) {
        factory.createTipExcludedNotification(onManage).notify(project)
    }

    override fun showAdvancedTipsAvailable(onOpenSettings: () -> Unit) {
        factory.createAdvancedTipsAvailableNotification(onOpenSettings).notify(project)
    }

    override fun showAddedToIdeaVimRc(onReload: (() -> Unit)?): TipMessageHandle {
        val notification = factory.createAddedToIdeaVimRcNotification(
            TipNotificationFactory.TIP_ADDED_TO_IDEAVIMRC_TEXT, onReload
        )
        notification.notify(project)
        return TipMessageHandle { notification.expire() }
    }

    override fun showAlreadyInIdeaVimRc() =
        message(factory.createAddedToIdeaVimRcNotification(TipNotificationFactory.TIP_ALREADY_IN_IDEAVIMRC_TEXT))

    override fun showCreateIdeaVimRcGuidance() =
        message(factory.createAddedToIdeaVimRcNotification(TipNotificationFactory.TIP_CREATE_IDEAVIMRC_GUIDANCE_TEXT))

    override fun showAddToIdeaVimRcFailed(reason: AddTipToIdeaVimRc.FailureReason) =
        message(factory.createAddToIdeaVimRcFailedNotification(reason))

    override fun showReloadedIdeaVimRc() =
        message(factory.createAddedToIdeaVimRcNotification(TipNotificationFactory.TIP_RELOADED_IDEAVIMRC_TEXT))

    override fun showReloadIdeaVimRcFailed() =
        message(factory.createReloadIdeaVimRcFailedNotification())

    private fun message(notification: Notification) = notification.notify(project)

    /**
     * Maps the application's [TipActions] onto the factory's action bundle. "Don't show again"
     * runs the application callback and then dismisses the tip notification — keeping dismissal
     * an adapter concern.
     */
    private fun toFactoryActions(actions: TipActions): TipNotificationActions =
        TipNotificationActions(
            onShowNextTip = actions.onShowNextTip,
            onExcludeTip = { notification ->
                actions.onExcludeTip()
                activeTip.expire(notification)
            },
            onAddToIdeaVimRc = actions.onAddToIdeaVimRc,
            onRecordNote = actions.onRecordNote?.let { record -> { promptAndRecordNote(record) } },
        )

    /** Asks the maintainer for a note, records it, then confirms where it was saved. */
    private fun promptAndRecordNote(record: (String) -> Unit) {
        val note = Messages.showMultilineInputDialog(
            project,
            TipNotificationFactory.TIP_NOTE_DIALOG_MESSAGE,
            TipNotificationFactory.TIP_NOTE_DIALOG_TITLE,
            null,
            null,
            null,
        )?.takeIf(String::isNotBlank) ?: return
        record(note)
        message(factory.createAddedToIdeaVimRcNotification(TipNotificationFactory.TIP_NOTE_SAVED_TEXT))
    }
}

private fun TipMessageHandle(onDismiss: () -> Unit): TipMessageHandle =
    object : TipMessageHandle {
        override fun dismiss() = onDismiss()
    }
