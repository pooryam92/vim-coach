package com.github.pooryam92.vimcoach.actions

import com.github.pooryam92.vimcoach.MyBundle
import com.github.pooryam92.vimcoach.services.TipLoaderService
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task

class RefetchVimTipsAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val loader = project.service<TipLoaderService>()

        object : Task.Backgroundable(project, MyBundle.message("refetchTipsProgress"), false) {
            override fun run(indicator: ProgressIndicator) {
                try {
                    loader.refetchTips()
                    ApplicationManager.getApplication().invokeLater {
                        showNotification(
                            project,
                            MyBundle.message("refetchTipsSuccess"),
                            NotificationType.INFORMATION
                        )
                    }
                } catch (e: Exception) {
                    ApplicationManager.getApplication().invokeLater {
                        showNotification(
                            project,
                            MyBundle.message("refetchTipsError", e.message ?: "Unknown error"),
                            NotificationType.ERROR
                        )
                    }
                }
            }
        }.queue()
    }

    private fun showNotification(project: com.intellij.openapi.project.Project, content: String, type: NotificationType) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Vim Tips")
            .createNotification(content, type)
            .notify(project)
    }
}


