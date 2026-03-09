package com.github.pooryam92.vimcoach.features.tips.ui.notifications

import com.github.pooryam92.vimcoach.core.shared.i18n.MyBundle
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.state.VimTipService
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import java.util.Collections
import java.util.WeakHashMap

class VimTipNotifier(
    private val tipService: VimTipService
) {

    fun showRandomTip(project: Project) {
        showTipReplacingActive(project, tipService.getRandomTip())
    }

    fun showRandomTipIfNoneActive(project: Project): Boolean {
        if (hasActiveTipNotification(project)) {
            return false
        }
        showTip(project, tipService.getRandomTip())
        return true
    }

    private fun showTip(project: Project, tip: VimTip) {
        val notification = createNotificationWithActions(project, tip)
        registerActiveTipNotification(project, notification)
        notification.notify(project)
    }

    private fun showTipReplacingActive(project: Project, tip: VimTip) {
        val notification = createNotificationWithActions(project, tip)
        expireActiveTipNotification(project)
        registerActiveTipNotification(project, notification)
        notification.notify(project)
    }

    internal fun createNotificationWithActions(project: Project, tip: VimTip): Notification {
        val notification = createNotification(tip)
        notification.addAction(NotificationAction.createSimple(TIP_NEXT_ACTION_TEXT) {
            notification.expire()
            showTipReplacingActive(project, tipService.getRandomTip())
        })
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

    private fun hasActiveTipNotification(project: Project): Boolean {
        synchronized(activeTipNotifications) {
            val existingNotification = activeTipNotifications[project] ?: return false
            if (existingNotification.isExpired) {
                activeTipNotifications.remove(project)
                return false
            }
            return true
        }
    }

    private fun expireActiveTipNotification(project: Project) {
        synchronized(activeTipNotifications) {
            activeTipNotifications.remove(project)?.expire()
        }
    }

    private fun registerActiveTipNotification(project: Project, notification: Notification) {
        synchronized(activeTipNotifications) {
            activeTipNotifications[project] = notification
        }
        notification.whenExpired {
            synchronized(activeTipNotifications) {
                if (activeTipNotifications[project] === notification) {
                    activeTipNotifications.remove(project)
                }
            }
        }
    }

    companion object {
        val APP_TITLE: String = MyBundle.message("appTitle")
        val NOTIFICATION_GROUP_ID: String = MyBundle.message("notificationGroupId")
        val TIP_NEXT_ACTION_TEXT: String = MyBundle.message("tipNextAction")
        val TIP_ICON = IconLoader.getIcon("/icons/vimCoach.svg", VimTipNotifier::class.java)
        val activeTipNotifications: MutableMap<Project, Notification> =
            Collections.synchronizedMap(WeakHashMap())

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
