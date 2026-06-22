package com.github.pooryam92.vimcoach.features.tips.application.loading

import com.github.pooryam92.vimcoach.features.tips.domain.TipLoadResult
import com.github.pooryam92.vimcoach.features.tips.domain.TipMetadata
import com.github.pooryam92.vimcoach.features.tips.domain.TipSourceLoadResult
import com.github.pooryam92.vimcoach.features.tips.persistence.VimTipRepository
import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.extensions.PluginId
import java.util.concurrent.atomic.AtomicBoolean

class TipRefresh() : RefreshTips {
    private var injectedTipService: VimTipRepository? = null
    private var injectedTipSource: TipSourceService? = null
    private var injectedCurrentPluginVersion: (() -> String?)? = null

    internal constructor(
        tipService: VimTipRepository? = null,
        tipSource: TipSourceService? = null,
        currentPluginVersion: (() -> String?)? = null
    ) : this() {
        injectedTipService = tipService
        injectedTipSource = tipSource
        injectedCurrentPluginVersion = currentPluginVersion
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
        return fetchAndSave(conditional = hasUsableCachedTips() && !cacheStaleAfterUpgrade())
    }

    /**
     * True when the cached tips were parsed by a different plugin version than the one running, so a
     * conditional 304 would keep serving an under-parsed cache. A null current version (descriptor
     * unresolvable) is treated as "not stale" to preserve the ETag optimization rather than refetch
     * on every startup.
     */
    private fun cacheStaleAfterUpgrade(): Boolean {
        val current = currentPluginVersion() ?: return false
        val cached = tipService().getMetadata().pluginVersion
        if (cached == current) {
            return false
        }
        logger.info("Vim tip cache parsed by plugin version '$cached' but running '$current'; forcing unconditional refetch")
        return true
    }

    private fun fetchAndSave(conditional: Boolean): TipLoadResult {
        logger.info("Fetching Vim tips (conditional=$conditional)")
        val sourceResult = loadFromSource(conditional)
        return toTipLoadResult(sourceResult)
    }

    private fun hasUsableCachedTips(): Boolean {
        val tipCount = tipService().countTips()
        if (tipCount == 0) {
            logger.info("Current Vim tip cache is empty")
            return false
        }

        val categories = tipService().getCategories()
        val hasCategories = categories.isNotEmpty()
        logger.info(
            "Current Vim tip cache size: $tipCount, recovered categories: ${categories.values.size}, usable=$hasCategories"
        )
        return hasCategories
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
        tipService().saveMetadata(stampPluginVersion(sourceResult.metadata))
        logger.info("Saved ${sourceResult.tips.size} Vim tips from source")
        return TipLoadResult.Updated(sourceResult.tips.size)
    }

    private fun stampPluginVersion(metadata: TipMetadata): TipMetadata =
        metadata.copy(pluginVersion = currentPluginVersion())

    private fun markRefreshTimestamp(): TipLoadResult {
        val updatedMetadata = tipService().getMetadata().copy(
            lastFetchTimestamp = System.currentTimeMillis()
        )
        tipService().saveMetadata(updatedMetadata)
        return TipLoadResult.NotModified
    }

    private fun tipService(): VimTipRepository = injectedTipService ?: service()

    private fun tipSource(): TipSourceService = injectedTipSource ?: service()

    private fun currentPluginVersion(): String? =
        (injectedCurrentPluginVersion ?: ::resolvePluginVersion).invoke()

    private fun resolvePluginVersion(): String? =
        PluginManager.getInstance().findEnabledPlugin(PluginId.getId(VIM_COACH_PLUGIN_ID))?.version

    private companion object {
        const val VIM_COACH_PLUGIN_ID = "com.github.pooryam92.vimcoach"
        val logger = Logger.getInstance(TipRefresh::class.java)
    }
}
