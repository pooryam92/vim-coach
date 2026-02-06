package com.github.pooryam92.plugin.startup

import com.github.pooryam92.plugin.notifications.VimTipNotifier
import com.github.pooryam92.plugin.services.TipCache
import com.github.pooryam92.plugin.tips.loading.TipLoader
import com.github.pooryam92.plugin.services.VimTipService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task

class VimTipStartupActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        val cache = project.service<TipCache>()
        val loader = project.service<TipLoader>()
        val notifier = VimTipNotifier(project.service<VimTipService>())
        object : Task.Backgroundable(project, "Loading Vim tips", false) {
            override fun run(indicator: ProgressIndicator) {
                cache.set(loader.loadTips())
                ApplicationManager.getApplication().invokeLater {
                    notifier.showRandomTip(project)
                }
            }
        }.queue()
    }
}
