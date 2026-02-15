package com.github.pooryam92.vimcoach.notifications

import com.github.pooryam92.vimcoach.services.VimTip
import com.github.pooryam92.vimcoach.services.VimTipService
import com.intellij.notification.Notification
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
        notification.notify(project)
    }

    internal fun createNotification(tip: VimTip): Notification {
        val summaryHtml = tip.summary
        val detailsHtml = tip.details
        val content = buildString {
            append("<html>")
            append("<div>")
            append("<b>")
            append(summaryHtml)
            append("</b>")
            append("<div")
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
        const val APP_TITLE = "Vim Coach"
        const val NOTIFICATION_GROUP_ID = "Vim Tips"
        val TIP_ICON = IconLoader.getIcon("/icons/vimCoach.svg", VimTipNotifier::class.java)
    }
}
