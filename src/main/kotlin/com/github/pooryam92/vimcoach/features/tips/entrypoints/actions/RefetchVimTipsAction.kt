package com.github.pooryam92.vimcoach.features.tips.entrypoints.actions

import com.github.pooryam92.vimcoach.core.shared.i18n.MyBundle
import com.github.pooryam92.vimcoach.features.tips.ui.notifications.TipNotificationFactory
import com.github.pooryam92.vimcoach.features.tips.application.TipLoaderService
import com.github.pooryam92.vimcoach.features.tips.domain.TipLoadResult
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project

class RefetchVimTipsAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val loader = service<TipLoaderService>()

        object : Task.Backgroundable(project, MyBundle.message("refetchTipsProgress"), false) {
            override fun run(indicator: ProgressIndicator) {
                val result = loader.refetchTips()
                ApplicationManager.getApplication().invokeLater {
                    showResultNotification(project, result)
                }
            }
        }.queue()
    }

    private fun showResultNotification(project: Project, result: TipLoadResult) {
        val notification = buildResultNotification(result)
        showNotification(project, notification.content, notification.type)
    }

    private fun showNotification(project: Project, content: String, type: NotificationType) {
        Notification(
            TipNotificationFactory.NOTIFICATION_GROUP_ID,
            TipNotificationFactory.APP_TITLE,
            content,
            type
        ).notify(project)
    }

    private fun buildResultNotification(result: TipLoadResult): ActionResultNotification {
        return when (result) {
            is TipLoadResult.Updated -> ActionResultNotification(
                content = MyBundle.message("refetchTipsSuccess", result.tipCount),
                type = NotificationType.INFORMATION
            )

            TipLoadResult.NotModified -> ActionResultNotification(
                content = MyBundle.message("refetchTipsNotModified"),
                type = NotificationType.INFORMATION
            )

            TipLoadResult.NoData -> ActionResultNotification(
                content = MyBundle.message("refetchTipsNoData"),
                type = NotificationType.WARNING
            )

            is TipLoadResult.Failed -> ActionResultNotification(
                content = MyBundle.message("refetchTipsError", result.message),
                type = NotificationType.ERROR
            )
        }
    }

    private data class ActionResultNotification(
        val content: String,
        val type: NotificationType
    )
}
