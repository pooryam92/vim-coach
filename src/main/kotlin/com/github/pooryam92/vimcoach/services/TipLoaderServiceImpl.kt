package com.github.pooryam92.vimcoach.services

import com.github.pooryam92.vimcoach.services.source.TipSourceLoadResult
import com.github.pooryam92.vimcoach.services.source.TipSourceService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

class TipLoaderServiceImpl(project: Project) : TipLoaderService {
    private val tipService = project.service<VimTipService>()
    private val tipSource = project.service<TipSourceService>()

    override fun loadTips(): TipLoadResult {
        if (tipService.countTips() > 0) {
            return TipLoadResult.SkippedAlreadyLoaded
        }
        return fetchAndSave()
    }

    override fun refetchTips(): TipLoadResult = fetchAndSave()

    private fun fetchAndSave(): TipLoadResult {
        return when (val sourceResult = tipSource.loadTips()) {
            is TipSourceLoadResult.Success -> {
                tipService.saveTips(sourceResult.tips)
                TipLoadResult.Updated(sourceResult.tips.size)
            }

            TipSourceLoadResult.Empty -> TipLoadResult.NoData
            is TipSourceLoadResult.Failure -> TipLoadResult.Failed(sourceResult.message, sourceResult.cause)
        }
    }
}
