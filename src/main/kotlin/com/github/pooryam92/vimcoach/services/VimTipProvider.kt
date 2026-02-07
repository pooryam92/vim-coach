package com.github.pooryam92.vimcoach.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class VimTipProvider(project: Project) {
    private val vimTips = project.service<VimTips>()

    fun getRandomTip(): VimTip {
        return vimTips.getTips().random()
    }
}


