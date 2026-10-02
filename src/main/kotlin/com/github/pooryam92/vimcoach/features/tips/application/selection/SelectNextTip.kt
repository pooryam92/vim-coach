package com.github.pooryam92.vimcoach.features.tips.application.selection

import com.github.pooryam92.vimcoach.features.tips.domain.TipHash
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.persistence.SettingsRepository
import com.github.pooryam92.vimcoach.features.tips.persistence.TipRotationRepository
import com.github.pooryam92.vimcoach.features.tips.persistence.VimTipRepository
import com.intellij.openapi.components.service
import kotlin.random.Random

/**
 * Single chokepoint for "which tip does the user see next": builds a [TipSelectionContext] from
 * [SettingsRepository], runs it through the [TipFilter] chain, then draws with [pickNext] against
 * the persisted [TipRotationRepository]. Registered as an application service so every project and
 * entry point shares one rotation.
 */
class SelectNextTip() {
    private var injectedTipRepository: VimTipRepository? = null
    private var injectedSettingsService: SettingsRepository? = null
    private var injectedRotationRepository: TipRotationRepository? = null

    internal constructor(
        tipRepository: VimTipRepository,
        rotationRepository: TipRotationRepository,
        settingsService: SettingsRepository? = null
    ) : this() {
        injectedTipRepository = tipRepository
        injectedSettingsService = settingsService
        injectedRotationRepository = rotationRepository
    }

    private val filters: List<TipFilter> =
        listOf(categoryFilter, excludedTipsFilter, configTipsFilter, advancedTipsFilter)

    fun select(includeConfigTips: Boolean): VimTip {
        val allTips = tipRepository().getTips()
        if (allTips.isEmpty()) return FALLBACK_TIP

        val context = buildContext(includeConfigTips)
        val filteredPool = filters.fold(allTips) { pool, filter -> filter.apply(pool, context) }
        return drawAndRecord(filteredPool, allTips) ?: FILTERED_FALLBACK_TIP
    }

    @Synchronized
    private fun drawAndRecord(pool: List<VimTip>, allTips: List<VimTip>): VimTip? {
        val rotation = rotationRepository()
        val progress = rotation.getProgress()
        val pick = pickNext(pool, progress.timesShown, progress.lastShownKey, Random) ?: return null

        rotation.recordShown(pick.key, pick.countToStore, contentKeys(allTips))
        return pick.tip
    }

    // Whole cache, not the filtered pool: disabled or excluded tips keep their progress.
    private fun contentKeys(tips: List<VimTip>): Set<String> {
        return tips.mapTo(mutableSetOf()) { TipHash.fromContent(it).value }
    }

    private fun buildContext(includeConfigTips: Boolean): TipSelectionContext {
        val settings = settingsService()
        val availableCategories = tipRepository().getCategories().values
        val enabledCategories = when {
            availableCategories.isEmpty() -> emptyList()
            settings == null -> availableCategories
            else -> settings.getEnabledTipCategories(availableCategories)
        }

        return TipSelectionContext(
            availableCategories = availableCategories,
            enabledCategories = enabledCategories,
            hiddenTipHashes = settings?.getHiddenTipHashes()?.toSet() ?: emptySet(),
            showAdvancedTips = settings?.isShowAdvancedTipsEnabled() ?: false,
            includeConfigTips = includeConfigTips,
        )
    }

    private fun tipRepository(): VimTipRepository = injectedTipRepository ?: service()

    private fun rotationRepository(): TipRotationRepository = injectedRotationRepository ?: service()

    // No settings service (e.g. an unconfigured cache outside a project) means we cannot know the
    // user's opt-in, so we fall back to the safe defaults: hide advanced tips, hide nothing else.
    private fun settingsService(): SettingsRepository? {
        injectedSettingsService?.let { return it }
        return runCatching { service<SettingsRepository>() }.getOrNull()
    }

    private companion object {
        val FALLBACK_TIP = VimTip(
            summary = "No tips found.",
            details = listOf("Tips have not been loaded yet.")
        )
        val FILTERED_FALLBACK_TIP = VimTip(
            summary = "No tips match the selected categories.",
            details = listOf(
                "Enable a matching category, or turn on \"Show advanced tips\", in Vim Coach settings."
            )
        )
    }
}
