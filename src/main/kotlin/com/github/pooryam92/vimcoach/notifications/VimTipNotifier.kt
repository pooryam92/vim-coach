package com.github.pooryam92.vimcoach.notifications

import com.github.pooryam92.vimcoach.MyBundle
import com.github.pooryam92.vimcoach.services.VimTip
import com.github.pooryam92.vimcoach.services.VimTipService
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

class VimTipNotifier(
    private val tipService: VimTipService
) {

    fun showRandomTip(project: Project) {
        showTip(project, tipService.getRandomTip())
    }

    private fun showTip(project: Project, tip: VimTip) {
        val notification = createNotification(tip)
        notification.addAction(NotificationAction.createSimple(MyBundle.message(TIP_NEXT_ACTION_KEY)) {
            notification.expire()
            showTip(project, tipService.getRandomTip())
        })
        notification.notify(project)
    }

    internal fun createNotification(tip: VimTip): Notification {
        val modeLabel = formatModeLabel(tip.mode)
        val detailsHtml = tip.details.replace("\n", "<br/>")
        val content = buildString {
            append("<html>")
            append(tip.summary)
            append("<br/><br/>")
            append(detailsHtml)
            if (modeLabel != null) {
                append("<br/><br/>Mode: ")
                append(modeLabel)
            }
            append("</html>")
        }
        val notification = Notification(
            NOTIFICATION_GROUP_ID,
            APP_TITLE,
            content,
            NotificationType.INFORMATION
        )
        return notification
    }

    private fun formatModeLabel(mode: String?): String? {
        return mode?.trim()?.ifBlank { null }
    }

    companion object {
        private const val APP_TITLE = "Vim Coach"
        const val NOTIFICATION_GROUP_ID = "Vim Tips"
        private const val TIP_NEXT_ACTION_KEY = "tipNextAction"
    }
}
