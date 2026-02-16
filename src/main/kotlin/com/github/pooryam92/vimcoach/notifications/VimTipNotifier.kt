package com.github.pooryam92.vimcoach.notifications

import com.github.pooryam92.vimcoach.services.VimTip
import com.github.pooryam92.vimcoach.services.VimTipService
import com.intellij.notification.Notification
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader

class VimTipNotifier(
    private val tipService: VimTipService
) {
    private val notificationFactory = VimTipNotificationFactory()

    fun showRandomTip(project: Project) {
        showTip(project, tipService.getRandomTip())
    }

    private fun showTip(project: Project, tip: VimTip) {
        val notification = createNotification(project, tip)
        notification.notify(project)
    }

    internal fun createNotification(project: Project, tip: VimTip): Notification =
        notificationFactory.create(project, tip) {
            TipDetailsDialog(project, tip).show()
        }

    companion object {
        const val APP_TITLE = "Vim Coach"
        const val NOTIFICATION_GROUP_ID = "Vim Tips"
        val TIP_ICON = IconLoader.getIcon("/icons/vimCoach.svg", VimTipNotifier::class.java)
    }
}
