package com.github.pooryam92.vimcoach.actions

import com.github.pooryam92.vimcoach.MyBundle
import com.github.pooryam92.vimcoach.notifications.VimTipNotifier
import com.github.pooryam92.vimcoach.services.TipLoaderService
import com.github.pooryam92.vimcoach.services.TipLoadResult
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
        val loader = project.service<TipLoaderService>()

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
        when (result) {
            is TipLoadResult.Updated -> {
                showNotification(
                    project,
                    MyBundle.message("refetchTipsSuccess", result.tipCount),
                    NotificationType.INFORMATION
                )
            }

            TipLoadResult.NotModified -> {
                showNotification(
                    project,
                    MyBundle.message("refetchTipsNotModified"),
                    NotificationType.INFORMATION
                )
            }

            TipLoadResult.NoData -> {
                showNotification(
                    project,
                    MyBundle.message("refetchTipsNoData"),
                    NotificationType.WARNING
                )
            }

            is TipLoadResult.Failed -> {
                showNotification(
                    project,
                    MyBundle.message("refetchTipsError", result.message),
                    NotificationType.ERROR
                )
            }
        }
    }

    private fun showNotification(project: Project, content: String, type: NotificationType) {
        Notification(
            VimTipNotifier.NOTIFICATION_GROUP_ID,
            VimTipNotifier.APP_TITLE,
            content,
            type
        ).notify(project)
    }
}
