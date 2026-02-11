package com.github.pooryam92.vimcoach.startup

import com.github.pooryam92.vimcoach.notifications.VimTipNotifier
import com.github.pooryam92.vimcoach.services.VimTipService
import com.github.pooryam92.vimcoach.services.TipLoaderService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task

class VimTipStartupActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        val loader = project.service<TipLoaderService>()
        val notifier = VimTipNotifier(project.service<VimTipService>())
        object : Task.Backgroundable(project, "Loading Vim tips", false) {
            override fun run(indicator: ProgressIndicator) {
                ApplicationManager.getApplication().invokeLater {
                    loader.loadTips()
                    notifier.showRandomTip(project)
                }
            }
        }.queue()
    }
}
