package com.github.pooryam92.vimcoach.notifications

import com.github.pooryam92.vimcoach.MyBundle
import com.github.pooryam92.vimcoach.services.VimTip
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

internal class VimTipNotificationFactory {

    fun create(project: Project, tip: VimTip, onMoreInfo: () -> Unit): Notification {
        val content = "<html><b>${tip.summary}</b></html>"
        val notification = Notification(
            VimTipNotifier.NOTIFICATION_GROUP_ID,
            VimTipNotifier.APP_TITLE,
            content,
            NotificationType.INFORMATION
        )
        notification.icon = VimTipNotifier.TIP_ICON
        notification.addAction(
            NotificationAction.createSimpleExpiring(MyBundle.message("tipMoreInfoAction")) {
                onMoreInfo()
            }
        )
        return notification
    }
}
