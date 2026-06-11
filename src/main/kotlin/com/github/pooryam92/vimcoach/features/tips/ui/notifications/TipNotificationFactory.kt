package com.github.pooryam92.vimcoach.features.tips.ui.notifications

import com.github.pooryam92.vimcoach.core.shared.i18n.MyBundle
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.util.IconLoader

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
        // Kept last so it does not sit where "Next tip" usually is and get clicked by accident.
        actions.onAddToIdeaVimRc?.let { onAddToIdeaVimRc ->
            notification.addAction(NotificationAction.createSimple(TIP_ADD_TO_IDEAVIMRC_ACTION_TEXT) {
                onAddToIdeaVimRc()
            })
        }
        return notification
    }

    internal fun createNotification(tip: VimTip): Notification {
        return Notification(
            NOTIFICATION_GROUP_ID,
            APP_TITLE,
            renderTipAsHtml(tip),
            NotificationType.INFORMATION
        ).apply {
            icon = TIP_ICON
        }
    }

    internal fun createTipExcludedNotification(onOpenSettings: () -> Unit): Notification {
        return Notification(
            NOTIFICATION_GROUP_ID,
            APP_TITLE,
            TIP_EXCLUDED_WITH_MANAGEMENT_TEXT,
            NotificationType.INFORMATION
        ).apply {
            icon = TIP_ICON
            addAction(NotificationAction.createSimple(TIP_MANAGE_EXCLUDED_ACTION_TEXT) {
                onOpenSettings()
            })
        }
    }

    internal fun createAddedToIdeaVimRcNotification(message: String): Notification {
        return Notification(
            NOTIFICATION_GROUP_ID,
            APP_TITLE,
            message,
            NotificationType.INFORMATION
        ).apply {
            icon = TIP_ICON
        }
    }

    internal fun createAddToIdeaVimRcFailedNotification(): Notification {
        return Notification(
            NOTIFICATION_GROUP_ID,
            APP_TITLE,
            TIP_ADD_TO_IDEAVIMRC_FAILED_TEXT,
            NotificationType.WARNING
        ).apply {
            icon = TIP_ICON
        }
    }

    private fun renderTipAsHtml(tip: VimTip): String {
        val summaryHtml = escapeHtml(tip.summary)
        val detailsHtml = tip.details.joinToString(DETAILS_SEPARATOR) { escapeHtml(it) }
        return buildString {
            append(HTML_OPEN)
            append(WRAPPER_OPEN)
            append(SUMMARY_OPEN)
            append(summaryHtml)
            append(SUMMARY_CLOSE)
            append(DETAILS_OPEN)
            append(detailsHtml)
            append(DETAILS_CLOSE)
            append(WRAPPER_CLOSE)
            append(HTML_CLOSE)
        }
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
        val TIP_ADD_TO_IDEAVIMRC_ACTION_TEXT: String = MyBundle.message("tipAddToIdeaVimRcAction")
        val TIP_ADDED_TO_IDEAVIMRC_TEXT: String = MyBundle.message("tipAddedToIdeaVimRcMessage")
        val TIP_ALREADY_IN_IDEAVIMRC_TEXT: String = MyBundle.message("tipAlreadyInIdeaVimRcMessage")
        val TIP_ADD_TO_IDEAVIMRC_FAILED_TEXT: String = MyBundle.message("tipAddToIdeaVimRcFailedMessage")
        val TIP_ICON = IconLoader.getIcon("/icons/vimCoach.svg", TipNotificationFactory::class.java)

        private const val DETAILS_SEPARATOR = "<br/>"
        private const val HTML_OPEN = "<html>"
        private const val HTML_CLOSE = "</html>"
        private const val WRAPPER_OPEN = "<div>"
        private const val WRAPPER_CLOSE = "</div>"
        private const val SUMMARY_OPEN = "<b>"
        private const val SUMMARY_CLOSE = "</b>"
        private const val DETAILS_OPEN = "<div style=\"margin-top:12px;margin-bottom:12px;\">"
        private const val DETAILS_CLOSE = "</div>"
    }
}

internal data class TipNotificationActions(
    val onShowNextTip: (() -> Unit)? = null,
    val onExcludeTip: ((Notification) -> Unit)? = null,
    val onAddToIdeaVimRc: (() -> Unit)? = null
)
