package com.github.pooryam92.vimcoach.notifications

import com.github.pooryam92.vimcoach.MyBundle
import com.github.pooryam92.vimcoach.services.VimTip
import com.github.pooryam92.vimcoach.services.VimTipService
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader

class VimTipNotifier(
    private val tipService: VimTipService
) {

    fun showRandomTip(project: Project) {
        showTip(project, tipService.getRandomTip())
    }

    private fun showTip(project: Project, tip: VimTip) {
        val notification = createNotificationWithActions(project, tip)
        notification.notify(project)
    }

    internal fun createNotificationWithActions(project: Project, tip: VimTip): Notification {
        val notification = createNotification(tip)
        notification.addAction(NotificationAction.createSimple(TIP_NEXT_ACTION_TEXT) {
            notification.expire()
            showTip(project, tipService.getRandomTip())
        })
        return notification
    }

    internal fun createNotification(tip: VimTip): Notification {
        val summaryHtml = escapeHtml(tip.summary)
        val detailsHtml = tip.details.joinToString("<br/>") { escapeHtml(it) }
        val content = buildString {
            append("<html>")
            append("<div>")
            append("<b>")
            append(summaryHtml)
            append("</b>")
            append("<div style=\"margin-top:12px;margin-bottom:12px;\">")
            append(detailsHtml)
            append("</div>")
            append("</div>")
            append("</html>")
        }
        val notification = Notification(
            NOTIFICATION_GROUP_ID,
            APP_TITLE,
            content,
            NotificationType.INFORMATION
        )
        notification.icon = TIP_ICON
        return notification
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
        val TIP_ICON = IconLoader.getIcon("/icons/vimCoach.svg", VimTipNotifier::class.java)
    }
}
