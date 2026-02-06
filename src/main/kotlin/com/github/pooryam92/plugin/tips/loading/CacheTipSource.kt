package com.github.pooryam92.plugin.tips.loading

import com.github.pooryam92.plugin.services.TipCache
import com.github.pooryam92.plugin.services.VimTipService
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class CacheTipSource(project: Project) : TipSource {
    private val cache = project.service<TipCache>()

    override fun loadTips(): List<VimTipService.VimTip>? {
        return cache.get()
    }

    fun saveTips(tips: List<VimTipService.VimTip>) {
        cache.set(tips)
    }
}
