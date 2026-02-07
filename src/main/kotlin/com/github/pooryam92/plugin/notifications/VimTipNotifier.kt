package com.github.pooryam92.plugin.notifications

import com.github.pooryam92.plugin.MyBundle
import com.github.pooryam92.plugin.services.VimTip
import com.github.pooryam92.plugin.services.VimTipProvider
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

class VimTipNotifier(
    private val tipProvider: VimTipProvider
) {

    fun showRandomTip(project: Project) {
        showTip(project, tipProvider.getRandomTip())
    }

    private fun showTip(project: Project, tip: VimTip) {
        val notification = createNotification(project, tip)
        notification.notify(project)
    }

    private fun createNotification(project: Project, tip: VimTip): Notification {
        val notification = Notification(
            NOTIFICATION_GROUP_ID,
            MyBundle.message(TIP_TITLE_KEY),
            tip.summary,
            NotificationType.INFORMATION
        )
        notification.addAction(NotificationAction.createSimple(MyBundle.message(TIP_NEXT_ACTION_KEY)) {
            notification.expire()
            showTip(project, tipProvider.getRandomTip())
        })
        notification.addAction(NotificationAction.createSimple(MyBundle.message(TIP_MORE_ACTION_KEY)) {
            Messages.showInfoMessage(project, tip.details, MyBundle.message(TIP_MORE_TITLE_KEY))
        })
        return notification
    }

    companion object {
        const val NOTIFICATION_GROUP_ID = "Vim Tips"
        private const val TIP_TITLE_KEY = "tipTitle"
        private const val TIP_NEXT_ACTION_KEY = "tipNextAction"
        private const val TIP_MORE_ACTION_KEY = "tipMoreAction"
        private const val TIP_MORE_TITLE_KEY = "tipMoreTitle"
    }
}
