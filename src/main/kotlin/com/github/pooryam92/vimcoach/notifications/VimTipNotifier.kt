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
        val notification = createNotification(tip)
        notification.addAction(NotificationAction.createSimple(MyBundle.message(TIP_NEXT_ACTION_KEY)) {
            notification.expire()
            showTip(project, tipService.getRandomTip())
        })
        notification.notify(project)
    }

    internal fun createNotification(tip: VimTip): Notification {
        val summaryHtml = tip.summary
        val detailsHtml = tip.details
        val content = buildString {
            append("<html>")
            append("<div style=\"margin:6px 0 4px 0; line-height:1.45;\">")
            append("<b>")
            append(summaryHtml)
            append("</b>")
            append("<div style=\"margin-top:6px;\">")
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

    companion object {
        private const val APP_TITLE = "Vim Coach"
        const val NOTIFICATION_GROUP_ID = "Vim Tips"
        private const val TIP_NEXT_ACTION_KEY = "tipNextAction"
        private val TIP_ICON = IconLoader.getIcon("/icons/vimCoach.svg", VimTipNotifier::class.java)
    }
}
