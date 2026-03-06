package com.github.pooryam92.vimcoach.features.tips.application

import com.github.pooryam92.vimcoach.features.tips.domain.TipLoadResult
import com.github.pooryam92.vimcoach.features.tips.source.application.TipSourceService
import com.github.pooryam92.vimcoach.features.tips.source.domain.TipSourceLoadResult
import com.github.pooryam92.vimcoach.features.tips.state.VimTipService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

class TipLoaderServiceImpl(project: Project) : TipLoaderService {
    private val tipService = project.service<VimTipService>()
    private val tipSource = project.service<TipSourceService>()

    override fun refetchTips(): TipLoadResult {
        return fetchAndSave(conditional = false)
    }

    override fun checkForUpdates(): TipLoadResult {
        return fetchAndSave(conditional = hasCachedTips())
    }

    private fun fetchAndSave(conditional: Boolean): TipLoadResult {
        val sourceResult = loadFromSource(conditional)
        return toTipLoadResult(sourceResult)
    }

    private fun hasCachedTips(): Boolean {
        return tipService.countTips() > 0
    }

    private fun loadFromSource(conditional: Boolean): TipSourceLoadResult {
        return if (conditional) {
            tipSource.loadTipsConditional(tipService.getMetadata())
        } else {
            tipSource.loadTips()
        }
    }

    private fun toTipLoadResult(sourceResult: TipSourceLoadResult): TipLoadResult {
        return when (sourceResult) {
            is TipSourceLoadResult.Success -> saveFetchedTips(sourceResult)
            TipSourceLoadResult.NotModified -> markRefreshTimestamp()
            TipSourceLoadResult.Empty -> TipLoadResult.NoData
            is TipSourceLoadResult.Failure -> TipLoadResult.Failed(sourceResult.message, sourceResult.cause)
        }
    }

    private fun saveFetchedTips(sourceResult: TipSourceLoadResult.Success): TipLoadResult {
        tipService.saveTips(sourceResult.tips)
        tipService.saveMetadata(sourceResult.metadata)
        return TipLoadResult.Updated(sourceResult.tips.size)
    }

    private fun markRefreshTimestamp(): TipLoadResult {
        val updatedMetadata = tipService.getMetadata().copy(
            lastFetchTimestamp = System.currentTimeMillis()
        )
        tipService.saveMetadata(updatedMetadata)
        return TipLoadResult.NotModified
    }
}
