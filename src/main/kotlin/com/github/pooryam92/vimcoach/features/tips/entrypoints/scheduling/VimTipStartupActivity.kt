package com.github.pooryam92.vimcoach.features.tips.entrypoints.scheduling

import com.github.pooryam92.vimcoach.features.tips.application.loading.RefreshTips
import com.github.pooryam92.vimcoach.features.tips.application.notifications.ShowTips
import com.github.pooryam92.vimcoach.features.tips.application.scheduling.ScheduleTips
import com.github.pooryam92.vimcoach.features.tips.persistence.SettingsRepository
import com.intellij.openapi.components.service
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class VimTipStartupActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        val settingsService = ApplicationManager.getApplication().service<SettingsRepository>()
        val periodicScheduler = project.service<ScheduleTips>()
        val refreshTips = service<RefreshTips>()
        val showTips = project.service<ShowTips>()
        if (settingsService.isPeriodicTipsEnabled()) {
            periodicScheduler.start()
        }
        object : Task.Backgroundable(project, "Checking for Vim tips updates", false) {
            override fun run(indicator: ProgressIndicator) {
                refreshTips.checkForUpdates()
                if (settingsService.isShowTipsOnStartupEnabled()) {
                    ApplicationManager.getApplication().invokeLater {
                        showTips.showRandomTipIfNoneActive()
                    }
                }
            }
        }.queue()
    }
}
