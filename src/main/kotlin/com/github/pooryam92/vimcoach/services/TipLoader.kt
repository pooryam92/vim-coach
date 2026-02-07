package com.github.pooryam92.vimcoach.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class TipLoader(project: Project) {
    private val vimTips = project.service<VimTips>()
    private val remoteSource = project.service<RemoteTipSource>()

    fun loadTips() {
        if (vimTips.countTips() > 0) {
            return
        }

        val tips = remoteSource.loadTips()
        if (!tips.isNullOrEmpty()) {
            vimTips.saveTips(tips)
        }
    }
}
