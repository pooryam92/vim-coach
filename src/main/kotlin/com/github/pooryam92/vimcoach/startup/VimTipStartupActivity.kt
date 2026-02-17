package com.github.pooryam92.vimcoach.startup

import com.github.pooryam92.vimcoach.notifications.VimTipNotifier
import com.github.pooryam92.vimcoach.services.TipLoaderService
import com.github.pooryam92.vimcoach.services.VimTipService
import com.intellij.openapi.components.service
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class VimTipStartupActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        val loader = project.service<TipLoaderService>()
        val notifier = VimTipNotifier(project.service<VimTipService>())
        object : Task.Backgroundable(project, "Checking for Vim tips updates", false) {
            override fun run(indicator: ProgressIndicator) {
                loader.checkForUpdates()
                ApplicationManager.getApplication().invokeLater {
                    notifier.showRandomTip(project)
                }
            }
        }.queue()
    }
}
