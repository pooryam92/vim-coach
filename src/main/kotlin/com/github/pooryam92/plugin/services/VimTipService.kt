package com.github.pooryam92.plugin.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class VimTipService(project: Project) : TipProvider {

    private val cache = project.service<TipCache>()

    override fun getRandomTip(): VimTip = getTips().random()

    override fun getAllTips(): List<VimTip> = getTips()

    private fun getTips(): List<VimTip> {
        return cache.get()
            ?: listOf(VimTip("No tips found.", "Tips have not been loaded yet.", null))
    }

    data class VimTip(
        val summary: String,
        val details: String,
        val category: String? = null
    )
}
