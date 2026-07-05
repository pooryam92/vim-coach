package com.github.pooryam92.vimcoach.features.tips.ui.notifications

import com.github.pooryam92.vimcoach.core.shared.i18n.MyBundle
import com.github.pooryam92.vimcoach.features.tips.application.ideavimrc.AddTipToIdeaVimRc
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.ColorUtil
import com.intellij.util.ui.UIUtil

class TipNotificationFactory {

    internal fun createNotificationWithActions(tip: VimTip, onShowNextTip: () -> Unit): Notification {
        return createNotificationWithActions(
            tip,
            TipNotificationActions(onShowNextTip = onShowNextTip)
        )
    }

    internal fun createNotificationWithActions(tip: VimTip, actions: TipNotificationActions): Notification {
        val notification = createNotification(tip)
        actions.onShowNextTip?.let { onShowNextTip ->
            notification.addAction(NotificationAction.createSimple(TIP_NEXT_ACTION_TEXT) {
                onShowNextTip()
            })
        }
        actions.onExcludeTip?.let { onExcludeTip ->
            notification.addAction(NotificationAction.createSimple(TIP_DONT_SHOW_AGAIN_ACTION_TEXT) {
                onExcludeTip(notification)
            })
        }
        actions.onAddToIdeaVimRc?.let { onAddToIdeaVimRc ->
            notification.addAction(NotificationAction.createSimple(addToIdeaVimRcActionText(tip)) {
                onAddToIdeaVimRc()
            })
        }
        actions.onRecordNote?.let { onRecordNote ->
            notification.addAction(NotificationAction.createSimple(TIP_NOTE_ACTION_TEXT) {
                onRecordNote()
            })
        }
        return notification
    }

    internal fun createNotification(tip: VimTip): Notification {
        return Notification(
            NOTIFICATION_GROUP_ID,
            notificationTitle(tip),
            renderTipAsHtml(tip),
            NotificationType.INFORMATION
        ).apply {
            icon = TIP_ICON
        }
    }

    // Advanced tips carry a black-diamond suffix (the ski-slope "expert run" convention) so opted-in
    // users can tell an advanced tip at a glance without an in-app legend.
    private fun notificationTitle(tip: VimTip): String {
        return if (tip.advanced) "$APP_TITLE $ADVANCED_TITLE_MARKER" else APP_TITLE
    }

    /** A named config uses its name verbatim as the apply button label; otherwise it stays generic. */
    private fun addToIdeaVimRcActionText(tip: VimTip): String {
        return tip.config?.name?.takeIf(String::isNotBlank) ?: TIP_ADD_TO_IDEAVIMRC_ACTION_TEXT
    }

    internal fun createTipExcludedNotification(onOpenSettings: () -> Unit): Notification {
        return settingsPointerNotification(
            TIP_EXCLUDED_WITH_MANAGEMENT_TEXT,
            TIP_MANAGE_EXCLUDED_ACTION_TEXT,
            onOpenSettings
        )
    }

    internal fun createAdvancedTipsAvailableNotification(onOpenSettings: () -> Unit): Notification {
        return settingsPointerNotification(
            ADVANCED_TIPS_AVAILABLE_TEXT,
            ADVANCED_TIPS_OPEN_SETTINGS_ACTION_TEXT,
            onOpenSettings
        )
    }

    private fun settingsPointerNotification(
        text: String,
        actionText: String,
        onOpenSettings: () -> Unit
    ): Notification {
        return Notification(
            NOTIFICATION_GROUP_ID,
            APP_TITLE,
            text,
            NotificationType.INFORMATION
        ).apply {
            icon = TIP_ICON
            addAction(NotificationAction.createSimple(actionText) {
                onOpenSettings()
            })
        }
    }

    internal fun createAddedToIdeaVimRcNotification(
        message: String,
        onReloadIdeaVimRc: (() -> Unit)? = null
    ): Notification {
        return Notification(
            NOTIFICATION_GROUP_ID,
            APP_TITLE,
            message,
            NotificationType.INFORMATION
        ).apply {
            icon = TIP_ICON
            onReloadIdeaVimRc?.let { reload ->
                addAction(NotificationAction.createSimple(TIP_RELOAD_IDEAVIMRC_ACTION_TEXT) { reload() })
            }
        }
    }

    internal fun createAddToIdeaVimRcFailedNotification(
        reason: AddTipToIdeaVimRc.FailureReason
    ): Notification {
        val text = when (reason) {
            AddTipToIdeaVimRc.FailureReason.ReadOnly -> TIP_ADD_TO_IDEAVIMRC_READONLY_TEXT
            AddTipToIdeaVimRc.FailureReason.NotAccessible -> TIP_ADD_TO_IDEAVIMRC_NOT_ACCESSIBLE_TEXT
            AddTipToIdeaVimRc.FailureReason.NothingToAdd -> TIP_ADD_TO_IDEAVIMRC_NOTHING_TEXT
        }
        return Notification(
            NOTIFICATION_GROUP_ID,
            APP_TITLE,
            text,
            NotificationType.WARNING
        ).apply {
            icon = TIP_ICON
        }
    }

    internal fun createReloadIdeaVimRcFailedNotification(): Notification {
        return Notification(
            NOTIFICATION_GROUP_ID,
            APP_TITLE,
            TIP_RELOAD_IDEAVIMRC_FAILED_TEXT,
            NotificationType.WARNING
        ).apply {
            icon = TIP_ICON
        }
    }

    private fun renderTipAsHtml(tip: VimTip): String {
        val summaryHtml = escapeHtml(tip.summary)
        val detailsHtml = tip.details.joinToString(DETAILS_SEPARATOR) { escapeHtml(it) }
        val cleanSummary = "$SUMMARY_DIV_OPEN$SUMMARY_OPEN$summaryHtml$SUMMARY_CLOSE$SUMMARY_DIV_CLOSE"

        return buildString {
            append(HTML_OPEN)
            append(WRAPPER_OPEN)
            append(cleanSummary)
            append(DETAILS_OPEN)
            append(detailsHtml)
            append(DETAILS_CLOSE)
            tip.mnemonic?.takeIf(String::isNotBlank)?.let { mnemonic ->
                append(mnemonicOpen(ColorUtil.toHex(mnemonicForeground())))
                append(escapeHtml(TIP_MNEMONIC_LABEL))
                append(" ")
                append(escapeHtml(mnemonic))
                append(MNEMONIC_CLOSE)
            }
            append(WRAPPER_CLOSE)
            append(HTML_CLOSE)
        }
    }

    private fun mnemonicForeground(): java.awt.Color {
        return ColorUtil.mix(UIUtil.getLabelForeground(), UIUtil.getContextHelpForeground(), MNEMONIC_DIM_RATIO)
    }

    private fun mnemonicOpen(color: String): String {
        return "<div style=\"margin-top:4px;font-style:italic;color:#$color;\">"
    }

    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    companion object {
        val APP_TITLE: String = MyBundle.message("appTitle")
        val NOTIFICATION_GROUP_ID: String = MyBundle.message("notificationGroupId")
        val TIP_NEXT_ACTION_TEXT: String = MyBundle.message("tipNextAction")
        val TIP_DONT_SHOW_AGAIN_ACTION_TEXT: String = MyBundle.message("tipDontShowAgainAction")
        val TIP_EXCLUDED_WITH_MANAGEMENT_TEXT: String = MyBundle.message("tipExcludedWithManagementMessage")
        val TIP_MANAGE_EXCLUDED_ACTION_TEXT: String = MyBundle.message("tipManageExcludedAction")
        val ADVANCED_TIPS_AVAILABLE_TEXT: String = MyBundle.message("advancedTipsAvailableMessage")
        val ADVANCED_TIPS_OPEN_SETTINGS_ACTION_TEXT: String = MyBundle.message("advancedTipsOpenSettingsAction")
        const val ADVANCED_TITLE_MARKER: String = "◆"
        val TIP_ADD_TO_IDEAVIMRC_ACTION_TEXT: String = MyBundle.message("tipAddToIdeaVimRcAction")
        val TIP_RELOAD_IDEAVIMRC_ACTION_TEXT: String = MyBundle.message("tipReloadIdeaVimRcAction")
        val TIP_ADDED_TO_IDEAVIMRC_TEXT: String = MyBundle.message("tipAddedToIdeaVimRcMessage")
        val TIP_ALREADY_IN_IDEAVIMRC_TEXT: String = MyBundle.message("tipAlreadyInIdeaVimRcMessage")
        val TIP_CREATE_IDEAVIMRC_GUIDANCE_TEXT: String = MyBundle.message("tipCreateIdeaVimRcGuidanceMessage")
        val TIP_ADD_TO_IDEAVIMRC_READONLY_TEXT: String = MyBundle.message("tipAddToIdeaVimRcReadOnlyMessage")
        val TIP_ADD_TO_IDEAVIMRC_NOT_ACCESSIBLE_TEXT: String = MyBundle.message("tipAddToIdeaVimRcNotAccessibleMessage")
        val TIP_ADD_TO_IDEAVIMRC_NOTHING_TEXT: String = MyBundle.message("tipAddToIdeaVimRcNothingToAddMessage")
        val TIP_RELOADED_IDEAVIMRC_TEXT: String = MyBundle.message("tipReloadedIdeaVimRcMessage")
        val TIP_RELOAD_IDEAVIMRC_FAILED_TEXT: String = MyBundle.message("tipReloadIdeaVimRcFailedMessage")
        val TIP_MNEMONIC_LABEL: String = MyBundle.message("tipMnemonicLabel")
        val TIP_NOTE_ACTION_TEXT: String = MyBundle.message("tipNoteAction")
        val TIP_NOTE_DIALOG_TITLE: String = MyBundle.message("tipNoteDialogTitle")
        val TIP_NOTE_DIALOG_MESSAGE: String = MyBundle.message("tipNoteDialogMessage")
        val TIP_NOTE_SAVED_TEXT: String = MyBundle.message("tipNoteSavedMessage")
        val TIP_ICON = IconLoader.getIcon("/icons/vimCoach.svg", TipNotificationFactory::class.java)

        private const val DETAILS_SEPARATOR = "<br/>"
        private const val HTML_OPEN = "<html>"
        private const val HTML_CLOSE = "</html>"
        private const val WRAPPER_OPEN = "<div>"
        private const val WRAPPER_CLOSE = "</div>"
        private const val SUMMARY_OPEN = "<b>"
        private const val SUMMARY_CLOSE = "</b>"
        private const val SUMMARY_DIV_OPEN = "<div style=\"margin-top:5px;\">"
        private const val SUMMARY_DIV_CLOSE = "</div>"
        private const val MNEMONIC_CLOSE = "</div>"
        private const val MNEMONIC_DIM_RATIO = 0.55
        private const val DETAILS_OPEN = "<div style=\"margin-top:8px;margin-bottom:8px;\">"
        private const val DETAILS_CLOSE = "</div>"
    }
}

internal data class TipNotificationActions(
    val onShowNextTip: (() -> Unit)? = null,
    val onExcludeTip: ((Notification) -> Unit)? = null,
    val onAddToIdeaVimRc: (() -> Unit)? = null,
    val onRecordNote: (() -> Unit)? = null
)
