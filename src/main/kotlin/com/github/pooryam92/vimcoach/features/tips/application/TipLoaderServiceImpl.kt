package com.github.pooryam92.vimcoach.features.tips.application

import com.github.pooryam92.vimcoach.features.tips.domain.TipLoadResult
import com.github.pooryam92.vimcoach.features.tips.source.application.TipSourceService
import com.github.pooryam92.vimcoach.features.tips.source.domain.TipSourceLoadResult
import com.github.pooryam92.vimcoach.features.tips.state.VimTipService
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import java.util.concurrent.atomic.AtomicBoolean

class TipLoaderServiceImpl() : TipLoaderService {
    private var injectedTipService: VimTipService? = null
    private var injectedTipSource: TipSourceService? = null

    internal constructor(
        tipService: VimTipService? = null,
        tipSource: TipSourceService? = null
    ) : this() {
        injectedTipService = tipService
        injectedTipSource = tipSource
    }

    private val updatesChecked = AtomicBoolean(false)

    override fun refetchTips(): TipLoadResult {
        return fetchAndSave(conditional = false)
    }

    override fun checkForUpdates(): TipLoadResult {
        if (!updatesChecked.compareAndSet(false, true)) {
            logger.info("Skipping Vim tip update check because it already ran in this application session")
            return TipLoadResult.NotModified
        }
        return fetchAndSave(conditional = hasCachedTips())
    }

    private fun fetchAndSave(conditional: Boolean): TipLoadResult {
        logger.info("Fetching Vim tips (conditional=$conditional)")
        val sourceResult = loadFromSource(conditional)
        return toTipLoadResult(sourceResult)
    }

    private fun hasCachedTips(): Boolean {
        val tipCount = tipService().countTips()
        logger.info("Current Vim tip cache size: $tipCount")
        return tipCount > 0
    }

    private fun loadFromSource(conditional: Boolean): TipSourceLoadResult {
        return if (conditional) {
            tipSource().loadTipsConditional(tipService().getMetadata())
        } else {
            tipSource().loadTips()
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
        tipService().saveTips(sourceResult.tips)
        tipService().saveMetadata(sourceResult.metadata)
        logger.info("Saved ${sourceResult.tips.size} Vim tips from source")
        return TipLoadResult.Updated(sourceResult.tips.size)
    }

    private fun markRefreshTimestamp(): TipLoadResult {
        val updatedMetadata = tipService().getMetadata().copy(
            lastFetchTimestamp = System.currentTimeMillis()
        )
        tipService().saveMetadata(updatedMetadata)
        return TipLoadResult.NotModified
    }

    private fun tipService(): VimTipService = injectedTipService ?: service()

    private fun tipSource(): TipSourceService = injectedTipSource ?: service()

    private companion object {
        val logger = Logger.getInstance(TipLoaderServiceImpl::class.java)
    }
}
