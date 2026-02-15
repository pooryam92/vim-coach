package com.github.pooryam92.vimcoach.services

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

class TipLoaderServiceImpl(project: Project) : TipLoaderService {
    private val tipService = project.service<VimTipService>()
    private val remoteSource = project.service<RemoteTipSourceService>()

    override fun loadTips(): TipLoadResult {
        if (tipService.countTips() > 0) {
            return TipLoadResult.SkippedAlreadyLoaded
        }
        return fetchAndSave()
    }

    override fun refetchTips(): TipLoadResult = fetchAndSave()

    private fun fetchAndSave(): TipLoadResult {
        return when (val remoteResult = remoteSource.loadTips()) {
            is RemoteTipLoadResult.Success -> {
                tipService.saveTips(remoteResult.tips)
                TipLoadResult.Updated(remoteResult.tips.size)
            }

            RemoteTipLoadResult.Empty -> TipLoadResult.NoData
            is RemoteTipLoadResult.Failure -> TipLoadResult.Failed(remoteResult.message, remoteResult.cause)
        }
    }
}
