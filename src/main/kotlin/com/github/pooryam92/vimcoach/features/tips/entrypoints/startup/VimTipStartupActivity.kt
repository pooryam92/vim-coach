package com.github.pooryam92.vimcoach.features.tips.entrypoints.startup

import com.github.pooryam92.vimcoach.features.tips.application.PeriodicTipSchedulerService
import com.github.pooryam92.vimcoach.features.tips.application.TipNotificationService
import com.github.pooryam92.vimcoach.features.tips.application.TipLoaderService
import com.github.pooryam92.vimcoach.features.tips.state.VimCoachSettingsService
import com.intellij.openapi.components.service
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class VimTipStartupActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        val settingsService = ApplicationManager.getApplication().service<VimCoachSettingsService>()
        val periodicScheduler = project.service<PeriodicTipSchedulerService>()
        val loader = service<TipLoaderService>()
        val tipNotificationService = project.service<TipNotificationService>()
        if (settingsService.isPeriodicTipsEnabled()) {
            periodicScheduler.start()
        }
        object : Task.Backgroundable(project, "Checking for Vim tips updates", false) {
            override fun run(indicator: ProgressIndicator) {
                loader.checkForUpdates()
                if (settingsService.isShowTipsOnStartupEnabled()) {
                    ApplicationManager.getApplication().invokeLater {
                        tipNotificationService.showRandomTip()
                    }
                }
            }
        }.queue()
    }
}
