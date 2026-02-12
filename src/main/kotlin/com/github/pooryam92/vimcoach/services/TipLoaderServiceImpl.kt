package com.github.pooryam92.vimcoach.services

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

class TipLoaderServiceImpl(project: Project) : TipLoaderService {
    private val tipService = project.service<VimTipService>()
    private val remoteSource = project.service<RemoteTipSourceService>()

    override fun loadTips() {
        if (tipService.countTips() > 0) {
            return
        }

        val tips = remoteSource.loadTips()
        if (!tips.isNullOrEmpty()) {
            tipService.saveTips(tips)
        }
    }

    override fun refetchTips() {
        val tips = remoteSource.loadTips()
        if (!tips.isNullOrEmpty()) {
            tipService.saveTips(tips)
        }
    }
}
