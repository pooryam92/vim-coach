package com.github.pooryam92.vimcoach.features.tips.application

import com.github.pooryam92.vimcoach.features.tips.domain.TipLoadResult
import com.github.pooryam92.vimcoach.features.tips.source.application.TipSourceService
import com.github.pooryam92.vimcoach.features.tips.source.domain.TipSourceLoadResult
import com.github.pooryam92.vimcoach.features.tips.state.VimTipService
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project

class TipLoaderServiceImpl(project: Project) : TipLoaderService {
    private val tipService = service<VimTipService>()
    private val tipSource = project.service<TipSourceService>()

    override fun refetchTips(): TipLoadResult {
        return fetchAndSave(conditional = false)
    }

    override fun checkForUpdates(): TipLoadResult {
        return fetchAndSave(conditional = hasCachedTips())
    }

    private fun fetchAndSave(conditional: Boolean): TipLoadResult {
        logger.info("Fetching Vim tips (conditional=$conditional)")
        val sourceResult = loadFromSource(conditional)
        return toTipLoadResult(sourceResult)
    }

    private fun hasCachedTips(): Boolean {
        val tipCount = tipService.countTips()
        logger.info("Current Vim tip cache size: $tipCount")
        return tipCount > 0
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
            TipSourceLoadResult.NotModified -> {
                logger.info("Tip source returned not modified; refreshing fetch timestamp only")
                markRefreshTimestamp()
            }

            TipSourceLoadResult.Empty -> {
                logger.info("Tip source returned empty data")
                TipLoadResult.NoData
            }

            is TipSourceLoadResult.Failure -> {
                logger.warn("Tip source failed: ${sourceResult.message}", sourceResult.cause)
                TipLoadResult.Failed(sourceResult.message, sourceResult.cause)
            }
        }
    }

    private fun saveFetchedTips(sourceResult: TipSourceLoadResult.Success): TipLoadResult {
        tipService.saveTips(sourceResult.tips)
        tipService.saveMetadata(sourceResult.metadata)
        logger.info("Saved ${sourceResult.tips.size} Vim tips from source")
        return TipLoadResult.Updated(sourceResult.tips.size)
    }

    private fun markRefreshTimestamp(): TipLoadResult {
        val updatedMetadata = tipService.getMetadata().copy(
            lastFetchTimestamp = System.currentTimeMillis()
        )
        tipService.saveMetadata(updatedMetadata)
        return TipLoadResult.NotModified
    }

    private companion object {
        val logger = Logger.getInstance(TipLoaderServiceImpl::class.java)
    }
}
